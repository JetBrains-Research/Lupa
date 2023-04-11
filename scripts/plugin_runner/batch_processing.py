"""
This script allows to run IntelliJ IDEA plugin using batch processing.

It accepts:
    * path to the dataset with the projects;
    * path to the output directory;
    * path to a yaml config. More information about the config can be found in the README file;
    * data to analyse using the plugin.

It also optionally accepts:
    * flag that specifies whether the database should be used to analyse only updated repositories (default is false);
    * index of batch to start from (default is 0);
    * plugin task name: cli or pythin-cli (default is cli);
    * additional plugin arguments.
"""
import argparse
import json
import logging
import os
import time
from pathlib import Path
from typing import Callable, Dict, List

import yaml

from plugin_runner.batcher import BatcherName
from plugin_runner.batching_config import BATCHING_SCHEMA, ConfigField
from benchmark.sampling.stratified_sampling import read_project_metrics
from data_collection.db_connect import DatabaseConn
from data_collection.repositories_table import RepositoriesTable

from plugin_runner.additional_arguments import AdditionalArguments
from plugin_runner.analyzers import AVAILABLE_ANALYZERS, Analyzer
from plugin_runner.merge_data import merge
from utils.config_utils import check_config
from utils.file_utils import Extensions, clear_directory, create_directory, get_subdirectories
from utils.language import Language
from utils.run_process_utils import run_in_subprocess

logger = logging.getLogger(__name__)


def main():
    logging.basicConfig(level=logging.DEBUG)

    parser = argparse.ArgumentParser()
    configure_parser(parser)

    args = parser.parse_args()
    additional_arguments = AdditionalArguments.parse_additional_arguments(args.kwargs)

    with open(args.batching_config) as file:
        batching_config = yaml.safe_load(file)

    errors = check_config(batching_config, BATCHING_SCHEMA)
    if errors:
        # Using json.dumps to display the error list nicely
        logger.error(json.dumps(errors, indent=4))
        return

    projects_paths = [
        Path(project)
        for project in get_subdirectories(args.input)
        if filter_repositories_predicate(args.use_db)(project)
    ]

    batches = split_into_batches(projects_paths, batching_config)
    batch_paths = create_batches(batches, args.output / 'batches')

    analyzer = Analyzer.get_analyzer_by_name(AVAILABLE_ANALYZERS, args.data)

    logs_dir = args.output / 'logs'
    create_directory(logs_dir)

    batch_output_paths = []
    for index, batch_path in enumerate(batch_paths[args.start_from:], start=args.start_from):
        batch_output_path = args.output / 'output' / f'batch_{index}'
        batch_output_paths.append(batch_output_path)
        create_directory(batch_output_path)

        command = get_analyzer_command(args.task_name, analyzer, batch_path, batch_output_path, additional_arguments)
        log_file_path = logs_dir / f'log_batch_{index}.{Extensions.TXT}'

        with open(log_file_path, 'w+') as log_file:
            start_time = time.time()
            run_in_subprocess(command, stdout_file=log_file, stderr_file=log_file)
            end_time = time.time()

        logger.info(f'Finished batch {index} processing in {end_time - start_time}s')

    merge(batch_output_paths, args.output, args.data)


def get_analyzer_command(
    task_name: str,
    analyzer: Analyzer,
    batch_path: Path,
    batch_output_path: Path,
    additional_arguments: List[str],
) -> List[str]:
    return [
        './gradlew',
        f':lupa-runner:{task_name}',
        f'-Prunner={analyzer.name}-analysis',
        f'-Pinput={batch_path}',
        f'-Poutput={batch_output_path}',
        *additional_arguments,
    ]


