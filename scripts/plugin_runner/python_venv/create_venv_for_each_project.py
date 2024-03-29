"""
This script creates a virtual environment for each project from the dataset.

It accepts
    * path to the folder with python projects.
    * flag that allows you not to install dependencies for each package (--no-deps flag for pip).
    * flag that allows you not to cache packages (--no-cache-dir flag for pip).
    * flag that allows you to install requirements individually.

The virtual environment is created in the root of the project in the folder named ".venv".
"""

import argparse
import logging
import sys
from pathlib import Path

from plugin_runner.python_venv.common import create_venv, install_requirements

from utils.file_utils import FileSystemItem, get_all_file_system_items
from utils.python.requirements_utils import gather_requirements_file_paths

logger = logging.getLogger(__name__)


def configure_arguments(parser: argparse.ArgumentParser) -> None:
    parser.add_argument(
        'dataset_path',
        help='Path to the dataset with projects, for each of which a virtual environment must be created.',
        type=lambda value: Path(value).absolute(),
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
        '--no-cache',
        help=(
            'If specified, the downloaded packages will not be cached (the --no-cache-dir flag will be passed to pip).'
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


def main() -> bool:
    parser = argparse.ArgumentParser()
    configure_arguments(parser)

    logging.basicConfig(level=logging.INFO, format='%(asctime)s | %(levelname)s | %(message)s')

    args = parser.parse_args()

    projects = get_all_file_system_items(args.dataset_path, item_type=FileSystemItem.SUBDIR, with_subdirs=False)

    is_error = False
    for project in projects:
        logger.info(f'Creating a virtual environment for the "{project.name}".')

        requirements_file_paths = gather_requirements_file_paths(project)

        venv_path = project / '.venv'
        create_venv(venv_path)

        for requirements_file in requirements_file_paths:
            exit_code = install_requirements(
                venv_path,
                requirements_file,
                args.no_package_dependencies,
                args.pip_for_each,
                args.no_cache,
            )

            # If we have an error when installing requirements from some file,
            # we should try to install requirements from other files instead of skipping them.
            if exit_code != 0:
                is_error = True

    return is_error


if __name__ == '__main__':
    sys.exit(main())
