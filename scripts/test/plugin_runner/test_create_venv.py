from distutils.version import Version
from typing import Dict, Set, Tuple

import pkg_resources
import pytest as pytest

from plugin_runner.create_venv import gather_requirements
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
