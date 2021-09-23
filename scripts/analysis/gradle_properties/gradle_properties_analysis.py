import argparse
import os
from collections import defaultdict

import pandas as pd
import re
import sys
from analysis.gradle_properties.column_names_utils import GradlePropertiesKeyStatsColumn, GradlePropertiesColumn
from typing import Dict, Any
from utils import Extensions, create_directory, get_file_lines

GradlePropertiesStats = Dict[str, Any]


def save_stats_with_to_csv(path_to_dir: str, filename: str, stats: GradlePropertiesStats):
    csv_file_path = os.path.join(path_to_dir, filename)
    stats_df = pd.DataFrame.from_dict(stats).sort_values(by=GradlePropertiesKeyStatsColumn.COUNT.value, ascending=False)
    stats_df.to_csv(csv_file_path, index=False)


def get_properties_key_stats(df: pd.DataFrame) -> GradlePropertiesStats:
    properties_key_stats = defaultdict(int)
    for _project_name, property_key, _property_value in df.values:
        properties_key_stats[property_key] += 1
    return {
        GradlePropertiesKeyStatsColumn.PROPERTY_KEY: properties_key_stats.keys(),
        GradlePropertiesKeyStatsColumn.COUNT: properties_key_stats.values(),
    }


def get_properties_key_value_stats(df: pd.DataFrame) -> GradlePropertiesStats:
    properties_keys = set()
    properties_values = set()
    for _project_name, property_key, property_value in df.values:
        properties_keys.add(property_key)
        for property_sub_value in str(property_value).split(' '):
            if property_sub_value != '':
                properties_values.add(property_sub_value)

    properties_key_value_stats = {v: {k: 0 for k in properties_keys} for v in properties_values}
    for _project_name, property_key, property_value in df.values:
        for property_sub_value in str(property_value).split(' '):
            if property_sub_value != '':
                properties_key_value_stats[property_sub_value][property_key] += 1

    stats = {v: [] for v in properties_values}
    stats[GradlePropertiesKeyStatsColumn.PROPERTY_KEY] = list(properties_keys)
    stats[GradlePropertiesKeyStatsColumn.COUNT] = [
        sum(properties_key_value_stats[v][k] for v in properties_values) for k in properties_keys
    ]

    for property_key in properties_keys:
        for property_value in properties_values:
            stats[property_value].append(properties_key_value_stats[property_value][property_key])

    return stats


def analyze_properties_key_stats(df: pd.DataFrame, path_to_result_dir: str):
    properties_key_stats = get_properties_key_stats(df)
    save_stats_with_to_csv(path_to_result_dir, f"gradle_properties_key_stats.{Extensions.CSV}", properties_key_stats)


def analyze_properties_key_value_stats(df: pd.DataFrame, path_to_result_dir: str):
    properties_key_value_stats = get_properties_key_value_stats(df)
    save_stats_with_to_csv(
        path_to_result_dir,
        f"gradle_properties_key_value_stats.{Extensions.CSV}",
        properties_key_value_stats,
    )


def get_gradle_properties(path_to_properties: str, path_to_select_properties: str) -> pd.DataFrame:
    df = pd.read_csv(path_to_properties)
    if path_to_select_properties is not None:
        selected_properties = list(map(lambda p: p.rstrip(), get_file_lines(path_to_select_properties)))
        df = df[df[GradlePropertiesColumn.PROPERTY_KEY].apply(lambda x: x in selected_properties)]
    return df


def analyze(path_to_properties: str, path_to_result_dir: str, path_to_select_properties: str):
    create_directory(path_to_result_dir)

    df = get_gradle_properties(path_to_properties, path_to_select_properties)
    print(f"Got {df.size} gradle properties")
    print(f"{len(set(df[GradlePropertiesColumn.PROJECT_NAME].values))} projects has gradle.properties file")
    analyze_properties_key_stats(df, path_to_result_dir)
    analyze_properties_key_value_stats(df, path_to_result_dir)


def preprocess_gradle_properties_data(path_to_properties: str) -> str:
    lines = get_file_lines(path_to_properties)
    path_to_prep_properties = "data/prep_gradle_properties_data.csv"
    with open(path_to_prep_properties, 'w+') as f:
        for line in lines:
            # if there is no property_value put null
            if re.match("^[^,]+,[^,]+,$", line):
                line = line[:-1] + 'None\n'
            sub_lines = line.split(',')
            # if property_value contains "," replace with " "
            if len(sub_lines) > 3:
                line = ','.join(sub_lines[:2] + [' '.join(sub_lines[2:])])
            f.write(line)
    return path_to_prep_properties


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('--input', type=str, help='path to csv file with gradle dependencies csv wit columns:'
                                                  'project_name -- project name'
                                                  'property_key -- property key'
                                                  'property_value -- property value', required=True)
    parser.add_argument('--output', type=str, help='path to output dir with result', required=True)
    parser.add_argument('--select', type=str, default=None, help='path to csv file with properties keys to select')

    args = parser.parse_args(sys.argv[1:])
    prep_input = preprocess_gradle_properties_data(args.input)
    analyze(prep_input, args.output, args.select)
