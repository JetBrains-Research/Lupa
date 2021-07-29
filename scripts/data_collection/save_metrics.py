"""
This script runs github metrics saver every day.
It stores current number of stars, forks and issues for given repositories.
It accepts
    * path to csv file --  dataset with full names of repositories
    * path to output directory
"""
import os
import sys
import pandas as pd
import argparse
import logging
from requests.packages.urllib3.util.retry import Retry
import schedule
import time
from github import Github
from datetime import datetime

module_path = os.path.abspath(os.path.join(os.path.realpath(__file__), os.pardir, os.pardir))
if module_path not in sys.path:
    sys.path.append(module_path)

from utils import create_directory


def main():
    logging.basicConfig(level=logging.DEBUG)
    args = parse_args()
    results_path = create_output_paths(args.output)
    with open(results_path, 'w') as results_fout:
        header = "\t".join(["full_name", "stars", "forks", "issues", "time"])
        results_fout.write(header + "\n")

    os.environ['GIT_TERMINAL_PROMPT'] = '0'
    token = os.getenv('GITHUB_TOKEN')
    github = Github(token, retry=Retry(total=50, backoff_factor=10, status_forcelist=[403]))

    schedule.every().day.at("01:00").do(save_metrics, args.csv_path, results_path, github)

    while True:
        schedule.run_pending()
        time.sleep(60)


def save_metrics(input_path: str, output_path: str, github: Github):
    dataset = pd.read_csv(input_path)

    with open(output_path, 'a') as results_fout:
        for index, project in enumerate(dataset.full_name):
            repo = github.get_repo(project)
            stars = repo.stargazers_count
            forks = repo.forks_count
            issues = repo.open_issues_count
            cur_time = datetime.today().isoformat()
            results_fout.write("\t".join(map(str, [repo.full_name, stars, forks, issues, cur_time])) + "\n")
            logging.info(f"Last processed project index: {index}")


def create_output_paths(output_directory: str) -> str:
    create_directory(output_directory)
    results_path = os.path.join(output_directory, "results.csv")
    return results_path


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("csv_path", metavar="csv-path", help="Path to csv file with github repositories data")
    parser.add_argument("output", help="Output directory")
    return parser.parse_args()


if __name__ == "__main__":
    main()
