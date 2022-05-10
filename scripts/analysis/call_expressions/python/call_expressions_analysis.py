import argparse
import logging
from pathlib import Path
from typing import Optional

from call_expressions_column import CallExpressionsColumn

import pandas as pd

logging.basicConfig(level=logging.INFO)


def configure_parser(parser: argparse.ArgumentParser):
    parser.add_argument(
        '--input',
        type=lambda value: Path(value).absolute(),
        help='path to csv file with FQ names',
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


def collect_stats(fq_names: pd.DataFrame) -> pd.DataFrame:
    total_stats = fq_names.drop(CallExpressionsColumn.CATEGORY.value, axis=1).drop_duplicates()
    total_stats = total_stats.value_counts([CallExpressionsColumn.FQ_NAME.value])
    total_stats = total_stats.reset_index(name=CallExpressionsColumn.TOTAL.value)

    grouped_stats = fq_names.groupby([CallExpressionsColumn.FQ_NAME.value, CallExpressionsColumn.CATEGORY.value])
    stats = grouped_stats[CallExpressionsColumn.PROJECT_NAME.value].count()
    stats = stats.reset_index(name=CallExpressionsColumn.COUNT.value)
    stats = stats.pivot(
        index=CallExpressionsColumn.FQ_NAME.value,
        columns=CallExpressionsColumn.CATEGORY.value,
        values=CallExpressionsColumn.COUNT.value,
    )

    stats.fillna(0, inplace=True)
    stats = stats.astype(int)
    stats.reset_index(inplace=True)

    stats = stats.merge(total_stats, on=CallExpressionsColumn.FQ_NAME.value)

    logging.info(f'Processed {len(stats)} unique FQ names.')
    return stats


def main():
    parser = argparse.ArgumentParser()
    configure_parser(parser)

    args = parser.parse_args()
    input_path: Path = args.input
    output_path: Path = args.output
    python_versions_path: Optional[Path] = args.python_versions

    output_path.mkdir(parents=True, exist_ok=True)

    fq_names = pd.read_csv(input_path, keep_default_na=False)
    logging.info(f'Received {len(fq_names)} FQ names.')

    if python_versions_path is None:
        stats = collect_stats(fq_names)
        stats.to_csv(output_path / 'call_expressions_stats.csv', index=False)
        logging.info('Saving call expressions stats.')
    else:
        python_versions = pd.read_csv(python_versions_path, keep_default_na=False, na_values='')
        fq_names_with_versions = fq_names.merge(
            python_versions,
            how='left',
            on=CallExpressionsColumn.PROJECT_NAME.value,
        )
        fq_names_by_version = fq_names_with_versions.groupby(CallExpressionsColumn.PYTHON_VERSION.value, dropna=False)

        for python_version, fq_names in fq_names_by_version:
            if pd.isna(python_version):
                python_version = 'PYTHON_UNKNOWN'

            fq_names.drop(columns=[CallExpressionsColumn.PYTHON_VERSION.value], inplace=True)

            logging.info(f'Processing {python_version}.')
            stats = collect_stats(fq_names)
            stats.to_csv(output_path / f'{python_version}.csv', index=False)
            logging.info(f'Saving call expressions stats ({python_version}).')


if __name__ == '__main__':
    main()
