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
import logging
import os
import subprocess
from pathlib import Path
from typing import List
import time

from plugin_runner.additional_arguments import AdditionalArguments
from plugin_runner.analyzers import Analyzer, AVAILABLE_ANALYZERS
from plugin_runner.merge_data import merge
from utils import get_subdirectories, create_directory, Extensions

PROJECT_DIR = Path(__file__).parent.parent.parent


def main():
    logging.basicConfig(level=logging.DEBUG)

    args = parse_args()
    additional_arguments = AdditionalArguments.parse_additional_arguments(args.kwargs)
    batch_paths = split(args.input, args.output, args.batch_size)

    batch_output_paths = []
    logs_dir = os.path.join(args.output, "logs")
    create_directory(logs_dir)
    for batch_path in batch_paths[args.start_from:]:
        start_time = time.time()
        index = batch_path.split("_")[-1]
        batch_output_path = os.path.join(args.output, f"output/batch_{index}")
        batch_output_paths.append(batch_output_path)
        create_directory(batch_output_path)
        log_file = os.path.join(PROJECT_DIR, os.path.join(logs_dir, f"log_batch_{index}.{Extensions.TXT}"))
        with open(log_file, "w+") as fout:
            command = [
                "./gradlew",
                f":kotlin-analysis-plugin:{args.task_name}",
                f"-Prunner={args.data}-analysis",
                f"-Pinput={batch_path}",
                f"-Poutput={batch_output_path}",
            ]
            command.extend(additional_arguments)
            process = subprocess.Popen(command, stdout=fout, stderr=fout, cwd=PROJECT_DIR)
        process.wait()
        end_time = time.time()
        process.terminate()
        logging.info(f"Finished batch {index} processing in {end_time - start_time}s")

    merge(batch_output_paths, args.output, args.data)


def split(input: str, output: str, batch_size: int) -> List[str]:
    dirs = get_subdirectories(input)
    batches = [dirs[i: i + batch_size] for i in range(0, len(dirs), batch_size)]
    batch_paths = []
    for index, batch in enumerate(batches):
        batch_directory_path = os.path.join(output, f"batches/batch_{index}")
        batch_paths.append(batch_directory_path)
        create_directory(batch_directory_path)
        for directory in batch:
            directory_name = os.path.split(directory)[-1]
            directory_sym_link = os.path.join(batch_directory_path, directory_name)
            if not os.path.exists(directory_sym_link):
                os.symlink(directory, directory_sym_link)
        logging.info(f"Create {index} batch")
    return batch_paths


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("input", help="Path to the dataset containing kotlin projects")
    parser.add_argument("output", help="Path to the output directory")
    analyzers_names = Analyzer.get_analyzers_names(AVAILABLE_ANALYZERS)
    parser.add_argument("data", help=f"Data to analyse: {', '.join(analyzers_names)}", choices=analyzers_names)
    parser.add_argument("--batch-size", help="Batch size for the plugin", nargs='?', default=300, type=int)
    parser.add_argument("--start-from", help="Index of batch to start processing from", nargs='?', default=0, type=int)
    parser.add_argument(
        "--task-name",
        help="The plugin task name",
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


if __name__ == "__main__":
    main()
