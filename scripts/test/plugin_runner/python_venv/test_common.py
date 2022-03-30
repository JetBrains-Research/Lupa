import tempfile
from distutils.version import Version
from pathlib import Path
from typing import Dict, List, Optional

import httpretty

import pkg_resources

from plugin_runner.python_venv.common import (
    Requirements,
    create_requirements_file,
    filter_unavailable_packages,
    filter_unavailable_versions,
    merge_requirements,
)

import pytest as pytest

from utils.python.pypi_utils import PYPI_PACKAGE_METADATA_URL


@pytest.fixture
def _httpretty_fixture():
    httpretty.enable(allow_net_connect=False)
    yield
    httpretty.disable()


FILTER_UNAVAILABLE_PACKAGES_TEST_DATA = [
    (
        {'numpy': 200, 'pandas': 200},
        {
            'numpy': {('==', pkg_resources.parse_version('1.2.3'))},
            'pandas': {('==', pkg_resources.parse_version('4.5.6'))},
        },
        {
            'numpy': {('==', pkg_resources.parse_version('1.2.3'))},
            'pandas': {('==', pkg_resources.parse_version('4.5.6'))},
        },
    ),
    (
        {'numpy': 404, 'pandas': 200},
        {
            'numpy': {('==', pkg_resources.parse_version('1.2.3'))},
            'pandas': {('==', pkg_resources.parse_version('4.5.6'))},
        },
        {
            'pandas': {('==', pkg_resources.parse_version('4.5.6'))},
        },
    ),
    (
        {'numpy': 404, 'pandas': 404},
        {
            'numpy': {('==', pkg_resources.parse_version('1.2.3'))},
            'pandas': {('==', pkg_resources.parse_version('4.5.6'))},
        },
        {},
    ),
    (
        {'numpy': 505},  # Unexpected code
        {'numpy': {('==', pkg_resources.parse_version('1.2.3'))}},
        {'numpy': {('==', pkg_resources.parse_version('1.2.3'))}},
    ),
]


@pytest.mark.parametrize(
    ('status_code_by_package_name', 'requirements', 'expected_requirements'),
    FILTER_UNAVAILABLE_PACKAGES_TEST_DATA,
)
def test_filter_unavailable_packages(
    _httpretty_fixture,
    status_code_by_package_name: Dict[str, int],
    requirements: Requirements,
    expected_requirements: Requirements,
):
    for package_name, status_code in status_code_by_package_name.items():
        httpretty.register_uri(
            httpretty.GET,
            PYPI_PACKAGE_METADATA_URL.format(package_name=package_name),
            status=status_code,
        )

    assert expected_requirements == filter_unavailable_packages(requirements)


FILTER_UNAVAILABLE_VERSIONS_TEST_DATA = [
    (
        {
            'numpy': """
            {
                "releases": {
                    "1.2.3": {},
                    "3.2.1": {}
                }
            }
            """,
            'pandas': """
            {
                "releases": {
                    "4.5.6": {},
                    "6.5.4": {}
                }
            }
            """,
        },
        {
            'numpy': set(zip(['==', '=='], map(pkg_resources.parse_version, ['1.2.3', '3.2.1']))),
            'pandas': set(zip(['==', '=='], map(pkg_resources.parse_version, ['4.5.6', '6.5.4']))),
        },
        {
            'numpy': set(zip(['==', '=='], map(pkg_resources.parse_version, ['1.2.3', '3.2.1']))),
            'pandas': set(zip(['==', '=='], map(pkg_resources.parse_version, ['4.5.6', '6.5.4']))),
        },
    ),
    (
        {
            'numpy': """
        {
            "releases": {
                "1.2.3": {}
            }
        }
        """,
            'pandas': """
        {
            "releases": {
                "4.5.6": {},
                "6.5.4": {}
            }
        }
        """,
        },
        {
            'numpy': set(zip(['==', '=='], map(pkg_resources.parse_version, ['1.2.3', '3.2.1']))),
            'pandas': set(zip(['==', '=='], map(pkg_resources.parse_version, ['4.5.6', '6.5.4']))),
        },
        {
            'numpy': {('==', pkg_resources.parse_version('1.2.3'))},
            'pandas': set(zip(['==', '=='], map(pkg_resources.parse_version, ['4.5.6', '6.5.4']))),
        },
    ),
    (
        {
            'numpy': """
        {
            "releases": {
                "1.2.3": {},
                "3.2.1": {}
            }
        }
        """,
            'pandas': """
        {
            "releases": {
                "4.5.6": {},
                "6.5.4": {}
            }
        }
        """,
        },
        {
            'numpy': set(zip(['==', '=='], map(pkg_resources.parse_version, ['1.2.3', '3.2.1']))),
            'pandas': set(zip(['==', '=='], map(pkg_resources.parse_version, ['7.8.9', '9.8.7']))),
        },
        {
            'numpy': set(zip(['==', '=='], map(pkg_resources.parse_version, ['1.2.3', '3.2.1']))),
            'pandas': set(),
        },
    ),
    (
        {
            'numpy': """
        {
            "releases": {
                "1.2.3": {},
                "3.2.1": {}
            }
        }
        """,
            'pandas': """
        {
            "releases": {}
        }
        """,
        },
        {
            'numpy': set(zip(['==', '=='], map(pkg_resources.parse_version, ['1.2.3', '3.2.1']))),
            'pandas': set(zip(['==', '=='], map(pkg_resources.parse_version, ['4.5.6', '6.5.4']))),
        },
        {
            'numpy': set(zip(['==', '=='], map(pkg_resources.parse_version, ['1.2.3', '3.2.1']))),
            'pandas': set(zip(['==', '=='], map(pkg_resources.parse_version, ['4.5.6', '6.5.4']))),
        },
    ),
]


