import argparse
import os
from shutil import copy
from utils import create_directory


def main():
    args = parse_args()
    create_directory(args.output)

    for root, dirs, files in os.walk(args.input):
        for file in files:
            if file.endswith(".kt"):
                file_abs_path = os.path.join(root, file)
                file_rel_path = os.path.relpath(file_abs_path, args.input)
                result_path = os.path.join(args.output, file_rel_path)
                create_directory(os.path.dirname(result_path))
                copy(file_abs_path, result_path)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", help="Input directory")
    parser.add_argument("--output", help="Output directory")
    return parser.parse_args()


if __name__ == "__main__":
    main()