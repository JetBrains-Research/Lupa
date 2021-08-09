import json
import re
from typing import Optional, Dict, List

import pandas as pd
import requests

GIT_API_BASE_URL = "https://api.github.com"
REPOS_REQUEST_DOMEN = "repos"


def get_languages_by_repo_names(repo_names: List[str]) -> Dict[str, Optional[str]]:
    language_by_full_name = {}
    for repo_name in repo_names:
        if repo_name is None:
            continue
        try:
            url = f"{GIT_API_BASE_URL}/{REPOS_REQUEST_DOMEN}/{repo_name}"
            response = requests.get(url)
            content = json.loads(response.content)
            if "language" in content:
                language_by_full_name[repo_name] = content["language"]
                print(f"Got package url for {repo_name}: {url}")
        except Exception as e:
            print(f"Can not access git api {repo_name}:", e)
    return language_by_full_name


def url_to_repo_name(url: str) -> Optional[str]:
    result = re.search('https://github.com/(.*)', url)
    return None if result is None or len(result.groups()) == 0 else result.group(1)


def project_names_to_language(path_to_stats):
    data = pd.read_csv(path_to_stats)

    repo_names = [url_to_repo_name(url) for url in data["url"].values]
    language_by_repo_name = get_languages_by_repo_names(repo_names)
    languages = [
        language_by_repo_name[repo_name] if repo_name is not None and repo_name in language_by_repo_name else None
        for repo_name in repo_names]

    pd.DataFrame.from_dict({
        "full_name": data["full_name"].tolist(),
        "tag": data["url"].tolist(),
        "language": languages
    }).to_csv(f"data/projects_info", index=False)


if __name__ == '__main__':
    project_names_to_language("data/full_name_to_url_data.csv")
