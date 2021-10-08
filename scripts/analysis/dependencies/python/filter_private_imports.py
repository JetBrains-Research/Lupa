import argparse
import re
from pathlib import Path

import pandas as pd


def _is_private_import(fq_import_name: str):
    import_parts = fq_import_name.split('.')

    # `_thread` is a module of the Python Standard Library
    if import_parts[0] == '_thread':
        return False

    return any(
        [import_part.startswith('_') and re.match(r'__.*__', import_part) is None for import_part in import_parts],
    )


def main(path_to_fq_names: Path, path_to_result: Path):
    fq_names = pd.read_csv(path_to_fq_names)

    print(f'Received {len(fq_names)} imports.')

    mask = fq_names.apply(lambda row: _is_private_import(row['import']), axis=1)
    filtered_fq_names = fq_names[~mask]

    print(f'Filtered {len(fq_names) - len(filtered_fq_names)} private imports.')

    path_to_result.parent.mkdir(parents=True, exist_ok=True)
    filtered_fq_names.to_csv(path_to_result, index=False)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument(
        '--input',
        type=lambda value: Path(value).absolute(),
        help='path to csv file with fq_names',
        required=True,
    )
    parser.add_argument(
        '--output',
        type=lambda value: Path(value).absolute(),
        help='path to output csv file with filtered fq_names',
        required=True,
    )

    args = parser.parse_args()

    main(args.input, args.output)
