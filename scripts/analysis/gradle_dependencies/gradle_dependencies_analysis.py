import argparse
import os
import sys
from typing import Dict, List, Any

import pandas as pd

from analysis.gradle_dependencies.column_names_utils import GradleDependenciesStatsColumn, GradleDependenciesColumn
from utils import Extensions, create_directory

GradleDependenciesStats = Dict[str, Any]


def save_stats_with_to_csv(path_to_dir: str, filename: str, stats: GradleDependenciesStats):
    csv_file_path = os.path.join(path_to_dir, filename)
    pd.DataFrame.from_dict(stats) \
        .sort_values(by="count", ascending=False) \
        .to_csv(csv_file_path, index=False)


def get_full_name_to_url(path_to_dependencies_urls: str) -> Dict[str, str]:
    return {full_name: url for full_name, url, _ in pd.read_csv(path_to_dependencies_urls).values}


def get_full_name_to_language(path_to_dependencies_urls: str) -> Dict[str, str]:
    return {full_name: language for full_name, _, language in pd.read_csv(path_to_dependencies_urls).values}


def check_is_community(full_name) -> bool:
    full_name_not_community_artifacts = ["org.jetbrains.kotlin", "androidx", "com.android", "com.google"]
    return not any(artifact in full_name for artifact in full_name_not_community_artifacts)


def get_full_name_stats(df: pd.DataFrame, path_to_dependencies_meta: str, unique=False) -> GradleDependenciesStats:
    full_name_to_url = get_full_name_to_url(path_to_dependencies_meta)
    full_name_to_language = get_full_name_to_language(path_to_dependencies_meta)
    full_names = {}
    projects_by_full_names = {}
    for project_name, _, group_id, artifact_id, config in df.values:
        full_name = f"{group_id}:{artifact_id}"
        if full_name not in full_names:
            projects_by_full_names[full_name] = set()
            full_names[full_name] = {c.value: 0 for c in GradleDependenciesStatsColumn}
            full_names[full_name][GradleDependenciesStatsColumn.FULL_NAME] = full_name
            full_names[full_name][GradleDependenciesStatsColumn.URL] = full_name_to_url.get(full_name, None)
            full_names[full_name][GradleDependenciesStatsColumn.LANGUAGE] = full_name_to_language.get(full_name, None)
            full_names[full_name][GradleDependenciesStatsColumn.COMMUNITY] = check_is_community(full_name)
        if not unique or project_name not in projects_by_full_names[full_name]:
            projects_by_full_names[full_name].add(project_name)
            full_names[full_name][GradleDependenciesStatsColumn.COUNT] += 1
            full_names[full_name][GradleDependenciesStatsColumn(config)] += 1

    stats = {c.value: [] for c in GradleDependenciesStatsColumn}
    for full_name, full_name_stats in full_names.items():
        for c in GradleDependenciesStatsColumn:
            stats[c.value].append(full_name_stats[c.value])
    return stats


def analyze_unique_full_names_stats(path_to_result_dir: str, path_to_dependencies_meta: str, df: pd.DataFrame):
    full_name_stats = get_full_name_stats(df, path_to_dependencies_meta, unique=True)
    save_stats_with_to_csv(path_to_result_dir, f"gradle_dependencies_by_project_stats.{Extensions.CSV}",
                           full_name_stats)


def analyze_full_names_stats(path_to_result_dir: str, path_to_dependencies_meta: str, df: pd.DataFrame):
    full_name_stats = get_full_name_stats(df, path_to_dependencies_meta)
    save_stats_with_to_csv(path_to_result_dir, f"gradle_dependencies_by_module_stats.{Extensions.CSV}", full_name_stats)


def get_gradle_dependencies(path_to_dependencies: str, path_to_tagged_projects: str, tags: List[str]) -> pd.DataFrame:
    dependencies = pd.read_csv(path_to_dependencies)
    tagged_projects = {project: tag for project, tag in pd.read_csv(path_to_tagged_projects).values}
    return dependencies[dependencies[GradleDependenciesColumn.PROJECT_NAME]
        .apply(lambda project_name: tagged_projects[project_name] in tags)]


def analyze(path_to_dependencies: str, path_to_result_dir: str, path_to_dependencies_meta: str,
            path_to_tagged_projects: str, tags: List[str]):
    create_directory(path_to_result_dir)

    df = get_gradle_dependencies(path_to_dependencies, path_to_tagged_projects, tags)
    print(f"Got {df.size} gradle dependencies")
    analyze_full_names_stats(path_to_result_dir, path_to_dependencies_meta, df)
    analyze_unique_full_names_stats(path_to_result_dir, path_to_dependencies_meta, df)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('--input', type=str, help='path to csv file with gradle dependencies csv wit columns:'
                                                  'project_name -- project name'
                                                  'group_id -- dependency group id'
                                                  'artifact_id -- dependency artifact id'
                                                  'config -- dependency configuration', required=True)
    parser.add_argument('--output', type=str, help='path to output dir with result', required=True)
    parser.add_argument('--meta', type=str, help='path to dir with csv with columns:'
                                                 'full_name -- groupId:artifactId of dependency'
                                                 'url -- url to github or other library resource'
                                                 'language -- language for library', required=True)
    parser.add_argument('--tagged_projects', type=str, default=None, help='path to csv file with tagged projects',
                        required=True)
    parser.add_argument('--tags', help='path to csv file with tagged projects', nargs='+', default=['android', 'other'])
    parser.add_argument('--ignore', type=str, default=None, help='path to csv file with dependencies to ignore')

    args = parser.parse_args(sys.argv[1:])

    analyze(args.input, args.output, args.meta, args.tagged_projects, args.tags)
