import argparse
import re
import sys
from collections import defaultdict
from typing import Optional

import pandas as pd

from analysis.gradle_dependencies.column_names_utils import GradleDependenciesMetaColumn, GradleDependenciesColumn
from analysis.gradle_plugins.column_names_utils import GradlePluginsColumn
from data_collection.github_data.api import get_repo
from data_collection.package_search.api import get_packages


def url_to_repo_name(url: str) -> Optional[str]:
    result = re.search('https://github.com/([^/]*)/([^/]*)(/.*)?', url)
    return None if result is None or len(result.groups()) < 2 else "/".join([result.group(1), result.group(2)])


def check_is_community(full_name) -> bool:
    full_name_not_community_artifacts = ['org.jetbrains.kotlin', 'androidx', 'com.android', 'com.google']
    return not any(artifact in full_name for artifact in full_name_not_community_artifacts)


def check_is_multiplatform(repo_name: str, plugins: pd.DataFrame) -> Optional[bool]:
    if repo_name is None:
        return None
    return ((plugins[GradlePluginsColumn.PROJECT_NAME] == repo_name.replace('/', '#').lower())
            & (plugins[GradlePluginsColumn.PLUGIN_ID] == 'org.jetbrains.kotlin.multiplatform')).any()


def get_gradle_dependencies_meta_csv(path_to_stats: str, path_to_result: str, path_to_plugins: str):
    data = pd.read_csv(path_to_stats)[:10]
    plugins = pd.read_csv(path_to_plugins)

    full_names = list({f"{group_id}:{artifact_id}" for group_id, artifact_id
                       in data[[GradleDependenciesColumn.GROUP_ID,
                                GradleDependenciesColumn.ARTIFACT_ID]].values})

    packages = get_packages(full_names)
    meta = defaultdict(list)

    for package in packages:
        full_name = f"{package.group_id}:{package.artifact_id}"
        url = package.scm.url if package.scm is not None and package.scm.url is not None else package.url
        language = None
        repo_name = None
        if url is not None:
            repo_name = url_to_repo_name(url)
            if repo_name is not None:
                repo = get_repo(repo_name)
                if repo is not None:
                    language = repo.language

        meta[GradleDependenciesMetaColumn.FULL_NAME].append(full_name)
        meta[GradleDependenciesMetaColumn.REPO_NAME].append(repo_name)
        meta[GradleDependenciesMetaColumn.URL].append(url)

        meta[GradleDependenciesMetaColumn.LANGUAGE].append(language)
        meta[GradleDependenciesMetaColumn.COMMUNITY].append(check_is_community(full_name))
        meta[GradleDependenciesMetaColumn.MULTIPLATFORM].append(check_is_multiplatform(repo_name, plugins))

    pd.DataFrame.from_dict(meta).to_csv(path_to_result, index=False)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('--input', type=str, help='path to csv file with gradle dependencies csv with columns:'
                                                  'project_name -- project name'
                                                  'group_id -- dependency group id'
                                                  'artifact_id -- dependency artifact id'
                                                  'config -- dependency configuration', required=True)
    parser.add_argument('--output', type=str, help='path to output dir with result', required=True)
    parser.add_argument('--plugins', type=str, help='path to csv with libraries plugins', required=True)

    args = parser.parse_args(sys.argv[1:])
    get_gradle_dependencies_meta_csv(args.input, args.output, args.plugins)
