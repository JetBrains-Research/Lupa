import argparse
import logging
from pathlib import Path

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
        help='path to csv with result',
        required=True,
    )


def main():
    parser = argparse.ArgumentParser()
    configure_parser(parser)

    args = parser.parse_args()
    input_path: Path = args.input
    output_path: Path = args.output

    fq_names = pd.read_csv(input_path, keep_default_na=False)
    logging.info(f'Received {len(fq_names)} FQ names.')

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

    output_path.parent.mkdir(parents=True, exist_ok=True)
    stats.to_csv(output_path, index=False)
    logging.info('Saving call expressions stats.')


if __name__ == '__main__':
    main()
