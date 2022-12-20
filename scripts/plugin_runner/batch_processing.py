# TODO: fix docs and add new ones

"""
This script allows to run IntelliJ IDEA plugin using batch processing.

It accepts
    * path to input directory containing kotlin projects
    * path to the output directory, where all methods extracted from input projects will be saved
    * data to analyse using the plugin (clones or ranges)
    * batch size (default is 300)
    * index of batch to start from
"""
import argparse
import json
import logging
import os
import time
from pathlib import Path
from typing import Callable, List

import yaml

from benchmark.batching.batcher import BatcherName
from benchmark.batching.config import BATCHING_SCHEMA, ConfigField
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

ROOT = Path(__file__).parent.parent.parent
logger = logging.getLogger(__name__)


def main():
    logging.basicConfig(level=logging.DEBUG)

    args = parse_args()
    additional_arguments = AdditionalArguments.parse_additional_arguments(args.kwargs)

    with open(args.batching_config) as file:
        batching_config = yaml.safe_load(file)

    errors = check_config(batching_config, BATCHING_SCHEMA)
    if errors:
        # Using json.dumps to display the error list nicely
        logger.error(json.dumps(errors, indent=4))
        return

    batcher_config = batching_config[ConfigField.BATCHER_CONFIG.value]
    batcher = BatcherName(batcher_config.pop(ConfigField.NAME.value))

    metric_name = batching_config.get(ConfigField.METRIC.value, None)
    if metric_name is None:
        projects_for_batching = {
            Path(project): {}
            for project in get_subdirectories(args.input)
            if filter_repositories_predicate(args.use_db)(project)
        }
        batch_constraints = {}
    else:
        projects = {
            Path(project): read_project_metrics(
                Path(project),
                Language(batching_config[ConfigField.LANGUAGE.value]),
            )
            for project in get_subdirectories(args.input)
            if filter_repositories_predicate(args.use_db)(project)
        }

        projects_for_batching = {
            project: metrics
            for project, metrics in projects.items()
            if metrics is not None and metrics.get(metric_name) is not None
        }

        if len(projects) != len(projects_for_batching):
            logging.warning(
                f'{len(projects) - len(projects_for_batching)} projects '
                f'without the {metric_name} metric will be skipped.',
            )

        batch_constraints = {metric_name: batching_config[ConfigField.BATCH_CONSTRAINTS.value][metric_name]}

    batches = batcher.execute(
        projects_for_batching,
        batch_constraints,
        batching_config.get(ConfigField.IGNORE_OVERSIZED_PROJECTS.value, False),
        **batcher_config,
    )

    batch_paths = split_into_directories(batches, args.output / 'batches')

    analyser = Analyzer.get_analyzer_by_name(AVAILABLE_ANALYZERS, args.data)

    logs_dir = args.output / 'logs'
    create_directory(logs_dir)

    batch_output_paths = []
    for index, batch_path in enumerate(batch_paths[args.start_from :], start=args.start_from):
        batch_output_path = args.output / 'output' / f'batch_{index}'
        batch_output_paths.append(batch_output_path)
        create_directory(batch_output_path)

        log_file_path = logs_dir / f'log_batch_{index}.{Extensions.TXT}'

        start_time = time.time()
        run_analyzer_on_batch(
            args.task_name,
            analyser,
            batch_path,
            batch_output_path,
            log_file_path,
            additional_arguments,
        )
        end_time = time.time()

        logging.info(f'Finished batch {index} processing in {end_time - start_time}s')

    merge(batch_output_paths, args.output, args.data)


def run_analyzer_on_batch(
    task_name: str,
    analyzer: Analyzer,
    batch_path: Path,
    batch_output_path: Path,
    logs_path: Path,
    additional_arguments: List[str],
):
    command = [
        './gradlew',
        f':lupa-runner:{task_name}',
        f'-Prunner={analyzer.name}-analysis',
        f'-Pinput={batch_path}',
        f'-Poutput={batch_output_path}',
        *additional_arguments,
    ]

    with open(logs_path, 'w+') as log_file:
        run_in_subprocess(command, ROOT, stdout_file=log_file, stderr_file=log_file)


def split_into_directories(batches: List[List[Path]], output: Path) -> List[Path]:
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

        logging.info(f'Create batch №{index}/{len(batches)}')

    return batch_paths


def filter_repositories_predicate(use_db: bool) -> Callable[[str], bool]:
    if not use_db:
        return lambda _: True

    db_conn = DatabaseConn()
    table = RepositoriesTable(db_conn)
    repositories_to_analyse = table.select_repositories_to_analyse()
    repositories_to_analyse_names = [f'{repo[0]}#{repo[1]}' for repo in repositories_to_analyse]
    return lambda path: os.path.basename(path) in repositories_to_analyse_names


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()

    parser.add_argument(
        'input',
        type=lambda value: Path(value),
        help='Path to the dataset containing kotlin projects',
    )

    parser.add_argument(
        'output',
        type=lambda value: Path(value),
        help='Path to the output directory',
    )

    analyzers_names = Analyzer.get_analyzers_names(AVAILABLE_ANALYZERS)
    parser.add_argument('data', help=f"Data to analyse: {', '.join(analyzers_names)}", choices=analyzers_names)

    parser.add_argument(
        'batching_config',
        type=lambda value: Path(value),
        help='Path to a yaml config. More information about the config can be found in the README file.',
    )

    parser.add_argument('--use-db', help='Use database to analyse only updated repositories', action='store_true')

    parser.add_argument(
        '--start-from',
        help='Index of batch to start processing from (not for using with --use-db flag)',
        nargs='?',
        default=0,
        type=int,
    )

    parser.add_argument(
        '--task-name',
        help='The plugin task name',
        nargs='?',
        default='cli',
        type=str,
        choices=['cli', 'python-cli'],
    )

    parser.add_argument(
        '--kwargs',
        help='Map of additional plugin arguments. Usage example: --kwargs venv=path/to/venv',
        nargs='*',
        action=AdditionalArguments,
    )

    return parser.parse_args()


if __name__ == '__main__':
    main()
