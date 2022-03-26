import json
import logging
import os
import re
import subprocess
from collections import defaultdict
from copy import copy
from distutils.version import Version
from pathlib import Path
from typing import Dict, List, Optional, Set, Tuple

from pkg_resources import parse_requirements as parse_line, parse_version

import requests
from requests.adapters import HTTPAdapter

from urllib3 import Retry

from utils.file_utils import get_all_file_system_items

PYPI_PACKAGE_METADATA_URL = 'https://pypi.org/pypi/{package_name}/json'
REQUIREMENTS_FILE_NAME_REGEXP = r'^[\S]*requirements[\S]*\.txt$'

Specs = Set[Tuple[str, Version]]
Requirements = Dict[str, Specs]

logger = logging.getLogger(__name__)


def _normalize_requirement_name(name: str) -> str:
    """
    Normalize the string: the name is converted to lowercase and all dots and underscores are replaced by hyphens.

    :param name: Name of package
    :return: Normalized name of package
    """
    normalized_name = name.lower()
    normalized_name = normalized_name.replace('.', '-')
    normalized_name = normalized_name.replace('_', '-')
    return normalized_name


def gather_requirements_file_paths(root: Path) -> List[Path]:
    """
    Gather paths to all files with requirements on the passed path.

    :param root: The path to the folder where you want to find the requirements files.
    :return: List of file paths.
    """
    requirements_file_paths = get_all_file_system_items(
        root=root,
        item_condition=lambda name: re.match(REQUIREMENTS_FILE_NAME_REGEXP, name) is not None,
    )

    # TODO: handle symlinks
    requirements_file_paths = list(filter(lambda path: os.path.isfile(path), requirements_file_paths))

    logger.info(f'{len(requirements_file_paths)} requirement files have been collected.')

    return requirements_file_paths


def gather_requirements(root: Path) -> Requirements:
    """
    Gather the requirements from the requirements files contained in the passed path.

    :param root: The path to the folder that contain the requirements files.
    :return: Dictionary, where for each package the collected specs are listed.
             The spec is a pair of operator and version.
    """
    logger.info('Collecting requirements.')

    requirements = defaultdict(set)
    requirements_file_paths = gather_requirements_file_paths(root)

    for file_path in requirements_file_paths:
        file_requirements = []
        with open(file_path, encoding='utf8', errors='ignore') as file:
            for line in file.readlines():
                try:
                    file_requirements.extend(list(parse_line(line)))
                except Exception:
                    # For some reason you can't catch RequirementParseError
                    # (or InvalidRequirement), so we catch Exception.
                    logger.warning(f'Unable to parse line "{line.strip()}" in the file {str(file_path)}. Skipping.')
                    continue

        for requirement in file_requirements:
            specs = {(operator, parse_version(version)) for operator, version in requirement.specs}
            requirements[_normalize_requirement_name(requirement.key)] |= specs

    logger.info(f'Collected {len(requirements)} packages.')

    return requirements


def _create_session() -> requests.Session:
    session = requests.Session()
    retries = Retry(total=10, backoff_factor=0.1)
    session.mount('https://', HTTPAdapter(max_retries=retries))
    return session


def filter_unavailable_packages(requirements: Requirements) -> Requirements:
    """
    Remove all package names that are not on PyPI.

    :param requirements: Dictionary, where for each package the specs are listed.
                         The spec is a pair of operator and version.
    :return: Dictionary, where for each package the specs are listed. The spec is a pair of operator and version.
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
    By a given package, collect a list of all the versions available on PyPI.

    :param package_name: PyPI package name.
    :return: Set of available versions. If the version could not be obtained, None will be returned.
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

    return set(map(parse_version, versions))


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

        available_versions = _get_available_versions(package_name)

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
