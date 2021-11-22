from distutils.version import Version
from typing import Dict, Set, Tuple

import httpretty
import pkg_resources
import pytest as pytest

from plugin_runner.create_venv import PYPI_PACKAGE_METADATA_URL, filter_unavailable_packages, gather_requirements
from test.plugin_runner import CREATE_VENV_TEST_FOLDER

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
]


@pytest.mark.parametrize(('folder_name', 'expected_requirements_by_package_name'), GATHER_REQUIREMENTS_TEST_DATA)
def test_gather_requirements(
    folder_name: str,
    expected_requirements_by_package_name: Dict[str, Set[Tuple[str, Version]]],
):
    actual_requirements_by_package_name = gather_requirements(CREATE_VENV_TEST_FOLDER / folder_name)
    assert actual_requirements_by_package_name == expected_requirements_by_package_name


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
    ('status_code_by_package_name', 'original_requirements_by_package_name', 'expected_requirements_by_package_name'),
    FILTER_UNAVAILABLE_PACKAGES_TEST_DATA,
)
def test_filter_unavailable_packages(
    status_code_by_package_name: Dict[str, int],
    original_requirements_by_package_name: Dict[str, Set[Tuple[str, Version]]],
    expected_requirements_by_package_name: Dict[str, Set[Tuple[str, Version]]],
):
    httpretty.enable(allow_net_connect=False)
    for package_name, status_code in status_code_by_package_name.items():
        httpretty.register_uri(
            httpretty.GET,
            PYPI_PACKAGE_METADATA_URL.format(package_name=package_name),
            status=status_code,
        )
    assert expected_requirements_by_package_name == filter_unavailable_packages(original_requirements_by_package_name)
    httpretty.disable()
