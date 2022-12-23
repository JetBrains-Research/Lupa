# TODO: docs

import argparse
import json
import logging
import time
from pathlib import Path

import pandas as pd
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
from plugin_runner.merge_data import merge_csv
from utils.config_utils import check_config
from utils.file_utils import (
    Extensions,
    FileSystemItem,
    clear_directory,
    create_directory,
    get_all_file_system_items,
    get_subdirectories,
)

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
        '--save-data',
        help=(
            'If specified, the analysis data for all batches will be saved, '
            'otherwise only the data for the last batch will be saved.'
        ),
        action='store_true',
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

    benchmark_output_dir = args.output / 'benchmark'

    analyser = Analyzer.get_analyzer_by_name(AVAILABLE_ANALYZERS, args.data)

    data = pd.DataFrame()
    for batch_index, batch_path in enumerate(batch_paths[args.start_from :], start=args.start_from):
        logger.info(f'Processing batch №{batch_index}...')

        analyzer_output_dir = args.output / 'output' / (f'batch_{batch_index}' if args.save_data else '')
        clear_directory(analyzer_output_dir)

        warmup_time_data = []
        for i in range(args.warmup_runs):
            logger.info(f'Warmup run №{i}')
            warmup_run_time = run_analysis(
                args.task_name,
                analyser,
                batch_path,
                analyzer_output_dir,
                additional_arguments,
            )
            warmup_time_data.append(warmup_run_time)

        warmup_data = pd.DataFrame.from_dict({'batch': batch_index, 'type': 'warmup', 'time': warmup_time_data})
        data = pd.concat([data, warmup_data])

        benchmark_time_data = []
        for i in range(args.benchmark_runs):
            logger.info(f'Benchmark run №{i}')
            benchmark_run_time = run_analysis(
                args.task_name,
                analyser,
                batch_path,
                analyzer_output_dir,
                additional_arguments,
            )
            benchmark_time_data.append(benchmark_run_time)

        benchmark_data = pd.DataFrame.from_dict(
            {'batch': batch_index, 'type': 'benchmark', 'time': benchmark_time_data},
        )
        data = pd.concat([data, benchmark_data])

        batch_output_dir = benchmark_output_dir / f'batch_{batch_index}'
        clear_directory(batch_output_dir)

        data.to_csv(batch_output_dir / f'benchmark_data.{Extensions.CSV.value}', index=False)

    merge_csv(get_subdirectories(benchmark_output_dir), 'benchmark_data', args.output)


if __name__ == '__main__':
    main()
