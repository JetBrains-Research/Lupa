"""
This script allows you to create a virtual environment
and install the requirements gathered from a given dataset with python projects.

It accepts
    * path to the folder with python projects
    * path to the folder where you want to create the environment
    * flag that allows you not to do version validation using PyPI.
    * flag that allows you not to install dependencies for each package (--no-deps flag for pip).
"""

import argparse
import json
import logging
import subprocess
from collections import defaultdict
from distutils.version import Version
from pathlib import Path
from typing import Dict, Optional, Set, Tuple

import pkg_resources
import requests

PYPI_PACKAGE_METADATA_URL = 'https://pypi.org/pypi/{package_name}/json'
REQUIREMENTS_FILE_NAME_REGEXP = '*requirements*.txt'

Requirements = Set[Tuple[str, Version]]

logger = logging.getLogger(__name__)


def configure_arguments(parser: argparse.ArgumentParser) -> None:
    parser.add_argument(
        'dataset_path',
        help='Path to dataset with projects from which you want to get dependencies and create a virtual environment.',
        type=lambda value: Path(value).absolute(),
    )

    parser.add_argument(
        'venv_path',
        help='Path to the folder where you want to create the virtual environment.',
        type=lambda value: Path(value).absolute(),
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


def gather_requirements(dataset_path: Path) -> Dict[str, Requirements]:
    """
    Collects requirements from all projects.

    :param dataset_path: the path to the folder with the projects that contain the requirements files.
    :return: dictionary, where for each package the collected requirements are listed.
             The requirement is a pair of operator and version.
    """

    requirements_by_package_name = defaultdict(set)

    for file_path in dataset_path.rglob(REQUIREMENTS_FILE_NAME_REGEXP):
        with open(file_path) as file:
            try:
                requirements = pkg_resources.parse_requirements(file)
            except pkg_resources.RequirementParseError:
                logger.info(f'Unable to parse {str(file_path)}.')
                continue

            for requirement in requirements:
                specs = {(operator, pkg_resources.parse_version(version)) for operator, version in requirement.specs}
                requirements_by_package_name[requirement.key] |= specs

    return requirements_by_package_name


def _get_available_versions(package_name: str) -> Optional[Set[Version]]:
    """
    By a given package, collects a list of all the versions available on PyPI.

    :param package_name: PyPI package name.
    :return: list of available versions.
    """

    url = PYPI_PACKAGE_METADATA_URL.format(package_name=package_name)
    try:
        metadata = requests.get(url).json()
    except requests.exceptions.RequestException as exception:
        logger.error(f'An error occurred when accessing the PyPI (package name: {package_name}).', exception)
        return None
    except json.JSONDecodeError:
        logger.error(f'Failed to get a version for the {package_name} package.')
        return None

    versions = metadata['releases'].keys()
    return set(map(pkg_resources.parse_version, versions))


def filter_unavailable_versions(requirements_by_package_name: Dict[str, Requirements]) -> Dict[str, Requirements]:
    """
    Removes all versions of packages that are not on PyPI.

    :param requirements_by_package_name: dictionary, where for each package the collected requirements are listed.
                                         The requirement is a pair of operator and version.
    :return: dictionary, where for each package the collected requirements are listed.
             The requirements contain only those versions which are available on the PyPI.
    """

    filtered_requirements_by_package_name = {}

    for name, requirements in requirements_by_package_name.items():
        logger.info(f'Checking {name} versions.')

        available_versions = _get_available_versions(name)

        specs = {}
        if available_versions is not None:
            specs = {(operator, version) for operator, version in requirements if version in available_versions}
        else:
            logger.warning(f'Unable to check {name} versions.')

        filtered_requirements_by_package_name[name] = specs

    return filtered_requirements_by_package_name


def merge_requirements(requirements_by_package_name: Dict[str, Requirements]) -> Dict[str, Optional[Version]]:
    """
    For each package leaves only the highest version of requirements.

    :param requirements_by_package_name: dictionary, where for each package the collected requirements are listed.
                                         The requirement is a pair of operator and version.
    :return: dictionary, where a version is specified for each package.
    """

    version_by_package_name = {}
    for name, requirements in requirements_by_package_name.items():
        max_version = None
        if requirements:
            max_version = max(version for _, version in requirements)
        version_by_package_name[name] = max_version
    return version_by_package_name


def create_requirements_file(version_by_package_name: Dict[str, Optional[str]], requirements_dir: Path) -> Path:
    """
    Creates a requirements file from the passed dictionary.

    :param version_by_package_name: dictionary, where for each package, specifies the version to be installed.
    :param requirements_dir: the path where the requirements file will be created.
    :return: the path to the created requirements file.
    """

    requirements_dir.mkdir(exist_ok=True, parents=True)
    path_to_requirements = requirements_dir / 'requirements.txt'

    with open(path_to_requirements, mode='w+') as file:
        for name, version in version_by_package_name.items():
            if version is None:
                file.write(f'{name}\n')
            else:
                file.write(f'{name}=={version}\n')

    return path_to_requirements


def create_venv(venv_path: Path, requirements_path: Path, no_package_dependencies: bool) -> None:
    """
    In the passed path creates a virtual environment and installs the passed requirements.

    :param venv_path: the path where the virtual environment will be created.
    :param requirements_path: the path to the requirements file.
    :param no_package_dependencies: whether it is necessary to not install dependencies for each package.
    :return: none
    """
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

    subprocess.run(pip_command)


def main():
    parser = argparse.ArgumentParser()
    configure_arguments(parser)

    logging.basicConfig(level=logging.INFO, format='%(asctime)s | %(levelname)s | %(message)s')

    args = parser.parse_args()

    requirements_by_package_name = gather_requirements(args.dataset_path)
    if not args.no_version_validation:
        requirements_by_package_name = filter_unavailable_versions(requirements_by_package_name)
    version_by_package_name = merge_requirements(requirements_by_package_name)
    requirements_path = create_requirements_file(version_by_package_name, args.venv_path)
    create_venv(args.venv_path, requirements_path, args.no_package_dependencies)


if __name__ == '__main__':
    main()
