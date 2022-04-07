from pathlib import Path
from test.plugin_runner.python_version import SETUP_FILES_TEST_DATA_FOLDER
from typing import Optional, Set

from plugin_runner.python_version.determine_python_version import (
    _try_to_determine_version_using_classifiers,
    _try_to_find_version_in_setup_file,
)
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


DETERMINE_VERSION_USING_CLASSIFIERS_TEST_DATA = [
    (
        {
            'Programming Language :: Python :: 3',
            'Programming Language :: Python :: 3.7',
            'Programming Language :: Python :: 3.8',
            'Programming Language :: Python :: 3.9',
        },
        PythonVersion.PYTHON_3,
    ),
    (
        {
            'Programming Language :: Python :: 2',
            'Programming Language :: Python :: 2.7',
        },
        PythonVersion.PYTHON_2,
    ),
    (
        {
            'Programming Language :: Python :: 2',
            'Programming Language :: Python :: 2.7',
            'Programming Language :: Python :: 3',
            'Programming Language :: Python :: 3.7',
            'Programming Language :: Python :: 3.8',
            'Programming Language :: Python :: 3.9',
        },
        None,
    ),
    (
        {
            'Operating System :: POSIX',
            'Topic :: Software Development',
            'Programming Language :: Python',
            'License :: OSI Approved',
        },
        None,
    ),
    (set(), None),
]


@pytest.mark.parametrize(('classifiers', 'expected_versions'), DETERMINE_VERSION_USING_CLASSIFIERS_TEST_DATA)
def test_try_to_determine_version_using_classifiers(classifiers: Set[str], expected_versions: Optional[PythonVersion]):
    assert _try_to_determine_version_using_classifiers(classifiers) == expected_versions
