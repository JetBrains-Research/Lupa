# TODO: docs

import argparse
import json
import logging
import time
from pathlib import Path

import yaml
from typing import List

from benchmark.batching.config import BATCHING_SCHEMA
from plugin_runner.additional_arguments import AdditionalArguments
from plugin_runner.analyzers import AVAILABLE_ANALYZERS, Analyzer
from plugin_runner.batch_processing import (
    configure_analysis_arguments,
    run_analyzer_on_batch,
    split_into_batches,
    split_into_directories,
)
from utils.config_utils import check_config
from utils.file_utils import Extensions, FileSystemItem, create_directory, get_all_file_system_items

logger = logging.getLogger(__name__)


def configure_parser(parser: argparse.ArgumentParser) -> None:
    parser.add_argument(
        'dataset',
        type=lambda value: Path(value),
        help='Path to a dataset with projects.',
    )

    parser.add_argument(
        'output',
        type=lambda value: Path(value),
        help='Path to the output directory.',
    )

    parser.add_argument(
        'batching_config',
        type=lambda value: Path(value),
        help='Path to a yaml config. More information about the config can be found in the README file.',
    )

    parser.add_argument(
        '--warmup-runs',
        type=int,
        help='Number of warm-up starts.',
        default=2,
    )

    parser.add_argument(
        '--benchmark-runs',
        type=int,
        help='Number of benchmark runs.',
        default=3,
    )

    parser.add_argument(
        '--start-from',
        type=int,
        help='Index of batch to start processing from.',
        default=0,
    )

    parser.add_argument(
        '--logs-path',
        type=lambda value: Path(value),
        help='Path to a file where you want to save the logs. By default, the logs will be written to stderr.',
    )

    configure_analysis_arguments(parser)


def run_analysis(
    task_name: str,
    analyser: Analyzer,
    batch_path: Path,
    output_dir: Path,
    additional_arguments: List[str],
) -> float:
    batch_output_path = output_dir / 'data'
    create_directory(batch_output_path)

    logs_dir = output_dir / 'logs'
    create_directory(logs_dir)

    log_file_path = logs_dir / f'log_batch.{Extensions.TXT}'

    start_time = time.time()
    run_analyzer_on_batch(
        task_name,
        analyser,
        batch_path,
        batch_output_path,
        log_file_path,
        additional_arguments,
    )
    end_time = time.time()

    return end_time - start_time


def main() -> None:
    parser = argparse.ArgumentParser()
    configure_parser(parser)

    args = parser.parse_args()
    additional_arguments = AdditionalArguments.parse_additional_arguments(args.kwargs)

    logging.basicConfig(
        filename=args.logs_path,
        level=logging.INFO,
        format='%(asctime)s | %(levelname)s | %(message)s',
    )

    with open(args.batching_config) as file:
        batching_config = yaml.safe_load(file)

    errors = check_config(batching_config, BATCHING_SCHEMA)
    if errors:
        # Using json.dumps to display the error list nicely
        logger.error(json.dumps(errors, indent=4))
        return

    projects_paths = get_all_file_system_items(args.dataset, item_type=FileSystemItem.SUBDIR, with_subdirs=False)

    batches = split_into_batches(projects_paths, batching_config)
    batch_paths = split_into_directories(batches, args.output / 'batches')

    analyser = Analyzer.get_analyzer_by_name(AVAILABLE_ANALYZERS, args.data)

    for batch_path in batch_paths:
        warmup_run_data = []
        benchmark_run_data = []

        for _ in range(args.warmup_runs):
            warmup_run_time = run_analysis(
                args.task_name,
                analyser,
                batch_path,
                args.output / 'output',
                additional_arguments,
            )

            warmup_run_data.append(warmup_run_time)

        for _ in range(args.benchmark_runs):
            benchmark_run_time = run_analysis(
                args.task_name,
                analyser,
                batch_path,
                args.output / 'output',
                additional_arguments,
            )

            benchmark_run_data.append(benchmark_run_time)


if __name__ == '__main__':
    main()