@pytest.mark.parametrize(
    ('json_response_by_package_name', 'requirements', 'expected_requirements'),
    FILTER_UNAVAILABLE_VERSIONS_TEST_DATA,
)
def test_filter_unavailable_versions(
    _httpretty_fixture,
    json_response_by_package_name: Dict[str, str],
    requirements: Requirements,
    expected_requirements: Requirements,
):
    for package_name, json_response in json_response_by_package_name.items():
        httpretty.register_uri(
            httpretty.GET,
            PYPI_PACKAGE_METADATA_URL.format(package_name=package_name),
            body=json_response,
        )

    assert expected_requirements == filter_unavailable_versions(requirements)


MERGE_REQUIREMENTS_TEST_DATA = [
    (
        {
            'numpy': set(zip(['==', '=='], map(pkg_resources.parse_version, ['1.2.3', '3.2.1']))),
            'pandas': set(zip(['==', '=='], map(pkg_resources.parse_version, ['4.5.6', '6.5.4']))),
        },
        {
            'numpy': pkg_resources.parse_version('3.2.1'),
            'pandas': pkg_resources.parse_version('6.5.4'),
        },
    ),
    (
        {
            'numpy': set(),
            'pandas': set(zip(['==', '=='], map(pkg_resources.parse_version, ['4.5.6', '6.5.4']))),
        },
        {
            'numpy': None,
            'pandas': pkg_resources.parse_version('6.5.4'),
        },
    ),
]


@pytest.mark.parametrize(
    ('requirements', 'expected_version_by_package_name'),
    MERGE_REQUIREMENTS_TEST_DATA,
)
def test_merge_requirements(
    requirements: Requirements,
    expected_version_by_package_name: Dict[str, Optional[Version]],
):
    assert expected_version_by_package_name == merge_requirements(requirements)


CREATE_REQUIREMENTS_FILES_TEST_DATA = [
    (
        {
            'numpy': pkg_resources.parse_version('3.2.1'),
            'pandas': pkg_resources.parse_version('6.5.4'),
        },
        [
            'numpy==3.2.1',
            'pandas==6.5.4',
        ],
    ),
    (
        {
            'numpy': None,
            'pandas': pkg_resources.parse_version('6.5.4'),
        },
        [
            'numpy',
            'pandas==6.5.4',
        ],
    ),
]


@pytest.mark.parametrize(('version_by_package_name', 'expected_lines'), CREATE_REQUIREMENTS_FILES_TEST_DATA)
def test_create_requirements_file(version_by_package_name: Dict[str, Optional[Version]], expected_lines: List[str]):
    with tempfile.TemporaryDirectory() as temp_dir:
        requirements_file_path = create_requirements_file(version_by_package_name, Path(temp_dir))
        with open(requirements_file_path) as requirements_file:
            actual_lines = list(map(str.strip, requirements_file.readlines()))
            assert actual_lines == expected_lines
