import tempfile
from distutils.version import Version
from pathlib import Path
from test.plugin_runner import CREATE_VENV_TEST_DATA_FOLDER
from typing import Dict, List, Optional, Set

import httpretty

import pkg_resources

from plugin_runner.python_venv.common import (
    PYPI_PACKAGE_METADATA_URL,
    Requirements,
    _get_available_versions,
    _normalize_requirement_name,
    create_requirements_file,
    filter_unavailable_packages,
    filter_unavailable_versions,
    gather_requirements,
    merge_requirements,
)

import pytest as pytest


@pytest.fixture
def _httpretty_fixture():
    httpretty.enable(allow_net_connect=False)
    yield
    httpretty.disable()


NORMALIZE_REQUIREMENT_NAME_TEST_DATA = [
    ('', ''),
    ('numpy', 'numpy'),
    ('PaNdAs', 'pandas'),
    ('sphinxcontrib.napoleon', 'sphinxcontrib-napoleon'),
    ('rUaMeL_yAmL', 'ruamel-yaml'),
    ('sOmE.lOnG_pAcKaGe-NaMe', 'some-long-package-name'),
]


@pytest.mark.parametrize(('given_name', 'expected_name'), NORMALIZE_REQUIREMENT_NAME_TEST_DATA)
def test_normalize_requirement_name(given_name: str, expected_name: str):
    assert _normalize_requirement_name(given_name) == expected_name


GATHER_REQUIREMENTS_TEST_DATA = [
    ('project_with_incorrect_requirement_file', {}),
    ('project_without_requirements_files', {}),
    ('project_with_empty_requirements_file', {}),
    (
        'project_with_several_requirements_files',
        {
            'numpy': set(zip(['==', '=='], map(pkg_resources.parse_version, ['1.2.3', '3.2.1']))),
            'pandas': set(zip(['==', '=='], map(pkg_resources.parse_version, ['4.5.6', '6.5.4']))),
            'plotly': set(),
        },
    ),
    (
        'project_with_nested_structure',
        {
            'numpy': set(zip(['==', '=='], map(pkg_resources.parse_version, ['1.2.3', '3.2.1']))),
            'pandas': set(zip(['==', '=='], map(pkg_resources.parse_version, ['4.5.6', '6.5.4']))),
            'plotly': set(),
        },
    ),
    # TODO: Add a test for nested requirements files (see: project_with_nested_requirements_files)
]


@pytest.mark.parametrize(('folder_name', 'expected_requirements'), GATHER_REQUIREMENTS_TEST_DATA)
def test_gather_requirements(folder_name: str, expected_requirements: Requirements):
    assert expected_requirements == gather_requirements(CREATE_VENV_TEST_DATA_FOLDER / folder_name)


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

    assert expected_versions == _get_available_versions(package_name)


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