import argparse
import os
from collections import defaultdict

import pandas as pd
import sys
from typing import Dict, Any
from utils import Extensions, create_directory

from analysis.gradle_plugins.column_names_utils import GradlePluginsStatsColumn

GradlePluginsStats = Dict[str, Any]


def save_stats_with_to_csv(path_to_dir: str, filename: str, stats: GradlePluginsStats):
    csv_file_path = os.path.join(path_to_dir, filename)
    stats_df = pd.DataFrame.from_dict(stats).sort_values(by=GradlePluginsStatsColumn.COUNT.value, ascending=False)
    stats_df.to_csv(csv_file_path, index=False)


def get_plugins_stats(df: pd.DataFrame, unique=False) -> GradlePluginsStats:
    plugins = defaultdict(int)
    projects_by_plugins = defaultdict(set)
    for project_name, plugin_id in df.values:
        if not unique or project_name not in projects_by_plugins[plugin_id]:
            projects_by_plugins[plugin_id].add(project_name)
            plugins[plugin_id] += 1

    return {GradlePluginsStatsColumn.PLUGIN_ID: plugins.keys(),
            GradlePluginsStatsColumn.COUNT: plugins.values()}


def analyze_unique_plugins_stats(path_to_result_dir: str, df: pd.DataFrame):
    plugins_stats = get_plugins_stats(df, unique=True)
    save_stats_with_to_csv(path_to_result_dir, f"gradle_plugins_by_project_stats.{Extensions.CSV}",
                           plugins_stats)


def analyze_plugins_stats(path_to_result_dir: str, df: pd.DataFrame):
    plugins_stats = get_plugins_stats(df)
    save_stats_with_to_csv(path_to_result_dir, f"gradle_plugins_by_module_stats.{Extensions.CSV}", plugins_stats)


def get_gradle_plugins(path_to_plugins: str) -> pd.DataFrame:
    return pd.read_csv(path_to_plugins)


def analyze(path_to_plugins: str, path_to_result_dir: str):
    create_directory(path_to_result_dir)

    df = get_gradle_plugins(path_to_plugins)
    print(f"Got {df.size} gradle plugins")
    analyze_plugins_stats(path_to_result_dir, df)
    analyze_unique_plugins_stats(path_to_result_dir, df)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('--input', type=str, help='path to gradle properties csv file with columns:'
                                                  'project_name -- project name'
                                                  'plugin_id -- plugin id', required=True)
    parser.add_argument('--output', type=str, help='path to output dir with result', required=True)

    args = parser.parse_args(sys.argv[1:])
    analyze(args.input, args.output)
