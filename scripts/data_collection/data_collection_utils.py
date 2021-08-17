import json
import os

import requests
from requests.adapters import HTTPAdapter
from urllib3 import Retry


def save_repo_json(project_json: dict, jsons_dir_path: str, file_name: str):
    output_file = os.path.join(jsons_dir_path, file_name)
    with open(output_file, 'w+') as fout:
        json.dump(project_json, fout, indent=4)


def get_github_token() -> str:
    os.environ['GIT_TERMINAL_PROMPT'] = '0'
    token = os.getenv('GITHUB_TOKEN')
    return token


# retries to avoid github api rate limit (403 status code)
def create_github_session() -> requests.Session:
    s = requests.Session()
    retries = Retry(total=50, backoff_factor=10, status_forcelist=[403])
    s.mount('https://', HTTPAdapter(max_retries=retries))
    return s
