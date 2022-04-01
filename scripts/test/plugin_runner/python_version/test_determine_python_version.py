from pathlib import Path
from test.plugin_runner.python_version import SETUP_FILES_TEST_DATA_FOLDER

from plugin_runner.python_version.determine_python_version import _try_to_find_version_in_setup_file
from plugin_runner.python_version.python_classifiers import PythonVersion

import pytest

FIND_VERSION_IN_SETUP_FILE_TEST_DATA = [
    ('empty_file.py', None),
    ('without_python_classifiers.py', None),
    ('python_3_only_classifier_1.py', PythonVersion.PYTHON_3),
    ('python_3_only_classifier_2.py', PythonVersion.PYTHON_3),
    ('python_2_only_classifier_1.py', PythonVersion.PYTHON_2),
    ('python_2_only_classifier_2.py', PythonVersion.PYTHON_2),
    ('python_3_classifiers_1.py', PythonVersion.PYTHON_3),
    ('python_3_classifiers_2.py', PythonVersion.PYTHON_3),
    ('python_2_classifiers_1.py', PythonVersion.PYTHON_2),
    ('python_2_classifiers_2.py', PythonVersion.PYTHON_3),
]


@pytest.mark.parametrize(('file_name', 'expected_version'), FIND_VERSION_IN_SETUP_FILE_TEST_DATA)
def test_try_to_find_version_in_setup_file(file_name: Path, expected_version: PythonVersion):
    file_path = SETUP_FILES_TEST_DATA_FOLDER / file_name
    assert _try_to_find_version_in_setup_file(file_path) == expected_version
