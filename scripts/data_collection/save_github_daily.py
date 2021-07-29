"""
This script runs GitHub API every day and saves all collected data about given repositories.
It accepts
    * path to csv file --  dataset with full names of repositories
    * path to output directory
"""
import os
import sys
import typing

import pandas as pd
import argparse
import logging

import requests
import schedule
import time
from datetime import datetime

module_path = os.path.abspath(os.path.join(os.path.realpath(__file__), os.pardir, os.pardir))
if module_path not in sys.path:
    sys.path.append(module_path)

from utils import create_directory, Extensions
from data_collection.data_collection_utils import save_repo_json, get_github_token, create_github_session


def main():
    logging.basicConfig(level=logging.DEBUG)
    args = parse_args()
    create_directory(args.output)

    token = get_github_token()
    headers = {'Authorization': f'token {token}'}
    session = create_github_session()

    schedule.every().day.at("17:00").do(save_jsons, args.csv_path, args.output, session, headers)

    while True:
        schedule.run_pending()
        time.sleep(60)


def save_jsons(input_path: str, output_path: str, session: requests.Session, headers: typing.Dict):
    dataset = pd.read_csv(input_path)
    day = datetime.today().strftime("%d-%m-%Y")
    day_output = os.path.join(output_path, day)
    create_directory(day_output)

    for index, project in enumerate(dataset.full_name):
        username, project_name = project.split('/')
        query_url = f"https://api.github.com/repos/{username}/{project_name}"
        r = session.get(query_url, headers=headers)
        response_json = r.json()

        if r.status_code != 200:
            logging.info(f"Request failed with status code: {r.status_code}. Message: {response_json['message']}")
            continue

        save_repo_json(response_json, day_output, f"{username}#{project_name}.{Extensions.JSON}")
        logging.info(f"Last processed project index: {index}")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("csv_path", metavar="csv-path", help="Path to csv file with github repositories data")
    parser.add_argument("output", help="Output directory")
    return parser.parse_args()


if __name__ == "__main__":
    main()
