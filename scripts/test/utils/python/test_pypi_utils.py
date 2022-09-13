from distutils.version import Version
from typing import Optional, Set

from httpretty import httpretty

import pkg_resources

import pytest

from utils.python.pypi_utils import (
    PYPI_PACKAGE_METADATA_URL,
    PYPI_VERSION_METADATA_URL,
    check_package_exists,
    get_available_versions,
    get_package_classifiers,
)


@pytest.fixture
def _httpretty_fixture():
    httpretty.enable(allow_net_connect=False)
    yield
    httpretty.disable()


GET_AVAILABLE_VERSIONS_TEST_DATA = [
    (
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
        """
        {
            "releases": {}
        }
        """,
        set(),
    ),
    (
        'This is not a json.',
        set(),
    ),
    (
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


@pytest.mark.parametrize(('response_json', 'expected_versions'), GET_AVAILABLE_VERSIONS_TEST_DATA)
def test_get_available_versions(
    _httpretty_fixture,
    response_json: str,
    expected_versions: Set[Version],
):
    httpretty.register_uri(
        httpretty.GET,
        PYPI_PACKAGE_METADATA_URL.format(package_name='some_package'),
        body=response_json,
    )

    assert expected_versions == get_available_versions('some_package')


GET_PACKAGE_CLASSIFIERS_TEST_DATA = [
    (
        """
        {
            "info": {
                "classifiers": [
                    "Programming Language :: Python :: 2",
                    "Programming Language :: Python :: 3",
                    "Programming Language :: Python :: 3.7",
                    "Programming Language :: Python :: 3.8"
                ]
            }
        }
        """,
        '1.0.0',
        {
            'Programming Language :: Python :: 2',
            'Programming Language :: Python :: 3',
            'Programming Language :: Python :: 3.7',
            'Programming Language :: Python :: 3.8',
        },
    ),
    (
        """
        {
            "info": {
                "classifiers": []
            }
        }
        """,
        '1.0.0',
        set(),
    ),
    (
        'This is not a json.',
        '1.0.0',
        set(),
    ),
    (
        """
        {
            "incorrect_key": {
                "classifiers": [
                    "Programming Language :: Python :: 2",
                    "Programming Language :: Python :: 3",
                    "Programming Language :: Python :: 3.7",
                    "Programming Language :: Python :: 3.8"
                ]
            }
        }
        """,
        '1.0.0',
        set(),
    ),
    (
        """
        {
            "info": {
                "incorrect_key": [
                    "Programming Language :: Python :: 2",
                    "Programming Language :: Python :: 3",
                    "Programming Language :: Python :: 3.7",
                    "Programming Language :: Python :: 3.8"
                ]
            }
        }
        """,
        '1.0.0',
        set(),
    ),
    (
        """
        {
            "info": {
                "classifiers": [
                    "Programming Language :: Python :: 2",
                    "Programming Language :: Python :: 3",
                    "Programming Language :: Python :: 3.7",
                    "Programming Language :: Python :: 3.8"
                ]
            }
        }
        """,
        None,
        {
            'Programming Language :: Python :: 2',
            'Programming Language :: Python :: 3',
            'Programming Language :: Python :: 3.7',
            'Programming Language :: Python :: 3.8',
        },
    ),
    (
        """
        {
            "info": {
                "classifiers": []
            }
        }
        """,
        None,
        set(),
    ),
    (
        'This is not a json.',
        None,
        set(),
    ),
    (
        """
        {
            "incorrect_key": {
                "classifiers": [
                    "Programming Language :: Python :: 2",
                    "Programming Language :: Python :: 3",
                    "Programming Language :: Python :: 3.7",
                    "Programming Language :: Python :: 3.8"
                ]
            }
        }
        """,
        None,
        set(),
    ),
    (
        """
        {
            "info": {
                "incorrect_key": [
                    "Programming Language :: Python :: 2",
                    "Programming Language :: Python :: 3",
                    "Programming Language :: Python :: 3.7",
                    "Programming Language :: Python :: 3.8"
                ]
            }
        }
        """,
        None,
        set(),
    ),
]


@pytest.mark.parametrize(
    ('response_json', 'package_version', 'expected_classifiers'),
    GET_PACKAGE_CLASSIFIERS_TEST_DATA,
)
def test_get_package_classifiers(
    _httpretty_fixture,
    response_json: str,
    package_version: Optional[str],
    expected_classifiers: Set[str],
):
    uri = (
        PYPI_PACKAGE_METADATA_URL.format(package_name='some_package')
        if package_version is None
        else PYPI_VERSION_METADATA_URL.format(package_name='some_package', package_version=package_version)
    )
    httpretty.register_uri(httpretty.GET, uri=uri, body=response_json)
    assert expected_classifiers == get_package_classifiers('some_package', package_version)


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
