"""
This script allows you to collect metrics for each project from a dataset.

It accepts:
    * dataset_path -- Path to the dataset with projects.

You can also specify:
    * metrics -- Metric names to be calculated. By default, all metrics specified in the Metric enum will be calculated.
    * n-cpu -- Number of workers for a parallel execution. By default, it is equal to the number of CPUs in the system.
"""
import logging
from argparse import ArgumentParser
from collections import defaultdict
from functools import partial
from multiprocessing import Pool
from pathlib import Path
from typing import Dict, Optional, Set

from benchmark.metrics_collection.metrics import Metric

from utils.file_utils import FileSystemItem, get_all_file_system_items
from utils.language import Language

from yaml import dump

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
        '--metrics',
        nargs='+',
        choices=Metric.values(),
        default=Metric.values(),
        help='Metric names to be collected. By default, all metrics specified in the Metric enum will be collected.',
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


def collect_project_metrics(project: Path, metrics: Set[Metric]) -> Optional[Dict[str, Dict[str, int]]]:
    """
    Collect all metrics specified in the ``metrics`` list from ``project``.

    All metrics will be grouped by language and also a total value will be given.

    :param project: Path to the project.
    :param metrics: List of metrics.
    :return: Dictionary with values for each metric. If the project does not exist or is not a directory,
    None will be returned.
    """
    if not project.exists() or not project.is_dir():
        return None

    files = get_all_file_system_items(project)
    files = [file for file in files if file.name != METRICS_FILE]

    metric_values = defaultdict(lambda: defaultdict(list))
    for file in files:
        language = Language.from_file_path(file)
        for metric in metrics:
            metric_values[metric][language].append(metric.execute(file))

    metrics = {
        metric.value: {language.value: sum(values) for language, values in language_values.items()}
        for metric, language_values in metric_values.items()
    }

    for metric, language_metrics in metrics.items():
        metrics[metric][TOTAL_FIELD_NAME] = sum(metric for metric in language_metrics.values())

    return metrics


def collect_and_save_metrics(project: Path, metrics: Set[Metric]) -> None:
    """
    Collect all metrics specified in the ``metrics`` list from ``project`` and save them.

    Metrics will be saved in the project root in yaml format.

    :param project: Path to the project.
    :param metrics: List of metrics.
    """
    logging.info(f'Collecting {project.name} metrics.')
    metrics = collect_project_metrics(project, metrics)

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

    metrics = {Metric(metric_name) for metric_name in args.metrics}
    projects = get_all_file_system_items(args.dataset_path, item_type=FileSystemItem.SUBDIR, with_subdirs=False)

    with Pool(args.n_cpu) as pool:
        pool.map(partial(collect_and_save_metrics, metrics=metrics), projects)


if __name__ == '__main__':
    main()
