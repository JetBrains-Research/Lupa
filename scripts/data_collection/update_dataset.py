"""
This script allows the user to clone repositories listed in dataset from GitHub or pull changes from repository.

Each repository is cloned without history (or with, if necessary and argument `store-history` is passed).
When the argument `save-to-db` is passed, the date of the last pull of each repository
is saved to the database configured by `database.ini` file

Script accepts
    * path to CSV file --  dataset downloaded from https://seart-ghs.si.usi.ch/
    * path to the output directory, where repositories are cloned
    * index to start from
    * whether to use database or n
"""
import argparse
import datetime
import logging
import os
from dataclasses import dataclass

from data_collection.db_connect import DatabaseConn
from data_collection.git_repo import GitRepository
from data_collection.repositories_table import RepositoriesTable

import pandas as pd

from utils.file_utils import create_directory

logging.basicConfig(level=logging.INFO)


@dataclass
class ApplicationConfig:
    input_path: str
    output_path: str
    start_from: int
    save_to_db: bool
    store_history: bool


def update_dataset(config):
    create_directory(config.output_path)

    db_conn = DatabaseConn() if config.save_to_db else None
    repositories_table = RepositoriesTable(db_conn)
    repositories_table.create()

    dataset = pd.read_csv(config.input_path)
    os.environ['GIT_TERMINAL_PROMPT'] = '0'
    for index, project in enumerate(dataset.full_name[config.start_from:]):
        logging.info(f'Start processing project {project} (index {index + config.start_from})')

        username, project_name = project.split('/')
        project_directory_name = f'{username}#{project_name}'
        project_directory = os.path.abspath(os.path.join(config.output_path, project_directory_name))
        git_repo = GitRepository(project, project_directory, config.store_history)

        cur_date = datetime.datetime.today().date()
        exists_in_db = repositories_table.exists_repository(username, project_name)

        if os.path.exists(project_directory):
            was_updated = git_repo.pull_changes()
            if exists_in_db and was_updated:
                logging.info(f'Repository {project} has been updated')
                repositories_table.update_date(username, project_name, cur_date)
            elif exists_in_db:
                logging.info(f"Repository {project} hasn't been updated")
            else:
                logging.info(
                    f'Repository {project} is stored on disk, but there is no information about it in the database. '
                    f'Adding {project} to database if needed...')
                repositories_table.insert(username, project_name, cur_date)

        else:
            was_cloned = git_repo.clone()

            if not was_cloned:
                logging.info(f'Error while cloning {project}, skipping..')
            elif exists_in_db:
                logging.info(f'Somehow repository {project} is stored in the database, but not on the disk')
                repositories_table.update_date(username, project_name, cur_date)
            else:
                repositories_table.insert(username, project_name, cur_date)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('csv_path', metavar='csv-path', help='Path to csv file with github repositories data')
    parser.add_argument('output', help='Output directory')
    parser.add_argument('--start-from', help='Index of repository to start from', nargs='?', default=0, type=int)
    parser.add_argument('--save-to-db', help='Save date of repository last pull to the database', action='store_true')
    parser.add_argument('--store-history', help='Store the whole commit history of repositories', action='store_true')
    args = parser.parse_args()
    config = ApplicationConfig(args.csv_path, args.output, args.start_from, args.save_to_db, args.store_history)
    update_dataset(config)


if __name__ == '__main__':
    main()
