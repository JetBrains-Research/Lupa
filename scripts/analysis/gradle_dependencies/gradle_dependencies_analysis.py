import argparse
import os
import sys
from collections import Counter
from typing import Tuple, Dict, List

import pandas as pd

from analysis.gradle_dependencies.column_names_utils import GradleDependenciesStatsColumn, \
    GradleDependenciesStatsExtensionColumn
from utils import Extensions, create_directory
from visualization.diagram import show_bar_plot

GradleDependenciesStats = Dict[str, Tuple[int, str, Dict[str, int]]]

GradleDependenciesConfigStats = Dict[str, int]


def dependency_configs_to_list(dependency_configs: Dict[str, int]) -> List[int]:
    row = []
    for column in GradleDependenciesStatsExtensionColumn:
        if column.value in dependency_configs:
            row.append(dependency_configs[column.value])
        else:
            row.append(0)
    return row


def save_dependencies_stats_with_to_csv(path_to_dir: str, filename: str, stats: GradleDependenciesFullStatsWithUrl):
    csv_file_path = os.path.join(path_to_dir, filename)
    with open(csv_file_path, 'w+') as csv_file:
        csv_file.write(stat_to_row([c.value for c in GradleDependenciesStatsColumn] + ["url"] +
                                   [c.value for c in GradleDependenciesStatsExtensionColumn]))
        for name, info in stats.items():
            csv_file.write(stat_to_row([name] + [info[0]] + [info[1]] + dependency_configs_to_list(info[2])))


def get_full_name_stats(df: pd.DataFrame) -> GradleDependenciesFullStatsWithUrl:
    full_names_to_urls = {full_name: url for full_name, url in pd.read_csv(
        "data/gradle_dependencies_full_name_url_data.csv").values}
    full_names = {}
    for group_id, artifact_id, config in df[["group", "name", "config"]].values:
        full_name = f"{group_id}:{artifact_id}"
        if full_name not in full_names:
            url = full_names_to_urls[full_name] if full_name in full_names_to_urls else None
            full_names[full_name] = [0, url, {config.value: 0 for config in GradleDependenciesStatsExtensionColumn}]
        full_names[full_name][0] += 1
        full_names[full_name][2][config] += 1
    return full_names


def get_unique_full_name_stats(df: pd.DataFrame) -> GradleDependenciesFullStatsWithUrl:
    full_names_to_urls = {full_name: url for full_name, url in pd.read_csv(
        "data/gradle_dependencies_full_name_url_data.csv").values}
    full_names = {}
    projects_by_full_names = {}
    for project_name, group_id, artifact_id, config in df[["project_name", "group", "name", "config"]].values:
        full_name = f"{group_id}:{artifact_id}"
        if full_name not in full_names:
            projects_by_full_names[full_name] = set()
            url = full_names_to_urls[full_name] if full_name in full_names_to_urls else None
            full_names[full_name] = [0, url, {config.value: 0 for config in GradleDependenciesStatsExtensionColumn}]
        if project_name not in projects_by_full_names[full_name]:
            projects_by_full_names[full_name].add(project_name)
            full_names[full_name][0] += 1
            full_names[full_name][2][config] += 1
    return full_names


def get_groups_stats(df: pd.DataFrame) -> GradleDependenciesFullStats:
    groups = {}
    for group_id, config in df[["group_id", "config"]].values:
        if group_id not in groups:
            groups[group_id] = [0, {config.value: 0 for config in GradleDependenciesStatsExtensionColumn}]
        groups[group_id][0] += 1
        groups[group_id][1][config] += 1
    return groups


def get_config_stats(df: pd.DataFrame) -> Dict[str, int]:
    groups = [data[4] for data in df.values]
    return Counter(groups)


def analyze_unique_full_names_stats(path_to_result_dir: str, df: pd.DataFrame):
    full_name_stats = get_unique_full_name_stats(df)
    save_stats_with_to_csv(path_to_result_dir, f"unique_full_name_stats.{Extensions.CSV}", full_name_stats)


def analyze_full_names_stats(path_to_result_dir: str, df: pd.DataFrame):
    full_name_stats = get_full_name_stats(df)
    save_stats_with_to_csv(path_to_result_dir, f"full_name_stats.{Extensions.CSV}", full_name_stats)


def get_gradle_dependencies(path_to_dependencies: str, path_to_tagged_projects: str, tags: List[str]) -> pd.DataFrame:
    dependencies = pd.read_csv(path_to_dependencies)
    tagged_projects = {project: tag for project, tag in pd.read_csv(path_to_tagged_projects).values}
    return dependencies[dependencies['project_name'].apply(lambda project_name: tagged_projects[project_name] in tags)]


def analyze(path_to_dependencies: str, path_to_result_dir: str, path_to_tagged_projects: str, tags: List[str]):
    create_directory(path_to_result_dir)

    df = get_gradle_dependencies(path_to_dependencies, path_to_tagged_projects, tags)
    print(f"Got {df.size} gradle dependencies")
    analyze_full_names_stats(path_to_result_dir, df)
    analyze_groups_stats(path_to_result_dir, df)
    analyze_config_stats(path_to_result_dir, df)
    analyze_unique_full_names_stats(path_to_result_dir, df)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('--input', type=str, help='path to csv file with gradle dependencies', required=True)
    parser.add_argument('--output', type=str, help='path to output dir with result', required=True)
    parser.add_argument('--ignore', type=str, default=None, help='path to csv file with dependencies to ignore')
    parser.add_argument('--tagged_projects', type=str, default=None, help='path to csv file with tagged projects')
    parser.add_argument('--tags', help='path to csv file with tagged projects', nargs='+', default=['android', 'other'])

    args = parser.parse_args(sys.argv[1:])

    analyze(args.input, args.output, args.tagged_projects, args.tags)
