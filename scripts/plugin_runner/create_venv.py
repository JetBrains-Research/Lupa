"""
This script allows you to create a virtual environment
and install the requirements gathered from a given dataset with python projects.

It accepts
    * path to the folder with python projects.
    * path to the folder where you want to create the environment.
    * flag that allows you not to do version validation using PyPI.
    * flag that allows you not to do package name validation using PyPI.
    * flag that allows you not to install dependencies for each package (--no-deps flag for pip).

In the current version of the script, if we find different versions of the same library
in different requirement files, we will choose the newest (largest) version.
"""

import argparse
import json
import logging
import os
import re
import subprocess
import sys
from collections import defaultdict
from copy import copy
from distutils.version import Version
from enum import unique, Enum
from pathlib import Path
from typing import Dict, Optional, Set, Tuple

import pkg_resources
import requests
from requests.adapters import HTTPAdapter
from urllib3 import Retry

PYPI_PACKAGE_METADATA_URL = 'https://pypi.org/pypi/{package_name}/json'
REQUIREMENTS_FILE_NAME_REGEXP = r'[\S]*requirements[\S]*.txt'

Specs = Set[Tuple[str, Version]]
Requirements = Dict[str, Specs]

logger = logging.getLogger(__name__)


def configure_arguments(parser: argparse.ArgumentParser) -> None:
    parser.add_argument(
        'dataset_path',
        help='Path to dataset with projects from which you want to get requirements and create a virtual environment.',
        type=lambda value: Path(value).absolute(),
    )

    parser.add_argument(
        'venv_path',
        help='Path to the folder where you want to create the virtual environment.',
        type=lambda value: Path(value).absolute(),
    )

    parser.add_argument(
        '--no-package-name-validation',
        help='If specified, no package name validation will be performed using PyPI.',
        action='store_true',
    )

    parser.add_argument(
        '--no-version-validation',
        help='If specified, no version validation will be performed using PyPI.',
        action='store_true',
    )

    parser.add_argument(
        '--no-package-dependencies',
        help=(
            'If specified, no dependencies will be installed for each package '
            '(the --no-deps flag will be passed to pip).'
        ),
        action='store_true',
    )


# TODO: move into utils
@unique
class FileSystemItem(Enum):
    PATH = 0
    SUBDIR = 1
    FILE = 2


def gather_requirements(dataset_path: Path) -> Requirements:
    """
    Collects requirements from all projects.

    :param dataset_path: the path to the folder with the projects that contain the requirements files.
    :return: dictionary, where for each package the collected specs are listed.
             The spec is a pair of operator and version.
    """

    logger.info('Collecting requirements.')

    requirements = defaultdict(set)
    # TODO: create an util function
    for fs_tuple in os.walk(dataset_path):
        for file_path in fs_tuple[FileSystemItem.FILE.value]:
            if not re.match(REQUIREMENTS_FILE_NAME_REGEXP, file_path):
                continue
    # for file_path in dataset_path.rglob(REQUIREMENTS_FILE_NAME_REGEXP):
            absolute_path = Path(os.path.join(fs_tuple[FileSystemItem.PATH.value], file_path))
            with open(absolute_path, encoding='utf8', errors='ignore') as file:
                file_requirements = []
                for index, line in enumerate(file.readlines()):
                    try:
                        file_requirements.extend(list(pkg_resources.parse_requirements(line)))
                    except Exception:
                        # For some reason you can't catch RequirementParseError
                        # (or InvalidRequirement), so we catch Exception.
                        logger.info(f'Unable to parse line number {index} in the file {str(absolute_path)}. Skipping.')
                        continue

                for requirement in file_requirements:
                    specs = {(operator, pkg_resources.parse_version(version)) for operator, version in requirement.specs}
                    requirements[requirement.key] |= specs

    logger.info(f'Collected {len(requirements)} packages.')

    return requirements


def _create_session() -> requests.Session:
    session = requests.Session()
    retries = Retry(total=10, backoff_factor=0.1)
    session.mount('https://', HTTPAdapter(max_retries=retries))
    return session


def filter_unavailable_packages(requirements: Requirements) -> Requirements:
    """
    Removes all package names that are not on PyPI.

    :param requirements: dictionary, where for each package the specs are listed.
                         The spec is a pair of operator and version.
    :return: dictionary, where for each package the specs are listed. The spec is a pair of operator and version.
    """

    logger.info('Filtering unavailable packages.')

    session = _create_session()
    filtered_requirements = copy(requirements)
    for package_name in requirements.keys():
        logger.info(f'Checking {package_name}.')

        try:
            response = session.get(PYPI_PACKAGE_METADATA_URL.format(package_name=package_name))

            if response.status_code == 200:
                continue

            if response.status_code == 404:
                logger.warning(
                    f'The {package_name} package does not exist on PyPI. Removing the package from requirements.',
                )
                filtered_requirements.pop(package_name)
            else:
                logger.warning(
                    f'PyPI returned an unexpected code ({response.status_code}). Skipping.',
                )

        except requests.exceptions.RequestException:
            logger.error('An error occurred when accessing the PyPI. Skipping.')

    logger.info(f'Filtered {len(requirements) - len(filtered_requirements)} packages.')
    return filtered_requirements


