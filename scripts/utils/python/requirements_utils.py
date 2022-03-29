import logging
import os
import re
from collections import defaultdict
from distutils.version import Version
from pathlib import Path
from typing import Dict, List, Set, Tuple

from pkg_resources import parse_requirements as parse_line, parse_version

from utils.file_utils import get_all_file_system_items

Specs = Set[Tuple[str, Version]]
Requirements = Dict[str, Specs]

REQUIREMENTS_FILE_NAME_REGEXP = r'^[\S]*requirements[\S]*\.txt$'

logger = logging.getLogger(__name__)


def normalize_requirement_name(name: str) -> str:
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
                    logger.warning(f'Unable to parse line "{line.strip()}" in the file {str(file_path)}.')
                    continue

        for requirement in file_requirements:
            specs = {(operator, parse_version(version)) for operator, version in requirement.specs}
            requirements[normalize_requirement_name(requirement.key)] |= specs

    logger.info(f'Collected {len(requirements)} packages.')

    return requirements
