"""This class contains methods for merging analysis results from different batches into a single file."""
import logging
import os
from pathlib import Path
from typing import List

import pandas as pd

from plugin_runner.analyzers import AVAILABLE_ANALYZERS, Analyzer
from utils.file_utils import get_files_by_name

PROJECT_INDEX = 'project_index.csv'
METHOD_INDEX = 'method_index.csv'

logging.basicConfig(level=logging.DEBUG)


def merge(batch_output_paths: List[str], output_dir: str, analyzer_name: str):
    analyzer = Analyzer.get_analyzer_by_name(AVAILABLE_ANALYZERS, analyzer_name)
    if analyzer.name == 'kotlin-clones':
        merge_clones(batch_output_paths, output_dir, analyzer.output_file)
    else:
        merge_csv(batch_output_paths, analyzer.output_file, output_dir)


def merge_csv(batch_output_paths: List[str], csv_filename: str, result_dir: str):
    result_df = pd.DataFrame()

    for batch_output_path in batch_output_paths:
        try:
            logging.info(f'Searching for {csv_filename} files in {batch_output_path}...')
            file_paths = get_files_by_name(batch_output_path, csv_filename)
            for file_path in file_paths:
                logging.info(f'Merging {file_path} file...')
                batch_df = pd.read_csv(file_path, sep='\t')
                result_df = pd.concat([result_df, batch_df])
        except pd.errors.EmptyDataError:
            logging.warning(f'File {batch_output_path} is empty!')
            continue

    with open(os.path.join(result_dir, csv_filename), 'a') as fout:
        result_df.to_csv(fout, index=False, sep='\t')


def merge_clones(batch_output_paths: List[str], output_dir: str, result_file_name: str):
    last_project_index = -1
    last_method_index = -1
    for batch_output_path in batch_output_paths:
        project_df = move_indexes_dataframe(batch_output_path, output_dir, PROJECT_INDEX, last_project_index + 1)
        method_df = move_indexes_dataframe(batch_output_path, output_dir, METHOD_INDEX, last_project_index + 1,
                                           last_method_index + 1)
        move_indexes_file(batch_output_path, output_dir, result_file_name,
                          last_project_index + 1, last_method_index + 1)

        last_project_index = project_df[0].max()
        last_method_index = method_df[1].max()


def move_indexes_dataframe(batch_output_path: str, output_path: str, filename: str, project_offset: int,
                           method_offset: int = None, sep: str = '\t') -> pd.DataFrame:
    dataframe = pd.read_csv(os.path.join(batch_output_path, filename), header=None, sep='\t')
    dataframe[0] = dataframe[0].apply(lambda x: x + project_offset)
    if method_offset:
        dataframe[1] = dataframe[1].apply(lambda x: x + method_offset)
    with open(os.path.join(output_path, filename), 'a') as fout:
        dataframe.to_csv(fout, index=False, header=False, sep=sep)
    return dataframe


def move_indexes_line(line: str, project_offset: int, method_offset: int, sep: str = ',') -> str:
    project_index, method_index, rest = line.split(sep, maxsplit=2)
    project_index = str(int(project_index) + project_offset)
    method_index = str(int(method_index) + method_offset)
    return sep.join([project_index, method_index, rest])


def move_indexes_file(batch_output_path: str, output_path: str, filename: str, project_offset: int,
                      method_offset: int, sep: str = ','):
    with open(os.path.join(batch_output_path, filename)) as batch_output:
        lines = list(map(lambda line: move_indexes_line(line, project_offset, method_offset, sep),
                         batch_output.readlines()))
    with open(os.path.join(output_path, filename), 'a') as final_output:
        final_output.writelines(lines)