def split_into_batches(project_paths: List[Path], batching_config: Dict) -> List[List[Path]]:
    """
    Split projects into batches using a batcher specified in a batching config.

    :param project_paths: The project paths.
    :param batching_config: The batching config. Must be valid: use ``BATCHING_SCHEMA`` from ``batching_config.py`` for
    validation.
    :return: List of batches. Each batch is a list of project paths included in that batch.
    """
    batcher_config = batching_config[ConfigField.BATCHER_CONFIG.value]
    batcher = BatcherName(batcher_config.pop(ConfigField.NAME.value))

    metric_name = batching_config.get(ConfigField.METRIC.value)

    projects_for_batching = {project: {} for project in project_paths}
    batch_constraints = {}

    # Since batching_config is valid, if the 'metric' field is not None,
    # then the 'language' and 'batch_constraints' fields are not None either.
    if metric_name is not None:
        projects = {
            project: read_project_metrics(project, Language(batching_config[ConfigField.LANGUAGE.value]))
            for project in project_paths
        }

        projects_for_batching = {project: metrics for project, metrics in projects.items() if metrics is not None}

        if len(projects) != len(projects_for_batching):
            logger.warning(
                f'{len(projects) - len(projects_for_batching)} projects without the metric file will be skipped.',
            )

        batch_constraints = {metric_name: batching_config[ConfigField.BATCH_CONSTRAINTS.value][metric_name]}

    return batcher.execute(
        projects_for_batching,
        batch_constraints,
        batching_config.get(ConfigField.IGNORE_OVERSIZED_PROJECTS.value, True),
        **batcher_config,
    )


def create_batches(batches: List[List[Path]], output: Path) -> List[Path]:
    """
    For each batch, creates a folder containing links to the projects included in that batch.

    :param batches: List of batches. Each batch is a list of project paths included in that batch.
    :param output: Directory where batches will be created.
    :return: List of batch paths.
    """
    clear_directory(output)

    batch_paths = []
    for index, batch in enumerate(batches):
        batch_directory_path = output / f'batch_{index}'
        batch_paths.append(batch_directory_path)
        create_directory(batch_directory_path)

        for project in batch:
            project_sym_link = batch_directory_path / project.name
            if not os.path.exists(project_sym_link):
                os.symlink(project, project_sym_link)

        logger.info(f'Create batch №{index}')

    return batch_paths


def filter_repositories_predicate(use_db: bool) -> Callable[[str], bool]:
    if not use_db:
        return lambda _: True

    db_conn = DatabaseConn()
    table = RepositoriesTable(db_conn)
    repositories_to_analyse = table.select_repositories_to_analyse()
    repositories_to_analyse_names = [f'{repo[0]}#{repo[1]}' for repo in repositories_to_analyse]
    return lambda path: os.path.basename(path) in repositories_to_analyse_names


def configure_parser(parser: argparse.ArgumentParser) -> None:
    parser.add_argument(
        'input',
        type=lambda value: Path(value).absolute(),
        help='Path to the dataset with the projects.',
    )

    parser.add_argument(
        'output',
        type=lambda value: Path(value).absolute(),
        help='Path to the output directory.',
    )

    parser.add_argument(
        'batching_config',
        type=lambda value: Path(value).absolute(),
        help='Path to a yaml config. More information about the config can be found in the README file.',
    )

    parser.add_argument('--use-db', help='Use database to analyse only updated repositories.', action='store_true')

    parser.add_argument(
        '--start-from',
        help='Index of batch to start processing from (not for using with --use-db flag).',
        nargs='?',
        default=0,
        type=int,
    )

    configure_analysis_arguments(parser)


def configure_analysis_arguments(parser: argparse.ArgumentParser) -> None:
    analyzers_names = Analyzer.get_analyzers_names(AVAILABLE_ANALYZERS)
    parser.add_argument('data', help=f"Data to analyse: {', '.join(analyzers_names)}.", choices=analyzers_names)

    parser.add_argument(
        '--task-name',
        help='The plugin task name.',
        nargs='?',
        default='cli',
        type=str,
        choices=['cli', 'python-cli'],
    )

    parser.add_argument(
        '--kwargs',
        help='Map of additional plugin arguments. Usage example: "--kwargs venv=path/to/venv".',
        nargs='*',
        action=AdditionalArguments,
    )


if __name__ == '__main__':
    main()
