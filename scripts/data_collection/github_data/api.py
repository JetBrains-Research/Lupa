import os
from typing import Optional

from github import Github
from requests.packages.urllib3.util.retry import Retry
from github.Repository import Repository


def get_github_token() -> str:
    os.environ['GIT_TERMINAL_PROMPT'] = '0'
    token = os.getenv('GITHUB_TOKEN')
    return token


def get_repo(repo_name: str) -> Optional[Repository]:
    token = get_github_token()
    github = Github(token, retry=Retry(total=50, backoff_factor=10, status_forcelist=[403]))
    repo = github.get_repo(repo_name)
    return repo
