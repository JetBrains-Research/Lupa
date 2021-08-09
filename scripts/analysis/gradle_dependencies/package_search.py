from typing import Optional, Dict, List
import json
import requests
import pandas as pd

PACKAGE_SEARCH_API_BASE_URL = "https://package-search.services.jetbrains.com/api"
PACKAGE_RANGE_REQUEST_DOMEN = "package?range="


def split_to_chunks(lst: List[str], n) -> List[str]:
    """Yield successive n-sized chunks from lst."""
    for i in range(0, len(lst), n):
        yield lst[i:i + n]


def get_urls_by_full_names(full_names: List[str]) -> Dict[str, Optional[str]]:
    url_by_full_name = {}
    for full_names_chunks in split_to_chunks(full_names, 20):
        try:
            full_names_range = ",".join(full_names_chunks)
            url = f"{PACKAGE_SEARCH_API_BASE_URL}/{PACKAGE_RANGE_REQUEST_DOMEN}{full_names_range}"
            response = requests.get(url)
            packages = json.loads(response.content)["packages"]
            for package_info in packages:
                full_name = f"{package_info['group_id']}:{package_info['artifact_id']}"
                url = package_info['scm']['url'] if 'scm' in package_info and 'url' in package_info['scm'] \
                    else package_info['url'] if 'url' in package_info \
                    else None
                url_by_full_name[full_name] = url
                print(f"Got package url for {full_name}: {url}")
        except Exception as e:
            print(f"Can not access package search {full_names_chunks}:", e)
    return url_by_full_name


def full_names_to_urls_csv(path_to_stats):
    data = pd.read_csv(path_to_stats)

    full_names = list(set([f"{group}:{name}" for group, name in data["project_name"].values]))
    url_by_full_name = get_urls_by_full_names(full_names)

    pd.DataFrame.from_dict({
        "full_name": url_by_full_name.keys(),
        "url": url_by_full_name.values()
    }).to_csv(f"url_{path_to_stats}", indexed=False)


if __name__ == '__main__':
    full_names_to_urls_csv("data/gradle_dependencies_data.csv")
