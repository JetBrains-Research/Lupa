"""
This script allows the user to find out actual full names of GitHub repositories
to avoid duplicated projects in a dataset.
The script makes requests to GitHub API, saves unique full names of repositories
and optionally saves all JSON responses containing repositories metadata.
It accepts
    * path to csv file --  dataset, f.e. downloaded from https://seart-ghs.si.usi.ch/
    * path to output directory
    * optional parameter to save metadata
    * index to start from (default 0)
"""

import os
import pandas as pd
import argparse
import logging
from typing import Set, TextIO, Tuple

from data_collection.data_collection_utils import save_repo_json, get_github_token, create_github_session
from utils import create_directory, Extensions


def main():
    logging.basicConfig(level=logging.DEBUG)
    args = parse_args()
    jsons_dir_path, results_path = create_output_paths(args.output, args.save_metadata)

    dataset = pd.read_csv(args.csv_path)
    token = get_github_token()
    headers = {'Authorization': f'token {token}'}
    session = create_github_session()

    with open(results_path, 'a') as results_fout:
        if args.start_from == 0:
            results_fout.write("full_name\n")
        unique_names = set()
        for index, project in enumerate(dataset.name[args.start_from:]):
            username, project_name = project.split('/')
            query_url = f"https://api.github.com/repos/{username}/{project_name}"
            r = session.get(query_url, headers=headers)
            response_json = r.json()

            if r.status_code != 200:
                if response_json["message"] == "Moved Permanently":  # 301
                    logging.info(f"Repository {username}#{project_name} moved permanently, redirecting")
                    r = session.get(r.url, headers=headers)
                    response_json = r.json()
                else:
                    logging.info(
                        f"Request failed with status code: {r.status_code}. Message: {response_json['message']}",
                    )
                    continue

            save_full_name(response_json["full_name"], unique_names, results_fout)
            if args.save_metadata:
                save_repo_json(response_json, jsons_dir_path, f"{username}#{project_name}.{Extensions.JSON}")

            logging.info(f"Last processed project: {args.start_from + index}")


def create_output_paths(output_directory: str, save_metadata: bool = False) -> Tuple[str, str]:
    create_directory(output_directory)
    jsons_dir_path = os.path.join(output_directory, "jsons/")
    if save_metadata:
        create_directory(jsons_dir_path)
    results_path = os.path.join(output_directory, "results.csv")
    return jsons_dir_path, results_path


def save_full_name(full_name: str, unique_names: Set[str], file_to_write: TextIO):
    if full_name not in unique_names:
        file_to_write.write(f"{full_name}\n")
        unique_names.add(full_name)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("csv_path", metavar="csv-path", help="Path to csv file with github repositories data")
    parser.add_argument("output", help="Output directory")
    parser.add_argument('--save-metadata', help="Enable saving jsons containing project metadata", action='store_true')
    parser.add_argument("--start-from", help="Index of the project to start from", nargs='?', const=0, type=int)
    return parser.parse_args()


if __name__ == "__main__":
    main()
