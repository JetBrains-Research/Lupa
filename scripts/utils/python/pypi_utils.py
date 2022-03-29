import json
import logging
from distutils.version import Version
from typing import Set

from pkg_resources import parse_version

import requests
from requests.adapters import HTTPAdapter

from urllib3 import Retry

PYPI_PACKAGE_METADATA_URL = 'https://pypi.org/pypi/{package_name}/json'

logger = logging.getLogger(__name__)


def _create_session() -> requests.Session:
    session = requests.Session()
    retries = Retry(total=10, backoff_factor=0.1)
    session.mount('https://', HTTPAdapter(max_retries=retries))
    return session


def get_available_versions(package_name: str) -> Set[Version]:
    """
    By a given package, collect a list of all the versions available on PyPI.

    :param package_name: PyPI package name.
    :return: Set of available versions. If the version could not be obtained, None will be returned.
    """
    session = _create_session()
    url = PYPI_PACKAGE_METADATA_URL.format(package_name=package_name)

    try:
        metadata = session.get(url).json()
    except requests.exceptions.RequestException:
        logger.error('An error occurred when accessing the PyPI.')
        return set()
    except json.JSONDecodeError:
        logger.error(f'Failed to get a version for the {package_name} package.')
        return set()

    if 'releases' in metadata.keys():
        versions = metadata['releases'].keys()
    else:
        logger.error('The PyPI response does not contain the "releases" field.')
        return set()

    return set(map(parse_version, versions))


def check_package_exists(package_name: str) -> bool:
    """
    Check if a package exists.

    :param package_name: PyPI package name.
    :return: True if the package exists, otherwise False.
    """
    session = _create_session()
    url = PYPI_PACKAGE_METADATA_URL.format(package_name=package_name)

    try:
        response = session.get(url)
    except requests.exceptions.RequestException:
        logger.error('An error occurred when accessing the PyPI.')
        return False

    if response.status_code == 200:
        return True

    if response.status_code == 404:
        return False

    logger.warning(f'PyPI returned an unexpected code ({response.status_code}).')
    return False
