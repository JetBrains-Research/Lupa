"""
This script will allow you to benchmark different batching algorithms.

It accepts:
    * path to the dataset with the projects;
    * path to the output directory;
    * path to a yaml config. More information about the config can be found in the README file;
    * data to analyse using the plugin.

It also optionally accepts:
    * number of warmup runs (default is 2);
    * number of benchmark runs (default is 3);
    * index of batch to start from (default is 0);
    * flag which specifies whether analysis data should be saved for every batch
      (by default, false, i.e. the data of the last batch will be saved);
    * path to the logs file (by default, the logs will be written to stderr.);
    * plugin task name: cli or pythin-cli (default is cli);
    * additional plugin arguments.
"""

import argparse
import json
import logging
import time
from pathlib import Path

import pandas as pd
import yaml
from typing import List

from plugin_runner.batching_config import BATCHING_SCHEMA
from plugin_runner.additional_arguments import AdditionalArguments
from plugin_runner.analyzers import AVAILABLE_ANALYZERS, Analyzer
from plugin_runner.batch_processing import (
    configure_analysis_arguments,
    create_batches,
    run_analyzer_on_batch,
    split_into_batches,
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
        type=lambda value: Path(value).absolute(),
        help='Path to a dataset with projects.',
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

    parser.add_argument(
        '--warmup-runs',
        type=int,
        help='Number of warm-up runs.',
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
        '--save-analysis-data',
        help=(
            'If specified, the analysis data for all batches will be saved, '
            'otherwise only the data for the last batch will be saved.'
        ),
        action='store_true',
    )

    parser.add_argument(
        '--logs-path',
        type=lambda value: Path(value).absolute(),
        help='Path to a file where you want to save the logs. By default, the logs will be written to stderr.',
    )

    configure_analysis_arguments(parser)


def run_benchmark(
    task_name: str,
    analyser: Analyzer,
    batch_path: Path,
    output_dir: Path,
    additional_arguments: List[str],
    number_of_runs: int,
) -> List[float]:
    batch_output_path = output_dir / 'data'
    create_directory(batch_output_path)

    logs_dir = output_dir / 'logs'
    create_directory(logs_dir)

    log_file_path = logs_dir / f'log_batch.{Extensions.TXT}'

    time_data = []
    for i in range(number_of_runs):
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
        time_data.append(end_time - start_time)

        logger.info(f'№{i + 1}. Time: {end_time - start_time}')

    return time_data


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
    batch_paths = create_batches(batches, args.output / 'batches')

    benchmark_output_dir = args.output / 'benchmark'

    analyser = Analyzer.get_analyzer_by_name(AVAILABLE_ANALYZERS, args.data)

    for batch_index, batch_path in enumerate(batch_paths[args.start_from :], start=args.start_from):
        data = pd.DataFrame()

        logger.info(f'Processing batch №{batch_index}...')

        analysis_output_dir = args.output / 'analysis' / (f'batch_{batch_index}' if args.save_analysis_data else '')
        clear_directory(analysis_output_dir)

        logger.info('Warming up...')
        warmup_time_data = run_benchmark(
            args.task_name,
            analyser,
            batch_path,
            analysis_output_dir,
            additional_arguments,
            args.warmup_runs,
        )

        warmup_data = pd.DataFrame.from_dict({'batch': batch_index, 'type': 'warmup', 'time': warmup_time_data})
        data = pd.concat([data, warmup_data])

        logger.info('Benchmarking...')
        benchmark_time_data = run_benchmark(
            args.task_name,
            analyser,
            batch_path,
            analysis_output_dir,
            additional_arguments,
            args.benchmark_runs,
        )

        benchmark_data = pd.DataFrame.from_dict(
            {'batch': batch_index, 'type': 'benchmark', 'time': benchmark_time_data},
        )
        data = pd.concat([data, benchmark_data])

        batch_output_dir = benchmark_output_dir / f'batch_{batch_index}'
        create_directory(batch_output_dir)
        clear_directory(batch_output_dir)

        data.to_csv(batch_output_dir / f'benchmark_data.{Extensions.CSV.value}', index=False)

    merge_csv(get_subdirectories(benchmark_output_dir), f'benchmark_data.{Extensions.CSV.value}', args.output)


if __name__ == '__main__':
    main()
