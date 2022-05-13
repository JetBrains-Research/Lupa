"""
This script analyzes Python imports.

It accepts
    * path to csv file with FQ names.
    * path to the folder where to save the stats.
    * path to the csv file with labeled projects by python version.

For each unique import name, the number of projects in which it occurs is counted.
It is also possible to group statistics by language version of Python.

This script is a wrapper over import_directives_analysis.py.
"""

import argparse
import logging
from pathlib import Path
from typing import Optional, Tuple

from analysis.dependencies.fq_names_tree import build_fq_name_tree_decomposition
from analysis.dependencies.import_directives_analysis import (
    fq_names_groups_to_stats,
    fq_names_to_dict,
    get_prefix_by_package,
    group_fq_names_by,
)
from analysis.dependencies.python.imports_column import ImportsColumn

import pandas as pd

logging.basicConfig(level=logging.INFO)

# These are the default Python arguments passed to import_directives_analysis.py
MAX_PACKAGE_LEN = 3
MAX_SUBPACKAGES = 10000
MAX_LEAF_SUBPACKAGES = 0.8
MIN_OCCURRENCE = 100
MAX_OCCURRENCE = 1500
MAX_U_OCCURRENCE = 500


def configure_parser(parser: argparse.ArgumentParser):
    parser.add_argument(
        '--input',
        type=lambda value: Path(value).absolute(),
        help='path to csv file with fq names',
        required=True,
    )

    parser.add_argument(
        '--output',
        type=lambda value: Path(value).absolute(),
        help='path to the folder where to save the stats',
        required=True,
    )

    parser.add_argument(
        '--python-versions',
        type=lambda value: Path(value).absolute(),
        help='path to the csv file with labeled projects by python version',
    )


def collect_stats(imports: pd.DataFrame) -> Tuple[pd.DataFrame, pd.DataFrame]:
    total_stats = imports.value_counts([ImportsColumn.IMPORT.value])
    total_stats = total_stats.reset_index(name=ImportsColumn.COUNT.value)
    total_stats.rename(columns={ImportsColumn.IMPORT.value: ImportsColumn.FQ_NAME.value}, inplace=True)

    fq_names = imports[ImportsColumn.IMPORT.value].tolist()
    fq_names_dict = fq_names_to_dict(fq_names)

    _, sub_roots = build_fq_name_tree_decomposition(
        fq_names_dict=fq_names_dict,
        max_subpackages=MAX_SUBPACKAGES,
        max_leaf_subpackages=MAX_LEAF_SUBPACKAGES,
        min_occurrence=MIN_OCCURRENCE,
        max_occurrence=MAX_OCCURRENCE,
        max_u_occurrence=MAX_U_OCCURRENCE,
    )

    packages = [sub_root.full_name for sub_root in sub_roots]
    fq_names_by_package = group_fq_names_by(
        fq_names=fq_names,
        group_by_function=lambda fq_name: get_prefix_by_package(fq_name, packages, MAX_PACKAGE_LEN),
    )
    fq_names_by_package_stats = fq_names_groups_to_stats(fq_names_by_package)

    package_stats = pd.DataFrame(
        fq_names_by_package_stats.items(),
        columns=[ImportsColumn.FQ_NAME.value, ImportsColumn.COUNT.value],
    )

    logging.info(f'Processed {len(total_stats)} unique FQ names and {len(package_stats)} unique package names.')
    return total_stats, package_stats


def main():
    parser = argparse.ArgumentParser()
    configure_parser(parser)

    args = parser.parse_args()

    input_path: Path = args.input
    output_path: Path = args.output
    python_versions_path: Optional[Path] = args.python_versions

    output_path.mkdir(parents=True, exist_ok=True)

    imports = pd.read_csv(input_path, keep_default_na=False)
    logging.info(f'Received {len(imports)} imports.')

    if python_versions_path is None:
        total_stats, package_stats = collect_stats(imports)
        total_stats.to_csv(output_path / 'import_stats.csv', index=False)
        package_stats.to_csv(output_path / 'import_stats_by_package.csv', index=False)
        logging.info('Saving import stats.')
    else:
        python_versions = pd.read_csv(python_versions_path, keep_default_na=False, na_values='')
        imports_with_versions = imports.merge(
            python_versions,
            how='left',
            on=ImportsColumn.PROJECT_NAME.value,
        )
        imports_by_version = imports_with_versions.groupby(ImportsColumn.PYTHON_VERSION.value, dropna=False)

        total_stats_path = output_path / 'import_stats'
        packages_stats_path = output_path / 'import_stats_by_package'

        total_stats_path.mkdir(exist_ok=True)
        packages_stats_path.mkdir(exist_ok=True)

        for python_version, imports in imports_by_version:
            if pd.isna(python_version):
                python_version = 'PYTHON_UNKNOWN'

            logging.info(f'Processing {python_version}.')
            total_stats, package_stats = collect_stats(imports)

            total_stats.to_csv(total_stats_path / f'{python_version}.csv', index=False)
            package_stats.to_csv(packages_stats_path / f'{python_version}.csv', index=False)

            logging.info(f'Saving import stats ({python_version}).')


if __name__ == '__main__':
    main()
