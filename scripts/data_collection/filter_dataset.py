import argparse
import os
from shutil import copy
from typing import List

from utils import create_directory


def main():
    args = parse_args()
    create_directory(args.output)
    filter_files(args.input, args.output, args.extension_list)


def filter_files(input_dir: str, output_dir: str, extension_list: List[str]):
    dot_extensions = list(map(lambda s: "." + s, extension_list))
    for root, _dirs, files in os.walk(input_dir):
        for file in files:
            file_abs_path = os.path.join(root, file)
            if not allowed_extension(file, dot_extensions) and input_dir == output_dir:
                os.remove(file_abs_path)

            elif allowed_extension(file, dot_extensions) and input_dir != output_dir:
                file_rel_path = os.path.relpath(file_abs_path, input_dir)
                result_path = os.path.join(output_dir, file_rel_path)
                create_directory(os.path.dirname(result_path))
                copy(file_abs_path, result_path)


def allowed_extension(filename: str, extensions_list: List[str]) -> bool:
    return any(map(lambda x: filename.endswith(x), extensions_list))


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("input", help="Input directory")
    parser.add_argument("output", help="Output directory")
    parser.add_argument("--extension-list", help="Allowed file extensions", nargs="+", default=["kt"])
    return parser.parse_args()


if __name__ == "__main__":
    main()
