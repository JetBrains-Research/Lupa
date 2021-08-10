import argparse
import re
import sys
from typing import Optional

import pandas as pd

from analysis.gradle_dependencies.column_names_utils import GradleDependenciesMetaColumn
from data_collection.github.api import get_repo
from data_collection.package_search.api import get_packages


def url_to_repo_name(url: str) -> Optional[str]:
    result = re.search('https://github.com/(.*)', url)
    return None if result is None or len(result.groups()) == 0 else result.group(1)


def get_gradle_dependencies_meta_csv(path_to_stats: str, path_to_result: str):
    data = pd.read_csv(path_to_stats)
    data = data[:10]
    full_names = list(set([f"{group_id}:{artifact_id}"
                           for group_id, artifact_id in data[["group_id", "artifact_id"]].values]))

    packages = get_packages(full_names)
    full_names, urls, languages = [], [], []

    for package in packages:
        full_name = f"{package.group_id}:{package.artifact_id}"
        url = package.scm.url if package.scm is not None and package.scm.url is not None else package.url
        language = None
        if url is not None:
            repo_name = url_to_repo_name(url)
            if repo_name is not None:
                repo = get_repo(repo_name)
                if repo is not None:
                    language = repo.language
        full_names.append(full_name)
        urls.append(url)
        languages.append(language)

    pd.DataFrame.from_dict({
        GradleDependenciesMetaColumn.FULL_NAME: full_names,
        GradleDependenciesMetaColumn.URL: urls,
        GradleDependenciesMetaColumn.LANGUAGE: languages,
    }).to_csv(path_to_result, index=False)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('--input', type=str, help='path to csv file with gradle dependencies csv with columns:'
                                                  'project_name -- project name'
                                                  'group_id -- dependency group id'
                                                  'artifact_id -- dependency artifact id'
                                                  'config -- dependency configuration', required=True)
    parser.add_argument('--output', type=str, help='path to output dir with result', required=True)

    args = parser.parse_args(sys.argv[1:])

    get_gradle_dependencies_meta_csv(args.input, args.output)
