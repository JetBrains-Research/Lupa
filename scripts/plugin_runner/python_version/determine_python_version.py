# TODO: doc

import argparse
import logging
from pathlib import Path
from typing import List, Optional

import pandas as pd

from plugin_runner.python_version.utils import PythonClassifiers, PythonVersion

from utils.file_utils import FileSystemItem, get_all_file_system_items

logger = logging.getLogger(__name__)


def _try_to_find_version_in_setup_file(file_path: Path) -> Optional[PythonVersion]:
    """
    Try to find the Python version using the PyPI classifiers in the setup file.

    :param file_path: The path to the setup file.
    :return: The found version of Python. If no Python version could be found, None will be returned.
    """
    with open(file_path) as file:
        content = file.read()

    if PythonClassifiers.PYTHON_3_ONLY.value in content:
        return PythonVersion.PYTHON_3

    if PythonClassifiers.PYTHON_2_ONLY.value in content:
        return PythonVersion.PYTHON_2

    for python_version in PythonVersion:
        if any(classifier in content for classifier in PythonClassifiers.get_classifiers_by_version(python_version)):
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

    # TODO: try to determine the version using the requirements files.

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
        help='The path where you want to save the csv table with the versions.',
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
