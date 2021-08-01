"""
This script allows to run IntelliJ IDEA plugin using batch processing.
It accepts
    * path to input directory containing kotlin projects
    * path to the output directory, where all methods extracted from input projects will be saved
    * data to analyse using the plugin (clones or ranges)
    * batch size (default is 300)
    * index of batch to start from
"""
import sys
import argparse
import logging
import os
import subprocess
from pathlib import Path
from shutil import copytree
from typing import List

from plugin_runner.merge_data import merge_clones, merge_ranges

module_path = os.path.abspath(os.path.join(os.path.realpath(__file__), os.pardir, os.pardir))
if module_path not in sys.path:
    sys.path.append(module_path)

from utils import get_subdirectories, create_directory, Extensions

PROJECT_DIR = Path(__file__).parent.parent.parent


def main():
    logging.basicConfig(level=logging.DEBUG)

    args = parse_args()
    batch_paths = split(args.input, args.output, args.batch_size)

    batch_output_paths = []
    logs_path = os.path.join(args.output, "logs")
    create_directory(logs_path)
    for batch_path in batch_paths[args.start_from:]:
        index = batch_path.split("_")[-1]
        batch_output_path = os.path.join(args.output, f"output/batch_{index}")
        batch_output_paths.append(batch_output_path)
        create_directory(batch_output_path)
        with open(os.path.join(PROJECT_DIR, os.path.join(logs_path, f"log_batch_{index}.{Extensions.TXT}")), "w+") as fout:
            process = subprocess.Popen(["./gradlew", ":kotlin-analysis-plugin:cli",
                                        f"-Prunner=kotlin-{args.data}-analysis",
                                        f"-Pinput={batch_path}",
                                        f"-Poutput={batch_output_path}"],
                                       stdout=fout, stderr=fout, cwd=PROJECT_DIR)
        process.wait()
        logging.info(f"Finished batch {index} processing")

    merge(batch_output_paths, args.output, args.data)


def split(input: str, output: str, batch_size: int) -> List[str]:
    dirs = get_subdirectories(input)
    batches = [dirs[i:i + batch_size] for i in range(0, len(dirs), batch_size)]
    batch_paths = []
    for index, batch in enumerate(batches):
        batch_directory_path = os.path.join(output, f"batches/batch_{index}")
        batch_paths.append(batch_directory_path)
        create_directory(batch_directory_path)
        for directory in batch:
            directory_name = os.path.split(directory)[-1]
            copytree(directory, os.path.join(batch_directory_path, directory_name))
        logging.info(f"Copied {index} batch")
    return batch_paths


def merge(batch_output_paths: List[str], output_dir: str, data: str):
    if data == "clones":
        merge_clones(batch_output_paths, output_dir)
    elif data == "ranges":
        merge_ranges(batch_output_paths, output_dir)
    elif data == "dependencies":
        merge_ranges(batch_output_paths, output_dir)
    else:
        logging.error("Can't merge results")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("input", help="Path to the dataset containing kotlin projects")
    parser.add_argument("output", help="Path to the output directory")
    parser.add_argument("data", help="Data to analyse: clones or ranges", choices=["dependencies", "clones", "ranges"])
    parser.add_argument("--batch-size", help="Batch size for the plugin", nargs='?', default=300,
                        type=int)
    parser.add_argument("--start-from", help="Index of batch to start processing from", nargs='?', default=0, type=int)
    return parser.parse_args()


if __name__ == "__main__":
    main()
