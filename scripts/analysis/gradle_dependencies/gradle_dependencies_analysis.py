import argparse
import os
import sys
from typing import Dict, List, Any

import pandas as pd

from analysis.gradle_dependencies.column_names_utils import GradleDependenciesStatsColumn
from utils import Extensions, create_directory

GradleDependenciesStats = Dict[str, Any]


def save_stats_with_to_csv(path_to_dir: str, filename: str, stats: GradleDependenciesStats):
    csv_file_path = os.path.join(path_to_dir, filename)
    pd.DataFrame.from_dict(stats).to_csv(csv_file_path, index=False)


def get_full_name_stats(path_to_dependencies_urls: str, df: pd.DataFrame, unique=False) -> GradleDependenciesStats:
    full_names_to_urls = {full_name: url for full_name, url in pd.read_csv(path_to_dependencies_urls).values}
    full_names = {}
    projects_by_full_names = {}
    for i, value in enumerate(df.values):
        project_name, _, group_id, artifact_id, config = value
        full_name = f"{group_id}:{artifact_id}"
        if full_name not in full_names:
            projects_by_full_names[full_name] = set()
            full_names[full_name] = {c.value: 0 for c in GradleDependenciesStatsColumn}
            full_names[full_name][GradleDependenciesStatsColumn.DEPENDENCY] = full_name
            full_names[full_name][GradleDependenciesStatsColumn.URL] = full_names_to_urls[full_name] \
                if full_name in full_names_to_urls else None
        if not unique or project_name not in projects_by_full_names[full_name]:
            projects_by_full_names[full_name].add(project_name)
            full_names[full_name][GradleDependenciesStatsColumn.COUNT] += 1
            full_names[full_name][GradleDependenciesStatsColumn(config)] += 1

    stats = {c.value: [] for c in GradleDependenciesStatsColumn}
    for full_name, full_name_stats in full_names.items():
        for c in GradleDependenciesStatsColumn:
            stats[c.value].append(full_name_stats[c.value])
    return stats


def analyze_unique_full_names_stats(path_to_result_dir: str, path_to_dependencies_urls: str, df: pd.DataFrame):
    full_name_stats = get_full_name_stats(path_to_dependencies_urls, df, unique=True)
    save_stats_with_to_csv(path_to_result_dir, f"unique_full_name_stats.{Extensions.CSV}", full_name_stats)


def analyze_full_names_stats(path_to_result_dir: str, path_to_dependencies_urls: str, df: pd.DataFrame):
    full_name_stats = get_full_name_stats(path_to_dependencies_urls, df)
    save_stats_with_to_csv(path_to_result_dir, f"full_name_stats.{Extensions.CSV}", full_name_stats)


def get_gradle_dependencies(path_to_dependencies: str, path_to_tagged_projects: str, tags: List[str]) -> pd.DataFrame:
    dependencies = pd.read_csv(path_to_dependencies)
    tagged_projects = {project: tag for project, tag in pd.read_csv(path_to_tagged_projects).values}
    return dependencies[dependencies['project_name'].apply(lambda project_name: tagged_projects[project_name] in tags)]


def analyze(path_to_dependencies: str, path_to_result_dir: str, path_to_dependencies_urls: str,
            path_to_tagged_projects: str, tags: List[str]):
    create_directory(path_to_result_dir)

    df = get_gradle_dependencies(path_to_dependencies, path_to_tagged_projects, tags)
    print(f"Got {df.size} gradle dependencies")
    analyze_full_names_stats(path_to_result_dir, path_to_dependencies_urls, df)
    analyze_unique_full_names_stats(path_to_result_dir, path_to_dependencies_urls, df)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('--input', type=str, help='path to csv file with gradle dependencies', required=True)
    parser.add_argument('--output', type=str, help='path to output dir with result', required=True)
    parser.add_argument('--urls', type=str, help='path to dir with csv with dependencies full names and urls',
                        required=True)
    parser.add_argument('--ignore', type=str, default=None, help='path to csv file with dependencies to ignore')
    parser.add_argument('--tagged_projects', type=str, default=None, help='path to csv file with tagged projects')
    parser.add_argument('--tags', help='path to csv file with tagged projects', nargs='+', default=['android', 'other'])

    args = parser.parse_args(sys.argv[1:])

    analyze(args.input, args.output, args.urls, args.tagged_projects, args.tags)
