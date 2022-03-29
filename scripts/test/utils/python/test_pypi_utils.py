from distutils.version import Version
from typing import Optional, Set

from httpretty import httpretty

import pkg_resources

import pytest

from utils.python.pypi_utils import PYPI_PACKAGE_METADATA_URL, check_package_exists, get_available_versions


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


CHECK_PACKAGE_EXISTS_TEST_DATA = [
    (
        200,
        True,
    ),
    (
        404,
        False,
    ),
    (
        300,
        None,
    ),
]


@pytest.mark.parametrize(('html_status', 'expected_existence'), CHECK_PACKAGE_EXISTS_TEST_DATA)
def test_check_package_exists(
    _httpretty_fixture,
    html_status: int,
    expected_existence: Optional[bool],
):
    httpretty.register_uri(
        httpretty.GET,
        PYPI_PACKAGE_METADATA_URL.format(package_name='some_package'),
        status=html_status,
    )

    assert expected_existence == check_package_exists('some_package')
