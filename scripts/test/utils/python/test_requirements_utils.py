import pkg_resources
import pytest

from test.utils.python import REQUIREMENTS_UTILS_TEST_DATA_FOLDER
from utils.python.requirements_utils import Requirements, gather_requirements, normalize_requirement_name

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
    assert normalize_requirement_name(given_name) == expected_name


GATHER_REQUIREMENTS_TEST_DATA_FOLDER = REQUIREMENTS_UTILS_TEST_DATA_FOLDER / 'projects_with_requirements_files'


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
    assert expected_requirements == gather_requirements(GATHER_REQUIREMENTS_TEST_DATA_FOLDER / folder_name)
