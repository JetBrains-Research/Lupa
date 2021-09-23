import argparse
import os
import sys
from collections import defaultdict
from typing import Dict, List, Any

import pandas as pd

from analysis.gradle_dependencies.column_names_utils import (
    GradleDependenciesStatsColumn,
    GradleDependenciesColumn,
    GradleDependenciesConfigs,
)
from utils import Extensions, create_directory

GradleDependenciesStats = Dict[str, Any]


def save_stats_with_to_csv(path_to_dir: str, filename: str, stats: pd.DataFrame):
    csv_file_path = os.path.join(path_to_dir, filename)
    stats.sort_values(by=GradleDependenciesStatsColumn.COUNT, ascending=False).to_csv(csv_file_path, index=False)


def get_full_name_stats(df: pd.DataFrame, unique=False) -> pd.DataFrame:
    full_names_stats = defaultdict(dict)
    projects_by_full_names = defaultdict(set)
    for project_name, _, group_id, artifact_id, config in df.values:
        full_name = f"{group_id}:{artifact_id}"
        if full_name not in full_names_stats:
            full_names_stats[full_name][GradleDependenciesStatsColumn.FULL_NAME] = full_name
            full_names_stats[full_name][GradleDependenciesStatsColumn.COUNT] = 0
            for c in GradleDependenciesConfigs:
                full_names_stats[full_name][c.value] = 0
        if not unique or project_name not in projects_by_full_names[full_name]:
            projects_by_full_names[full_name].add(project_name)
            full_names_stats[full_name][GradleDependenciesConfigs(config)] += 1
            full_names_stats[full_name][GradleDependenciesStatsColumn.COUNT] += 1

    stats = defaultdict(list)
    for _full_name, full_name_stats in full_names_stats.items():
        for k in full_name_stats.keys():
            stats[k].append(full_name_stats[k])
    return pd.DataFrame.from_dict(stats)


def merge_stats_with_meta(stats: pd.DataFrame, meta: pd.DataFrame) -> pd.DataFrame:
    df = pd.merge(meta, stats, how='outer', on=GradleDependenciesStatsColumn.FULL_NAME)
    df = df.reindex(columns=[c.value for c in GradleDependenciesStatsColumn])
    return df


def analyze_unique_full_names_stats(path_to_result_dir: str, meta: pd.DataFrame, df: pd.DataFrame):
    stats = get_full_name_stats(df, unique=True)
    save_stats_with_to_csv(path_to_result_dir, f"gradle_dependencies_by_project_stats.{Extensions.CSV}", stats)
    meta_stats = merge_stats_with_meta(stats, meta)
    save_stats_with_to_csv(
        path_to_result_dir,
        f"gradle_dependencies_by_project_with_meta_stats.{Extensions.CSV}",
        meta_stats,
    )


def analyze_full_names_stats(path_to_result_dir: str, meta: pd.DataFrame, df: pd.DataFrame):
    stats = get_full_name_stats(df)
    save_stats_with_to_csv(path_to_result_dir, f"gradle_dependencies_by_module_stats.{Extensions.CSV}", stats)
    meta_stats = merge_stats_with_meta(stats, meta)
    save_stats_with_to_csv(
        path_to_result_dir,
        f"gradle_dependencies_by_module_with_meta_stats.{Extensions.CSV}",
        meta_stats,
    )


def get_gradle_dependencies(path_to_dependencies: str, path_to_tagged_projects: str, tags: List[str]) -> pd.DataFrame:
    dependencies = pd.read_csv(path_to_dependencies)
    if path_to_tagged_projects:
        tagged_projects = {project: tag for project, tag in pd.read_csv(path_to_tagged_projects).values}
        dependencies = dependencies[
            dependencies[GradleDependenciesColumn.PROJECT_NAME].apply(
                lambda project_name: tagged_projects[project_name] in tags,
            )
        ]
    return dependencies


def get_gradle_dependencies_meta(path_to_dependencies_meta: str) -> pd.DataFrame:
    return pd.read_csv(path_to_dependencies_meta)


def analyze(
    path_to_dependencies: str,
    path_to_result_dir: str,
    path_to_dependencies_meta: str,
    path_to_tagged_projects: str,
    tags: List[str],
):
    create_directory(path_to_result_dir)

    df = get_gradle_dependencies(path_to_dependencies, path_to_tagged_projects, tags)
    meta = get_gradle_dependencies_meta(path_to_dependencies_meta)
    print(f"Got {df.size} gradle dependencies")
    analyze_full_names_stats(path_to_result_dir, meta, df)
    analyze_unique_full_names_stats(path_to_result_dir, meta, df)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('--input', type=str, help='path to csv file with gradle dependencies csv with columns:'
                                                  'project_name -- project name'
                                                  'group_id -- dependency group id'
                                                  'artifact_id -- dependency artifact id'
                                                  'config -- dependency configuration', required=True)
    parser.add_argument('--output', type=str, help='path to output dir with result', required=True)
    parser.add_argument('--meta', type=str, help='path to file with gradle dependencies meta file with columns:'
                                                 'full_name -- groupId:artifactId of dependency'
                                                 'url -- url to github_data or other library resource'
                                                 'community -- url to github_data or other library resource'
                                                 'multiplatform -- url to github_data or other library resource'
                                                 'language -- language for library', required=True)
    parser.add_argument('--tagged_projects', type=str, default=None, help='path to csv file with tagged projects',
                        required=True)
    parser.add_argument('--tags', help='path to csv file with tagged projects', nargs='+', default=['android', 'other'])
    parser.add_argument('--ignore', type=str, default=None, help='path to csv file with dependencies to ignore')

    args = parser.parse_args(sys.argv[1:])
    analyze(args.input, args.output, args.meta, args.tagged_projects, args.tags)
