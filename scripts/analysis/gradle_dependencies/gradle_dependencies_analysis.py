import argparse
import os
import sys
from collections import Counter
from typing import Tuple, Dict, List, Any

import pandas as pd

from analysis.gradle_dependencies.column_names_utils import GradleDependenciesStatsColumn, \
    GradleDependencyConfigStatsColumn, GradleDependenciesStatsExtensionColumn
from analysis.gradle_dependencies.package_search import get_url_by_full_name
from utils import Extensions, create_directory
from visualization.diagram import show_bar_plot

GradleDependenciesFullStats = Dict[str, Tuple[int, Dict[str, int]]]

GradleDependenciesFullStatsWithUrl = Dict[str, Tuple[int, str, Dict[str, int]]]

GradleDependenciesShortStats = Dict[str, int]


def stat_to_row(stats: List[Any]) -> str:
    return ",".join(list(map(str, stats))) + "\n"


def dependency_configs_to_list(dependency_configs: Dict[str, int]) -> List[int]:
    row = []
    for column in GradleDependenciesStatsExtensionColumn:
        if column.value in dependency_configs:
            row.append(dependency_configs[column.value])
        else:
            row.append(0)
    return row


def save_short_stats_to_csv(path_to_dir: str, filename: str, stats: GradleDependenciesShortStats):
    csv_file_path = os.path.join(path_to_dir, filename)
    with open(csv_file_path, 'w+') as csv_file:
        csv_file.write(stat_to_row([c.value for c in GradleDependencyConfigStatsColumn]))
        for dependency_name, count in stats.items():
            csv_file.write(stat_to_row([dependency_name] + [count]))


def save_full_stats_to_csv(path_to_dir: str, filename: str, stats: GradleDependenciesFullStats):
    csv_file_path = os.path.join(path_to_dir, filename)
    with open(csv_file_path, 'w+') as csv_file:
        csv_file.write(stat_to_row([c.value for c in GradleDependenciesStatsColumn] +
                                   [c.value for c in GradleDependenciesStatsExtensionColumn]))
        for name, info in stats.items():
            csv_file.write(stat_to_row([name] + [info[0]] + dependency_configs_to_list(info[1])))


def save_full_stats_with_url_to_csv(path_to_dir: str, filename: str, stats: GradleDependenciesFullStatsWithUrl):
    csv_file_path = os.path.join(path_to_dir, filename)
    with open(csv_file_path, 'w+') as csv_file:
        csv_file.write(stat_to_row([c.value for c in GradleDependenciesStatsColumn] + ["url"] +
                                   [c.value for c in GradleDependenciesStatsExtensionColumn]))
        for name, info in stats.items():
            csv_file.write(stat_to_row([name] + [info[0]] + [info[1]] + dependency_configs_to_list(info[2])))


def get_full_name_stats(df: pd.DataFrame) -> GradleDependenciesFullStatsWithUrl:
    full_names_to_urls = {full_name: url for full_name, url in pd.read_csv("gradle_dependencies_full_name_url_data.csv").values}
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
    full_names_to_urls = {full_name: url for full_name, url in pd.read_csv("gradle_dependencies_full_name_url_data.csv").values}
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
    for group_id, config in df[["group", "config"]].values:
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
    save_full_stats_with_url_to_csv(path_to_result_dir, f"unique_full_name_stats.{Extensions.CSV}", full_name_stats)


def analyze_full_names_stats(path_to_result_dir: str, df: pd.DataFrame):
    full_name_stats = get_full_name_stats(df)
    save_full_stats_with_url_to_csv(path_to_result_dir, f"full_name_stats.{Extensions.CSV}", full_name_stats)


def analyze_groups_stats(path_to_result_dir: str, df: pd.DataFrame):
    groups_stats = get_groups_stats(df)
    save_full_stats_to_csv(path_to_result_dir, f"groups_stats.{Extensions.CSV}", groups_stats)


def analyze_config_stats(path_to_result_dir: str, df: pd.DataFrame):
    config_stats = get_config_stats(df)
    save_short_stats_to_csv(path_to_result_dir, f"config_stats.{Extensions.CSV}", config_stats)
    show_bar_plot(config_stats, "dependency configuration", "count",
                  "Gradle dependencies configuration occurrence statistics")


def get_gradle_dependencies(path_to_dependencies: str, path_to_tagged_projects: str, tags: List[str]) -> pd.DataFrame:
    dependencies = pd.read_csv(path_to_dependencies)
    tagged_projects = {project: tag for project, tag in pd.read_csv(path_to_tagged_projects).values}
    return dependencies[dependencies['project_name'].apply(lambda project_name: tagged_projects[project_name] in tags)]


def process_gradle_dependencies(path_to_dependencies) -> pd.DataFrame:
    dependencies = pd.read_csv(path_to_dependencies)

    with open("gradle_dependencies_data_prep.csv", "w+") as f:
        for i, value in enumerate(dependencies.values):
            project_name, module_id, group_id, artifact_id, config = value
            if group_id == 'org.jetbrains.kotlin' and not artifact_id.startswith('kotlin'):
                print(f"error: {artifact_id}")
                artifact_id = 'kotlin-' + artifact_id
            f.write(",".join(map(str, [project_name, module_id, group_id, artifact_id, config])) + "\n")

def analyze(path_to_dependencies: str, path_to_result_dir: str,
            path_to_tagged_projects: str, tags: List[str]):
    create_directory(path_to_result_dir)

    df = get_gradle_dependencies(path_to_dependencies, path_to_tagged_projects, tags)
    print(df.size)
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
