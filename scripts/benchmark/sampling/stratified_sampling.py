"""
This script will extract a stratified sample from a dataset where the distribution of the selected metrics is saved.

It accepts:
    * ``dataset_path`` -- Path to a dataset with projects.
    * ``config_path`` -- Path to a yaml config. More information about the config can be found in the README file.
    * ``output_path`` -- Path to a csv file with selected projects.

You can also specify:
    * ``--random-state`` -- Seed for random number generator.
    * ``--logs-path`` -- Path to a file where you want to save the logs. By default, the logs will be written to stderr.
"""
import argparse
import json
import logging
from pathlib import Path
from typing import Any, Dict, List, Optional

import numpy as np
import pandas as pd
import yaml

from benchmark.metrics_collection.collect_project_metrics import METRICS_FILE, TOTAL_FIELD_NAME
from benchmark.sampling.config import ConfigField, SCHEMA
from utils.config_utils import check_config
from utils.file_utils import FileSystemItem, get_all_file_system_items
from utils.language import Language

logger = logging.getLogger(__name__)

PROJECT_COLUMN = 'project'
BINS_DEFAULT = 'auto'


def configure_parser(parser: argparse.ArgumentParser) -> None:
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
        'output_path',
        type=lambda value: Path(value),
        help='Path to a csv file with selected projects.',
    )

    parser.add_argument('--random-state', type=int, help='Seed for random number generator.')

    parser.add_argument(
        '--logs-path',
        type=lambda value: Path(value),
        help='Path to a file where you want to save the logs. By default, the logs will be written to stderr.',
    )


def read_project_metrics(project: Path, language: Optional[Language]) -> Optional[Dict[str, int]]:
    """
    Read a metric file from ``project`` into a dictionary.

    :param project: Path to the project with the metric file.
    :param language: Language, the metrics for which must be read. If None, the total field will be used.
    :return: Dictionary with the language metrics. If the metric file are not found, None will be returned.
    """
    logger.info(f'Reading {project.name}')

    metrics_file_path = project / METRICS_FILE
    if not metrics_file_path.exists():
        logger.warning(f'File with metrics not found in {project.name}')
        return None

    with open(metrics_file_path) as file:
        metric_values = yaml.safe_load(file)

    field = TOTAL_FIELD_NAME if language is None else language.value

    return {
        metric: values[field]
        for metric, values in metric_values.items()
        if values is not None and values.get(field) is not None
    }


def read_metrics(dataset: Path, language: Optional[Language]) -> Optional[pd.DataFrame]:
    """
    Read metric files from ``dataset`` into a dataframe.

    :param dataset: Path to the dataset with projects.
    :param language: Language, the metrics for which must be read. if None, the total field will be used.
    :return: Dataframe with the language metrics. If no metrics are found, None will be returned.
    """
    if not dataset.is_dir() or not dataset.exists():
        logger.error('The dataset path is not a directory or the dataset does not exist.')
        return None

    projects = get_all_file_system_items(dataset, item_type=FileSystemItem.SUBDIR, with_subdirs=False)

    metrics_by_project = {project.name: read_project_metrics(project, language) for project in projects}
    metrics = pd.DataFrame.from_dict(metrics_by_project, orient='index')
    if metrics.isna().values.all():
        return None

    metrics.index.name = PROJECT_COLUMN
    metrics.reset_index(inplace=True)

    return metrics


def convert_to_intervals(data: pd.DataFrame, column_bins: Dict[str, Any]) -> pd.DataFrame:
    """
    Convert continuous values in the columns of ``dataframe`` into intervals, using bins.

    :param data: Dataframe whose columns are to be converted.
    :param column_bins:
        Dictionary, where bins are specified for each column to be converted.
        The possible values of the bins are given here:
        https://numpy.org/doc/stable/reference/generated/numpy.histogram_bin_edges.html.
        By default, "bins" equals to "auto".
    :return: Dataframe with the converted columns.
    """
    converted_data = data.dropna(subset=column_bins.keys()).copy()
    for column, bins in column_bins.items():
        converted_data[column] = pd.cut(
            converted_data[column],
            np.histogram_bin_edges(converted_data[column].values, bins if bins is not None else BINS_DEFAULT),
            include_lowest=True,
        )
    return converted_data


def get_stratified_sample(
    data: pd.DataFrame,
    strata: List[str],
    size: int,
    random_state: Optional[int] = None,
) -> pd.DataFrame:
    """
    Get a stratified sample from ``data`` using columns specified in ``strata`` in order to keep the distribution.

    To do this, group ``data`` by columns specified in ``strata`` and take from each group a proportional number of
    samples.

    If the ``size`` value is greater than the number of elements in the group, all elements in the group will be
    selected.

    If the ``strata`` list is empty, the function behaves like ``pd.DataFrame.sample``.

    :param data: Dataframe from which the sample must be extracted.
    :param strata: List of columns whose distribution should be saved.
    :param size: Sample size.
    :param random_state: Seed for random number generator.
    :return: Sample dataframe.
    """
    if not strata:
        logger.warning('Columns for stratification are not specified. Stratification will not be performed.')
        return data.sample(min(size, len(data)), random_state=random_state).reset_index(drop=True)

    return (
        data.groupby(strata, observed=True)
        .apply(
            lambda group: group.sample(
                min(round(size / len(data) * len(group)), len(group)),
                random_state=random_state,
            ),
        )
        .reset_index(drop=True)
    )


def main() -> None:
    parser = argparse.ArgumentParser()
    configure_parser(parser)

    args = parser.parse_args()

    logging.basicConfig(
        filename=args.logs_path,
        level=logging.INFO,
        format='%(asctime)s | %(levelname)s | %(message)s',
    )

    with open(args.config_path) as file:
        config = yaml.safe_load(file)

    errors = check_config(config, SCHEMA)
    if errors:
        # Using json.dumps to display the error list nicely
        logger.error(json.dumps(errors, indent=4))
        return

    logger.info('Reading metrics.')
    metrics = read_metrics(args.dataset_path, Language.from_value(config[ConfigField.LANGUAGE.value]))
    if metrics is None:
        logger.error('Metrics not found.')
        return

    logger.info('Converting metrics.')
    converted_metrics = convert_to_intervals(metrics, config[ConfigField.STRATA.value])

    logger.info('Sampling.')
    sample = get_stratified_sample(
        converted_metrics,
        config[ConfigField.STRATA.value].keys(),
        config[ConfigField.SAMPLE_SIZE.value],
        args.random_state,
    )

    logger.info('Saving sample.')
    sample[PROJECT_COLUMN].to_csv(args.output_path, index=False)


if __name__ == '__main__':
    main()
