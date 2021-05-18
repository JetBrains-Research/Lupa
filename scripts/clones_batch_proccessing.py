import argparse
import logging
import os
import subprocess
import pandas as pd
from pathlib import Path
from shutil import copytree
from typing import List
from utils import get_subdirectories, create_directory

PROJECT_DIR = Path(__file__).parent.parent
METHOD_DATA = "method_data.txt"
PROJECT_INDEX = "project_index.csv"
METHOD_INDEX = "method_index.csv"


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
        with open(os.path.join(PROJECT_DIR, os.path.join(logs_path, f"log_batch_{index}.txt")), "w+") as fout:
            process = subprocess.Popen(["./gradlew", ":kotlin-analysis-plugin:cli", f"-Pinput={batch_path}",
                                        f"-Poutput={batch_output_path}"], stdout=fout, stderr=fout, cwd=PROJECT_DIR)
        process.wait()
        logging.info(f"Finished batch {index} processing")

    merge(batch_output_paths, args.output)


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


def merge(batch_output_paths: List[str], output_dir: str):
    last_project_index = -1
    last_method_index = -1
    for batch_output_path in batch_output_paths:
        project_df = move_indexes_dataframe(batch_output_path, output_dir, PROJECT_INDEX, last_project_index + 1)
        method_df = move_indexes_dataframe(batch_output_path, output_dir, METHOD_INDEX, last_project_index + 1,
                                           last_method_index + 1)
        move_indexes_file(batch_output_path, output_dir, METHOD_DATA, last_project_index + 1, last_method_index + 1)

        last_project_index = project_df[0].max()
        last_method_index = method_df[1].max()


def move_indexes_dataframe(batch_output_path: str, output_path: str, filename: str, project_offset: int,
                           method_offset: int = None, sep: str = '\t') -> pd.DataFrame:
    dataframe = pd.read_csv(os.path.join(batch_output_path, filename), header=None, sep='\t')
    dataframe[0] = dataframe[0].apply(lambda x: x + project_offset)
    if method_offset:
        dataframe[1] = dataframe[1].apply(lambda x: x + method_offset)
    with open(os.path.join(output_path, filename), "a") as fout:
        dataframe.to_csv(fout, index=False, header=False, sep=sep)
    return dataframe


def move_indexes_line(line: str, project_offset: int, method_offset: int, sep: str = ',') -> str:
    project_index, method_index, rest = line.split(sep, maxsplit=2)
    project_index = str(int(project_index) + project_offset)
    method_index = str(int(method_index) + method_offset)
    return sep.join([project_index, method_index, rest])


def move_indexes_file(batch_output_path: str, output_path: str, filename: str, project_offset: int,
                      method_offset: int, sep: str = ','):
    with open(os.path.join(batch_output_path, filename), 'r') as batch_output:
        lines = list(map(lambda line: move_indexes_line(line, project_offset, method_offset, sep),
                         batch_output.readlines()))
    with open(os.path.join(output_path, filename), "a") as final_output:
        final_output.writelines(lines)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("input", help="Path to the dataset containing kotlin projects")
    parser.add_argument("output", help="Path to the output directory")
    parser.add_argument("--batch_size", help="Batch size for the method extraction plugin ", nargs='?', const=500,
                        type=int)
    parser.add_argument("--start_from", help="Index of batch to start processing from", nargs='?', const=0, type=int)
    return parser.parse_args()


if __name__ == "__main__":
    main()
