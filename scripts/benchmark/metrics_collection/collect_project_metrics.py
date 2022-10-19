"""
This script allows you to collect metrics for each project from a dataset.

It accepts:
    * ``dataset_path`` -- Path to the dataset with projects.
    * ``config_path`` -- Path to a yaml config. More information about the config can be found in the README file.

You can also specify:
    * ``--n-cpu`` -- Number of workers for a parallel execution. By default, it is equal to the number of CPUs.
    * ``--logs-path`` -- Path to a file where you want to save the logs. By default, the logs will be written to stderr.
"""
import json
import logging
from argparse import ArgumentParser
from functools import partial
from multiprocessing import Pool
from pathlib import Path
from typing import Dict, Optional

from benchmark.metrics_collection.config import check_config
from benchmark.metrics_collection.metrics import MetricName

from utils.file_utils import FileSystemItem, get_all_file_system_items

from yaml import dump, safe_load

METRICS_FILE = 'project_metrics.yaml'
TOTAL_FIELD_NAME = 'total'


logger = logging.getLogger(__name__)


def configure_parser(parser: ArgumentParser) -> None:
    parser.add_argument(
        'dataset_path',
        type=lambda value: Path(value),
        help='Path to a dataset with projects.',
    )

    parser.add_argument(
        'config_path',
        type=lambda value: Path(value),
        help='Path to a yaml config. More information about the config can be found in the README file.',
    )

    parser.add_argument(
        '--n-cpu',
        type=int,
        help='Number of workers for a parallel execution. By default, it is equal to the number of CPUs in the system.',
    )

    parser.add_argument(
        '--logs-path',
        type=lambda value: Path(value),
        help='Path to a file where you want to save the logs. By default, the logs will be written to stderr.',
    )


def collect_project_metrics(
    project: Path,
    metric_configs: Dict[MetricName, Dict],
) -> Optional[Dict[str, Dict[str, int]]]:
    """
    Collect all metrics specified in ``metric_configs`` from ``project``.

    All metrics will be grouped by language and also a total value will be given.

    :param project: Path to the project.
    :param metric_configs: Dictionary, where for each metric to be collect, a config is specified.
    :return: Dictionary with values for each metric. If the project does not exist, is not a directory or is empty,
    None will be returned.
    """
    if not project.exists() or not project.is_dir():
        return None

    files = get_all_file_system_items(project)
    files = [file for file in files if file.name != METRICS_FILE and file.exists()]

    if not files:
        return None

    metric_values = {
        metric.value: {language.value: value for language, value in metric.execute(files, **config).items()}
        for metric, config in metric_configs.items()
    }

    for metric, language_metrics in metric_values.items():
        metric_values[metric][TOTAL_FIELD_NAME] = sum(metric for metric in language_metrics.values())

    return metric_values


def collect_and_save_metrics(project: Path, metric_configs: Dict[MetricName, Dict]) -> None:
    """
    Collect all metrics specified in ``metric_configs`` from ``project`` and save them.

    Metrics will be saved in the project root in yaml format.

    :param project: Path to the project.
    :param metric_configs: Dictionary, where for each metric to be collect, a config is specified.
    """
    logging.info(f'Collecting {project.name} metrics.')
    metrics = collect_project_metrics(project, metric_configs)

    if metrics is None or len(metrics) == 0:
        logging.warning(f'{project.name} does not exist or is empty.')
        return

    logging.info(f'Saving {project.name} metrics.')
    save_path = project / METRICS_FILE
    with open(save_path, mode='w') as output:
        dump(metrics, output)


def main():
    parser = ArgumentParser()
    configure_parser(parser)

    args = parser.parse_args()

    logging.basicConfig(
        filename=args.logs_path,
        level=logging.INFO,
        format='%(asctime)s | %(levelname)s | %(message)s',
    )

    projects = get_all_file_system_items(args.dataset_path, item_type=FileSystemItem.SUBDIR, with_subdirs=False)

    with open(args.config_path) as file:
        config = safe_load(file)

    errors = check_config(config)
    if errors:
        # Using json.dumps to display the error list nicely
        logger.error(json.dumps(errors, indent=4))
        return

    metric_configs = {
        MetricName(metric): config if config is not None else {} for metric, config in config['metrics'].items()
    }

    with Pool(args.n_cpu) as pool:
        pool.map(partial(collect_and_save_metrics, metric_configs=metric_configs), projects)


if __name__ == '__main__':
    main()
