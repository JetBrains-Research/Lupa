import json
from typing import List

import requests
from dacite import from_dict

from data_collection.package_search.model import Package, PackageSearchResponse

PACKAGE_SEARCH_API_BASE_URL = "https://package-search.services.jetbrains.com/api"
PACKAGE_RANGE_REQUEST_DOMEN = "package?range="


def split_to_chunks(lst: List[str], n) -> List[str]:
    """Yield successive n-sized chunks from lst."""
    for i in range(0, len(lst), n):
        yield lst[i:i + n]


def get_packages(full_names: List[str]) -> List[Package]:
    """ Get packages for given package full names: group_id:artifact_id. """
    packages = []
    current_loaded, total_loaded = 0, len(full_names)
    for full_names_chunks in split_to_chunks(full_names, 20):
        try:
            full_names_range = ",".join(full_names_chunks)
            url = f"{PACKAGE_SEARCH_API_BASE_URL}/{PACKAGE_RANGE_REQUEST_DOMEN}{full_names_range}"
            response = requests.get(url)
            response = from_dict(data_class=PackageSearchResponse, data=json.loads(response.content))
            packages += response.packages
            current_loaded += len(response.packages)
            print(f"Got {current_loaded}/{total_loaded} packages")
        except Exception as e:
            print(f"Can not access package search {full_names_chunks}:", e)
    return packages
