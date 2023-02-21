"""
This script will allow you to benchmark different batching algorithms.

It accepts:
    * path to the dataset with the projects;
    * path to the output directory;
    * path to a yaml config. More information about the config can be found in the README file;
    * data to analyse using the plugin.

It also optionally accepts:
    * path to a csv file with a sample of projects (if not specified, the entire dataset will be used);
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
from enum import Enum
from pathlib import Path

import pandas as pd
import yaml
from typing import List

from benchmark.sampling.stratified_sampling import PROJECT_COLUMN
from plugin_runner.batching_config import BATCHING_SCHEMA
from plugin_runner.additional_arguments import AdditionalArguments
from plugin_runner.analyzers import AVAILABLE_ANALYZERS, Analyzer
from plugin_runner.batch_processing import (
    configure_analysis_arguments,
    create_batches,
    get_analyzer_command,
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
from utils.run_process_utils import run_in_subprocess

from tempfile import NamedTemporaryFile

logger = logging.getLogger(__name__)


class BenchmarkResultColumn(Enum):
    BATCH = 'batch'
    TYPE = 'type'
    TIME = 'time'
    RSS = 'rss'

    @classmethod
    def values(cls) -> List[str]:
        return [column.value for column in cls]

    @classmethod
    def metrics(cls) -> List[str]:
        return [cls.TIME.value, cls.RSS.value]


class RunType(Enum):
    WARMUP = 'warmup'
    BENCHMARK = 'benchmark'

    @classmethod
    def values(cls) -> List[str]:
        return [run_type.value for run_type in cls]


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
        '--sample',
        type=lambda value: Path(value).absolute(),
        help=(
            'Path to a csv file with a sample of projects. '
            'Must consist of one column "project" containing the project names. '
            'If not specified, the entire dataset will be used for the benchmark.'
        ),
    )

    parser.add_argument(
        '--warmup-runs',
        type=int,
        help='Number of warm-up runs.',
        default=1,
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
        '--save-every-run',
        help=(
            'If specified, the analysis data for all runs will be saved, '
            'otherwise only the data for the last run will be saved.'
        ),
        action='store_true',
    )

    parser.add_argument(
        '--logs-path',
        type=lambda value: Path(value).absolute(),
        help='Path to a file where you want to save the logs. By default, the logs will be written to stderr.',
    )

    configure_analysis_arguments(parser)


def wrap_with_benchmark(command: List[str], benchmark_output: Path) -> List[str]:
    wrapper = [
        '/usr/bin/time',
        '-f',
        f'{BenchmarkResultColumn.TIME.value},{BenchmarkResultColumn.RSS.value}\n%e,%M',
        '-o',
        str(benchmark_output),
    ]

    return wrapper + command


def run_benchmark(
    task_name: str,
    analyser: Analyzer,
    batch_path: Path,
    output_dir: Path,
    additional_arguments: List[str],
    number_of_runs: int,
    save_every_run: bool,
) -> pd.DataFrame:
    benchmark_data = []
    for i in range(number_of_runs):
        batch_output_path = output_dir / (f'run_{i + 1}' if save_every_run else '') / 'data'
        create_directory(batch_output_path)

        logs_dir = output_dir / (f'run_{i + 1}' if save_every_run else '') / 'logs'
        log_file_path = logs_dir / f'log_batch.{Extensions.TXT}'
        create_directory(logs_dir)

        with NamedTemporaryFile() as metric_file:
            command = get_analyzer_command(task_name, analyser, batch_path, batch_output_path, additional_arguments)
            command = wrap_with_benchmark(command, Path(metric_file.name))

            with open(log_file_path, 'w+') as log_file:
                run_in_subprocess(command, stdout_file=log_file, stderr_file=log_file)

            run_data = pd.read_csv(metric_file)
            logger.info(
                f'№{i + 1}. '
                f'Time: {run_data.loc[0, BenchmarkResultColumn.TIME.value]} sec, '
                f'RSS: {run_data.loc[0, BenchmarkResultColumn.RSS.value]} KB',
            )

            benchmark_data.append(run_data)

    return pd.concat(benchmark_data)


def main() -> None:
    parser = argparse.ArgumentParser()
    configure_parser(parser)

    args = parser.parse_args()
    additional_arguments = AdditionalArguments.parse_additional_arguments(args.kwargs)

    logging.basicConfig(
        filename=args.logs_path,
        level=logging.INFO,
        format='%(asctime)s | %(levelname)s | %(name)s | %(message)s',
        force=True,
    )

    with open(args.batching_config) as file:
        batching_config = yaml.safe_load(file)

    errors = check_config(batching_config, BATCHING_SCHEMA)
    if errors:
        # Using json.dumps to display the error list nicely
        logger.error(json.dumps(errors, indent=4))
        return

    projects_paths = get_all_file_system_items(args.dataset, item_type=FileSystemItem.SUBDIR, with_subdirs=False)
    if args.sample is not None:
        sample = pd.read_csv(args.sample)
        projects_paths = [
            project_path for project_path in projects_paths if project_path.name in sample[PROJECT_COLUMN].values
        ]

    batches = split_into_batches(projects_paths, batching_config)
    batch_paths = create_batches(batches, args.output / 'batches')

    benchmark_output_dir = args.output / 'benchmark'

    analyser = Analyzer.get_analyzer_by_name(AVAILABLE_ANALYZERS, args.data)

    for batch_index, batch_path in enumerate(batch_paths[args.start_from:], start=args.start_from):
        data = pd.DataFrame()

        logger.info(f'Processing batch №{batch_index}...')

        analysis_output_dir = args.output / 'analysis' / f'batch_{batch_index}'
        clear_directory(analysis_output_dir)

        logger.info('Warming up...')
        warmup_data = run_benchmark(
            args.task_name,
            analyser,
            batch_path,
            analysis_output_dir / 'warming',
            additional_arguments,
            args.warmup_runs,
            args.save_every_run,
        )

        warmup_data[BenchmarkResultColumn.BATCH.value] = batch_index
        warmup_data[BenchmarkResultColumn.TYPE.value] = RunType.WARMUP.value
        data = pd.concat([data, warmup_data])

        logger.info('Benchmarking...')
        benchmark_data = run_benchmark(
            args.task_name,
            analyser,
            batch_path,
            analysis_output_dir / 'benchmarking',
            additional_arguments,
            args.benchmark_runs,
            args.save_every_run,
        )

        benchmark_data[BenchmarkResultColumn.BATCH.value] = batch_index
        benchmark_data[BenchmarkResultColumn.TYPE.value] = RunType.BENCHMARK.value
        data = pd.concat([data, benchmark_data])

        batch_output_dir = benchmark_output_dir / f'batch_{batch_index}'
        create_directory(batch_output_dir)
        clear_directory(batch_output_dir)

        data.to_csv(batch_output_dir / f'benchmark_data.{Extensions.CSV.value}', index=False)

    merge_csv(get_subdirectories(benchmark_output_dir), f'benchmark_data.{Extensions.CSV.value}', args.output)


if __name__ == '__main__':
    main()
