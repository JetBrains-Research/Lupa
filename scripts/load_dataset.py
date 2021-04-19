"""
This script allows the user to clone repositories listed in dataset from GitHub.
It accepts
    * path to csv file --  dataset downloaded from https://seart-ghs.si.usi.ch/
    * path to output directory, where repositories are cloned
"""

import pandas as pd
import subprocess
import argparse
import os
from utils import create_directory


def main():
    args = parse_args()
    create_directory(args.output)

    dataset = pd.read_csv(args.csv_path)
    os.environ['GIT_TERMINAL_PROMPT'] = '0'
    for project in dataset.name:
        p = subprocess.Popen(["git", "clone", f"https://github.com/{project}.git"],
                             cwd=args.output)
        return_code = p.wait()
        if return_code != 0:
            print(f"Error while cloning {project}, skipping..")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--csv_path", help="Path to csv file with github repositories data")
    parser.add_argument("--output", help="Output directory")
    return parser.parse_args()


if __name__ == "__main__":
    main()
