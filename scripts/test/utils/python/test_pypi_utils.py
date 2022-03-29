from distutils.version import Version
from typing import Set

import pkg_resources
import pytest
from httpretty import httpretty

from utils.python.pypi_utils import PYPI_PACKAGE_METADATA_URL, get_available_versions


@pytest.fixture
def _httpretty_fixture():
    httpretty.enable(allow_net_connect=False)
    yield
    httpretty.disable()


GET_AVAILABLE_VERSIONS_TEST_DATA = [
    (
        'numpy',
        """
        {
            "releases": {
                "1.2.3": {},
                "3.4.5": {},
                "6.7.8": {}
            }
        }
        """,
        set(map(pkg_resources.parse_version, ['1.2.3', '3.4.5', '6.7.8'])),
    ),
    (
        'numpy',
        """
        {
            "releases": {}
        }
        """,
        set(),
    ),
    (
        'numpy',
        'This is not a json.',
        set(),
    ),
    (
        'numpy',
        """
        {
            "incorrect_key": {
                "1.2.3": {},
                "3.4.5": {},
                "6.7.8": {}
            }
        }
        """,
        set(),
    ),
]


@pytest.mark.parametrize(('package_name', 'response_json', 'expected_versions'), GET_AVAILABLE_VERSIONS_TEST_DATA)
def test_get_available_versions(
    _httpretty_fixture,
    package_name: str,
    response_json: str,
    expected_versions: Set[Version],
):
    httpretty.register_uri(
        httpretty.GET,
        PYPI_PACKAGE_METADATA_URL.format(package_name=package_name),
        body=response_json,
    )

    assert expected_versions == get_available_versions(package_name)
