import logging
import subprocess
from copy import copy
from distutils.version import Version
from pathlib import Path
from typing import Dict, Optional

from utils.python.pypi_utils import check_package_exists, get_available_versions
from utils.python.requirements_utils import Requirements

logger = logging.getLogger(__name__)


def filter_unavailable_packages(requirements: Requirements) -> Requirements:
    """
    Remove all package names that are not on PyPI.

    :param requirements: Dictionary, where for each package the specs are listed.
                         The spec is a pair of operator and version.
    :return: Dictionary, where for each package the specs are listed. The spec is a pair of operator and version.
    """
    logger.info('Filtering unavailable packages.')

    filtered_requirements = copy(requirements)
    for package_name in requirements.keys():
        logger.info(f'Checking {package_name}.')

        exists = check_package_exists(package_name)

        if exists is None:
            logger.warning(f'Unable to check the {package_name} package. Skipping.')
            continue

        if not exists:
            logger.warning(f'The {package_name} package does not exist on PyPI. Removing the package from requirements.')
            filtered_requirements.pop(package_name)

    logger.info(f'Filtered {len(requirements) - len(filtered_requirements)} packages.')
    return filtered_requirements


def filter_unavailable_versions(specs: Requirements) -> Requirements:
    """
    Remove all versions of packages that are not on PyPI.

    :param specs: Dictionary, where for each package the specs are listed.
                  The spec is a pair of operator and version.
    :return: Dictionary, where for each package the specs are listed. The spec is a pair of operator and version.
             The specs contain only those versions which are available on the PyPI.
    """
    logger.info('Filtering unavailable versions.')

    filtered_requirements = {}
    for package_name, package_specs in specs.items():
        logger.info(f'Checking {package_name} versions.')

        available_versions = get_available_versions(package_name)

        filtered_specs = package_specs
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
    For each package leave only the highest version of requirements.

    :param requirements: Dictionary, where for each package the specs are listed.
                         The spec is a pair of operator and version.
    :return: Dictionary, where a version is specified for each package.
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
    Create a requirements file from the passed dictionary.

    :param version_by_package_name: Dictionary, where for each package, specifies the version to be installed.
    :param requirements_dir: The path where the requirements file will be created.
    :return: The path to the created requirements file.
    """
    logger.info('Creating requirements file.')

    requirements_dir.mkdir(exist_ok=True, parents=True)
    path_to_requirements_file = requirements_dir / 'requirements.txt'

    with open(path_to_requirements_file, mode='w+') as file:
        for package_name, version in version_by_package_name.items():
            if version is None:
                file.write(f'{package_name}\n')
            else:
                file.write(f'{package_name}=={version}\n')

    return path_to_requirements_file


def create_venv(venv_path: Path) -> None:
    """
    Create a virtual environment in the passed path.

    :param venv_path: The path where the virtual environment will be created.
    """
    logger.info('Creating virtual environment.')

    venv_path.mkdir(exist_ok=True, parents=True)

    subprocess.run(
        [
            'python3',
            '-m',
            'venv',
            str(venv_path),
        ],
    )


def install_requirements(venv_path: Path, requirements_path: Path, no_package_dependencies: bool, for_each: bool):
    """
    Install all requirements from the requirements file in the virtual environment.

    :param venv_path: The path where the virtual environment is located.
    :param requirements_path: The path to the requirements file.
    :param no_package_dependencies: Whether it is necessary to not install dependencies for each package.
    :param for_each: Is it necessary to call `pip install` for each requirement individually or for the whole file.
    :return: Pip return code or number of pip errors if for_each flag is specified.
    """
    logger.info(f'Installing requirements from {requirements_path}.')

    pip_command = [
        venv_path / 'bin' / 'pip',
        'install',
        '--disable-pip-version-check',
    ]

    if no_package_dependencies:
        pip_command.append('--no-deps')

    if for_each:
        errors = 0
        with open(requirements_path, 'r') as requirements_file:
            for requirement in requirements_file:
                errors += subprocess.run(pip_command + [requirement.strip()]).returncode != 0
        return errors

    return subprocess.run(pip_command + ['-r', str(requirements_path)]).returncode
