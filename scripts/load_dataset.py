"""
This script allows the user to clone repositories listed in dataset from GitHub.
It accepts
    * path to CSV file --  dataset downloaded from https://seart-ghs.si.usi.ch/
    * path to the output directory, where repositories are cloned
    * allowed extensions to filter, f.e. to save only .kt files
    * index to start from
"""
import logging

import pandas as pd
import subprocess
import argparse
import os

from filter_dataset import filter_files
from utils import create_directory


def main():
    args = parse_args()
    create_directory(args.output)

    dataset = pd.read_csv(args.csv_path)
    os.environ['GIT_TERMINAL_PROMPT'] = '0'
    for project in dataset.full_name[args.start_from:]:
        username, project_name = project.split('/')
        project_directory = f"{username}#{project_name}"
        create_directory(os.path.join(args.output, project_directory))
        p = subprocess.Popen(["git", "clone", f"https://github.com/{project}.git", project_directory, "--depth", "1"],
                             cwd=args.output)
        return_code = p.wait()
        if return_code != 0:
            logging.info(f"Error while cloning {project}, skipping..")
        elif args.allowed_extensions is not None:
            project_directory = os.path.join(args.output, project_directory)
            filter_files(project_directory, project_directory, args.allowed_extensions)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("csv_path", help="Path to csv file with github repositories data")
    parser.add_argument("output", help="Output directory")
    parser.add_argument("--allowed_extensions", help=" optional Allowed file extensions", nargs="+")
    parser.add_argument("--start_from", help="Index of repository to start from", nargs='?', const=0, type=int)
    return parser.parse_args()


if __name__ == "__main__":
    main()