def _get_available_versions(package_name: str) -> Set[Version]:
    """
    By a given package, collects a list of all the versions available on PyPI.

    :param package_name: PyPI package name.
    :return: set of available versions. If the version could not be obtained, None will be returned.
    """

    session = _create_session()
    url = PYPI_PACKAGE_METADATA_URL.format(package_name=package_name)
    try:
        metadata = session.get(url).json()
    except requests.exceptions.RequestException:
        logger.error('An error occurred when accessing the PyPI. Skipping.')
        return set()
    except json.JSONDecodeError:
        logger.error(f'Failed to get a version for the {package_name} package. Skipping')
        return set()

    if 'releases' in metadata.keys():
        versions = metadata['releases'].keys()
    else:
        logger.error('The PyPI response does not contain the "releases" field. Skipping.')
        return set()

    return set(map(pkg_resources.parse_version, versions))


def filter_unavailable_versions(specs: Requirements) -> Requirements:
    """
    Removes all versions of packages that are not on PyPI.

    :param specs: dictionary, where for each package the specs are listed.
                  The spec is a pair of operator and version.
    :return: dictionary, where for each package the specs are listed. The spec is a pair of operator and version.
             The specs contain only those versions which are available on the PyPI.
    """

    logger.info('Filtering unavailable versions.')

    filtered_requirements = {}
    for package_name, specs in specs.items():
        logger.info(f'Checking {package_name} versions.')

        available_versions = _get_available_versions(package_name)

        filtered_specs = specs
        if available_versions:
            filtered_specs = {
                (operator, version) for operator, version in filtered_specs if version in available_versions
            }
        else:
            logger.warning(f'Unable to check {package_name} versions.')

        filtered_requirements[package_name] = filtered_specs

    return filtered_requirements


def merge_requirements(requirements: Requirements) -> Dict[str, Optional[Version]]:
    """
    For each package leaves only the highest version of requirements.

    :param requirements: dictionary, where for each package the specs are listed.
                         The spec is a pair of operator and version.
    :return: dictionary, where a version is specified for each package.
    """

    logger.info('Merging requirements.')

    version_by_package_name = {}
    for package_name, specs in requirements.items():
        max_version = None
        if specs:
            max_version = max(version for _, version in specs)
        version_by_package_name[package_name] = max_version
    return version_by_package_name


def create_requirements_file(version_by_package_name: Dict[str, Optional[Version]], requirements_dir: Path) -> Path:
    """
    Creates a requirements file from the passed dictionary.

    :param version_by_package_name: dictionary, where for each package, specifies the version to be installed.
    :param requirements_dir: the path where the requirements file will be created.
    :return: the path to the created requirements file.
    """

    logger.info('Creating requirements file.')

    requirements_dir.mkdir(exist_ok=True, parents=True)
    path_to_requirements = requirements_dir / 'requirements.txt'

    with open(path_to_requirements, mode='w+') as file:
        for package_name, version in version_by_package_name.items():
            if version is None:
                file.write(f'{package_name}\n')
            else:
                file.write(f'{package_name}=={version}\n')

    return path_to_requirements


def create_venv(venv_path: Path, requirements_path: Path, no_package_dependencies: bool) -> int:
    """
    In the passed path creates a virtual environment and installs the passed requirements.

    :param venv_path: the path where the virtual environment will be created.
    :param requirements_path: the path to the requirements file.
    :param no_package_dependencies: whether it is necessary to not install dependencies for each package.
    :return: pip return code
    """

    logger.info('Creating virtual environment.')

    venv_path.mkdir(exist_ok=True, parents=True)
    pip_path = venv_path / 'bin' / 'pip'

    subprocess.run(
        [
            'python3',
            '-m',
            'venv',
            str(venv_path),
        ],
    )

    pip_command = [
        str(pip_path),
        'install',
        '-r',
        str(requirements_path),
    ]

    if no_package_dependencies:
        pip_command.append('--no-deps')

    return subprocess.run(pip_command).returncode


def main():
    parser = argparse.ArgumentParser()
    configure_arguments(parser)

    logging.basicConfig(level=logging.INFO, format='%(asctime)s | %(levelname)s | %(message)s')

    args = parser.parse_args()

    requirements = gather_requirements(args.dataset_path)

    if not args.no_package_name_validation:
        requirements = filter_unavailable_packages(requirements)

    if not args.no_version_validation:
        requirements = filter_unavailable_versions(requirements)

    version_by_package_name = merge_requirements(requirements)
    requirements_path = create_requirements_file(version_by_package_name, args.venv_path)
    exit_code = create_venv(args.venv_path, requirements_path, args.no_package_dependencies)

    return exit_code


if __name__ == '__main__':
    sys.exit(main())
