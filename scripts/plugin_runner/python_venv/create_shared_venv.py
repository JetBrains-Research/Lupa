"""
This script creates a virtual environment and installs the requirements gathered from a given python dataset.

It accepts
    * path to the folder with python projects.
    * path to the folder where you want to create the environment.
    * flag that allows you not to do version validation using PyPI.
    * flag that allows you not to do package name validation using PyPI.
    * flag that allows you not to install dependencies for each package (--no-deps flag for pip).
    * flag that allows you to install requirements individually.

In the current version of the script, if we find different versions of the same library
in different requirement files, we will choose the newest (largest) version.
"""

import argparse
import logging
import sys
from pathlib import Path

from plugin_runner.python_venv.common import (
    create_requirements_file,
    create_venv,
    filter_unavailable_packages,
    filter_unavailable_versions,
    install_requirements,
    merge_requirements,
)

from utils.python.requirements_utils import gather_requirements

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

    parser.add_argument(
        '--pip-for-each',
        help=(
            'Call `pip install` for each requirement individually. '
            'By default, `pip install` will be applied to the entire file with the collected requirements.'
        ),
        action='store_true',
    )


def main() -> int:
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
    create_venv(args.venv_path)
    return install_requirements(args.venv_path, requirements_path, args.no_package_dependencies, args.pip_for_each)


if __name__ == '__main__':
    sys.exit(main())
