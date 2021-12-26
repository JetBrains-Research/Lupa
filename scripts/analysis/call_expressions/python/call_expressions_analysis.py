import argparse
from pathlib import Path

import pandas as pd

from call_expressions_column import CallExpressionsColumn


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

    fq_names = pd.read_csv(input_path)

    stats = fq_names.groupby(CallExpressionsColumn.FQ_NAME.value).aggregate(
        {CallExpressionsColumn.FQ_NAME.value: 'count', CallExpressionsColumn.CATEGORY.value: 'first'},
    )
    stats.rename(columns={CallExpressionsColumn.FQ_NAME.value: CallExpressionsColumn.COUNT.value}, inplace=True)
    stats.reset_index(CallExpressionsColumn.FQ_NAME.value, inplace=True)

    output_path.parent.mkdir(parents=True, exist_ok=True)
    stats.to_csv(output_path, index=False)


if __name__ == '__main__':
    main()
