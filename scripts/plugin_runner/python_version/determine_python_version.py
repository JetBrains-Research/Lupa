"""
This script tries to determine the Python version for projects from a dataset using metadata from PyPI.

It accepts
    * path to the folder with python projects.
    * path to `csv` file, where to save versions.

First, we try to determine the version using the classifiers from the `setup.py` or `setup.cfg` files. If this fails,
for each project we collect the requirements and use their classifiers specified on the PyPI to determine the version.
"""

import argparse
import logging
import re
from pathlib import Path
from typing import List, Optional, Set

import pandas as pd

from plugin_runner.python_version.python_classifiers import PythonClassifiers, PythonVersion

from utils.file_utils import FileSystemItem, get_all_file_system_items, get_file_content
from utils.python.pypi_utils import get_available_versions, get_package_classifiers
from utils.python.requirements_utils import Requirements, gather_requirements

logger = logging.getLogger(__name__)


def _try_to_find_version_in_setup_file(file_path: Path) -> Optional[PythonVersion]:
    """
    Try to find the Python version using the PyPI classifiers in the setup file.

    :param file_path: The path to the setup file.
    :return: The found version of Python. If no Python version could be found, None will be returned.
    """
    content = get_file_content(file_path)

    if PythonClassifiers.PYTHON_3_ONLY.value in content:
        return PythonVersion.PYTHON_3

    if PythonClassifiers.PYTHON_2_ONLY.value in content:
        return PythonVersion.PYTHON_2

    for python_version in PythonVersion:
        if any(classifier in content for classifier in PythonClassifiers.get_classifiers_by_version(python_version)):
            return python_version

    return None


def _try_to_determine_version_using_classifiers(classifiers: Set[str]) -> Optional[PythonVersion]:
    """
    Try to determine the Python version using the classifiers obtained from the PyPI package metadata.

    :param classifiers: List of PyPI classifiers.
    :return: Python version. If it could not be determined will be returned None.
    """
    python_versions = PythonClassifiers.get_versions_by_classifiers(classifiers)

    if len(python_versions) == 1:
        return python_versions.pop()

    return None


def _try_to_determine_version_using_requirements(requirements: Requirements) -> Optional[PythonVersion]:
    """
    Try to determine the python version using the requirements files.

    All requirements are considered to determine the version, except those that use the operators "<", ">", "!=".
    Also, if the version ends with ".*", any existing version with the same prefix is taken instead.

    :param requirements: Dictionary, where for each package the collected specs are listed.
                         The spec is a pair of operator and version.
    :return: Python version. If it could not be determined will be returned None.
    """
    packages_without_specs = set()
    for package_name, package_specs in requirements.items():
        if not package_specs:
            packages_without_specs.add(package_name)
            continue

        for operator, version in package_specs:
            if operator in {'<', '>', '!='}:
                continue

            version = str(version)

            if version.endswith('.*'):
                available_versions = get_available_versions(package_name)
                prefix = re.sub(r'\.\*$', '', version)
                available_versions = {
                    available_version
                    for available_version in available_versions
                    if str(available_version).startswith(prefix)
                }

                if not available_versions:
                    continue

                version = str(available_versions.pop())

            version_classifiers = get_package_classifiers(package_name, version)

            python_version = _try_to_determine_version_using_classifiers(version_classifiers)
            if python_version is not None:
                return python_version

    for package_name in packages_without_specs:
        package_classifiers = get_package_classifiers(package_name)
        python_version = _try_to_determine_version_using_classifiers(package_classifiers)
        if python_version is not None:
            return python_version

    return None


def _find_setup_files(project_path: Path) -> List[Path]:
    """
    Find setup files ("setup.py" or "setup.cfg") in the project.

    :param project_path: The path to the project where you want to find the setup files.
    :return: List of paths to the found setup files.
    """
    return get_all_file_system_items(project_path, lambda name: name == 'setup.py' or name == 'setup.cfg')


def determine_version(project_path: Path) -> Optional[PythonVersion]:
    """
    Determine the Python version used in the project.

    If there are setup files in the project, try to determine the Python version using them,
    otherwise, if this was not possible or there are no such files in the project,
    determine the version using requirements files.

    :param project_path: The path to the project for which you want to determine the version.
    :return: The Python version used in the project. If the version could not be determined, None is returned.
    """
    logger.info(f'Processing {project_path.name}.')

    logger.info('Try to determine the Python version using the setup files.')

    setup_files = _find_setup_files(project_path)
    logger.info(f'Found {len(setup_files)} setup files.')

    for setup_file in setup_files:
        python_version = _try_to_find_version_in_setup_file(setup_file)
        if python_version is not None:
            logger.info(f'Determined Python version: {python_version.value}')
            return python_version

    logger.info('Try to determine the Python version using the requirements files.')

    requirements = gather_requirements(project_path)
    python_version = _try_to_determine_version_using_requirements(requirements)
    if python_version is not None:
        logger.info(f'Determined Python version: {python_version.value}')
        return python_version

    logger.warning('Unable to determine the version of Python.')
    return None


def configure_arguments(parser: argparse.ArgumentParser) -> None:
    parser.add_argument(
        'dataset_path',
        help='Path to the dataset with the projects for which you want to determine the python version used.',
        type=lambda value: Path(value).absolute(),
    )

    parser.add_argument(
        'output_path',
        help='Path where you want to save the csv table with the versions.',
        type=lambda value: Path(value).absolute(),
    )


def main() -> None:
    logging.basicConfig(level=logging.INFO, format='%(asctime)s | %(levelname)s | %(message)s')

    parser = argparse.ArgumentParser()
    configure_arguments(parser)

    args = parser.parse_args()

    projects = get_all_file_system_items(args.dataset_path, item_type=FileSystemItem.SUBDIR, with_subdirs=False)

    project_to_version = {}
    for project in projects:
        project_to_version[project.name] = determine_version(project)

    df = pd.DataFrame.from_dict(project_to_version, orient='index', columns=['python_version'])
    df.index.name = 'project_name'

    args.output_path.parent.mkdir(exist_ok=True, parents=True)
    df.to_csv(args.output_path)


if __name__ == '__main__':
    main()
