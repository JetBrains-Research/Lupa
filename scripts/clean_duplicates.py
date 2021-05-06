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

import json
import os
from typing import Set, TextIO, Tuple

import pandas as pd
import argparse
import requests
import logging
from utils import create_directory
from requests.adapters import HTTPAdapter
from requests.packages.urllib3.util.retry import Retry


def main():
    logging.basicConfig(level=logging.DEBUG)
    args = parse_args()
    jsons_dir_path, results_path = create_output_paths(args.output, args.save_metadata)

    dataset = pd.read_csv(args.csv_path)
    os.environ['GIT_TERMINAL_PROMPT'] = '0'
    token = os.getenv('GITHUB_TOKEN')
    headers = {'Authorization': f'token {token}'}

    # retries to avoid github api rate limit (403 status code)
    s = requests.Session()
    retries = Retry(total=50, backoff_factor=10, status_forcelist=[403])
    s.mount('https://', HTTPAdapter(max_retries=retries))

    with open(results_path, 'a') as results_fout:
        if args.start_from == 0:
            results_fout.write("full_name\n")
        unique_names = set()
        for index, project in enumerate(dataset.name[args.start_from:]):
            username, project_name = project.split('/')
            query_url = f"https://api.github.com/repos/{username}/{project_name}"
            r = s.get(query_url, headers=headers)
            response_json = r.json()

            if r.status_code != 200:
                if response_json["message"] == "Moved Permanently":  # 301
                    logging.info(f"Repository {username}#{project_name} moved permanently, redirecting")
                    r = s.get(r.url, headers=headers)
                    response_json = r.json()
                else:
                    logging.info(f"Request failed with status code: {r.status_code}. Message: {response_json['message']}")
                    continue

            save_full_name(response_json["full_name"], unique_names, results_fout)
            if args.save_metadata:
                save_project_json(response_json, jsons_dir_path, f"{username}#{project_name}.json")

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


def save_project_json(project_json: dict, jsons_dir_path: str, file_name: str):
    output_file = os.path.join(jsons_dir_path, file_name)
    with open(output_file, 'w+') as fout:
        json.dump(project_json, fout, indent=4)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("csv_path", help="Path to csv file with github repositories data")
    parser.add_argument("output", help="Output directory")
    parser.add_argument('--save_metadata', help="Enable saving jsons containing project metadata", action='store_true')
    parser.add_argument("--start_from", help="Index of the project to start from", nargs='?', const=0, type=int)
    return parser.parse_args()


if __name__ == "__main__":
    main()
