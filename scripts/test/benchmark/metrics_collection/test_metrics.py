from pathlib import Path
from test.benchmark.metrics_collection import METRICS_TEST_DATA_FOLDER
from typing import Dict, List

from benchmark.metrics_collection.metrics import (
    FileSize,
    MetricArgument,
    NumberOfDependencies,
    NumberOfFiles,
    NumberOfLines,
)

import pytest

from utils.language import Language


COLLECT_NUMBER_OF_FILES_TEST_DATA = [
    ([], {}),
    (['unknown_extension.unk'], {Language.UNKNOWN: 1}),
    (['unknown_extension1.unk', 'unknown_extension2.unk', 'unknown_extension3.unk'], {Language.UNKNOWN: 3}),
    (['unknown_extension.unk1', 'unknown_extension.unk2', 'unknown_extension.unk3'], {Language.UNKNOWN: 3}),
    (['known_extension.py'], {Language.PYTHON: 1}),
    (['known_extension1.py', 'known_extension2.py', 'known_extension3.py'], {Language.PYTHON: 3}),
    (
        ['known_extension.py', 'known_extension.kt', 'known_extension.dockerfile'],
        {Language.PYTHON: 1, Language.KOTLIN: 1, Language.DOCKERFILE: 1},
    ),
]


@pytest.mark.parametrize(('files', 'expected_values'), COLLECT_NUMBER_OF_FILES_TEST_DATA)
def test_collect_number_of_files(files: List[str], expected_values: Dict[Language, int]):
    assert NumberOfFiles.collect_from_files([Path(file) for file in files]) == expected_values


COLLECT_NUMBER_OF_LINES_TEST_DATA = [
    ([], {}, {}),
    (['empty_file.txt'], {Language.TEXT: 0}, {}),
    (['one_line_file.txt'], {Language.TEXT: 1}, {}),
    (['multi_line_file.txt'], {Language.TEXT: 12}, {}),
    (['file_with_trailing_newline.txt'], {Language.TEXT: 2}, {}),
    (['file_with_non_utf8_encoding.txt'], {Language.TEXT: 4}, {}),
    (['requirements.txt'], {Language.TEXT: 6}, {}),
    (['build.gradle.kts'], {Language.KOTLIN: 84}, {}),
    (['one_line_comments.py'], {Language.PYTHON: 13}, {}),
    (['one_line_comments.py'], {Language.PYTHON: 11}, {MetricArgument.IGNORE_EMPTY_LINES.value: True}),
    (['one_line_comments.py'], {Language.PYTHON: 9}, {MetricArgument.IGNORE_COMMENTS.value: True}),
    (
        ['one_line_comments.py'],
        {Language.PYTHON: 7},
        {MetricArgument.IGNORE_COMMENTS.value: True, MetricArgument.IGNORE_EMPTY_LINES.value: True},
    ),
    (['one_line_comments.kt'], {Language.KOTLIN: 14}, {}),
    (['one_line_comments.kt'], {Language.KOTLIN: 13}, {MetricArgument.IGNORE_EMPTY_LINES.value: True}),
    (['one_line_comments.kt'], {Language.KOTLIN: 11}, {MetricArgument.IGNORE_COMMENTS.value: True}),
    (
        ['one_line_comments.kt'],
        {Language.KOTLIN: 10},
        {MetricArgument.IGNORE_COMMENTS.value: True, MetricArgument.IGNORE_EMPTY_LINES.value: True},
    ),
    (
        [
            'empty_file.txt',
            'one_line_file.txt',
            'multi_line_file.txt',
            'file_with_trailing_newline.txt',
            'file_with_non_utf8_encoding.txt',
            'one_line_comments.py',
            'one_line_comments.kt',
            'requirements.txt',
            'build.gradle.kts',
        ],
        {Language.TEXT: 25, Language.PYTHON: 13, Language.KOTLIN: 98},
        {},
    ),
]


@pytest.mark.parametrize(('files', 'expected_number_of_lines', 'config'), COLLECT_NUMBER_OF_LINES_TEST_DATA)
def test_collect_number_of_lines(files: List[str], expected_number_of_lines: Dict[Language, int], config: Dict):
    assert (
        NumberOfLines.collect_from_files([Path(METRICS_TEST_DATA_FOLDER / file) for file in files], **config)
        == expected_number_of_lines
    )


COLLECT_FILE_SIZE_TEST_DATA = [
    ([], {}),
    (['empty_file.txt'], {Language.TEXT: 0}),
    (['one_line_file.txt'], {Language.TEXT: 13}),
    (['multi_line_file.txt'], {Language.TEXT: 23}),
    (['file_with_trailing_newline.txt'], {Language.TEXT: 14}),
    (['file_with_non_utf8_encoding.txt'], {Language.TEXT: 50}),
    (['one_line_comments.py'], {Language.PYTHON: 347}),
    (['one_line_comments.kt'], {Language.KOTLIN: 297}),
    (['requirements.txt'], {Language.TEXT: 64}),
    (['build.gradle.kts'], {Language.KOTLIN: 2486}),
    (
        [
            'empty_file.txt',
            'one_line_file.txt',
            'multi_line_file.txt',
            'file_with_trailing_newline.txt',
            'file_with_non_utf8_encoding.txt',
            'one_line_comments.py',
            'one_line_comments.kt',
            'requirements.txt',
            'build.gradle.kts',
        ],
        {Language.TEXT: 164, Language.PYTHON: 347, Language.KOTLIN: 2783},
    ),
]


@pytest.mark.parametrize(('files', 'expected_size'), COLLECT_FILE_SIZE_TEST_DATA)
def test_collect_file_size(files: List[str], expected_size: Dict[Language, int]):
    assert FileSize.collect_from_files([Path(METRICS_TEST_DATA_FOLDER / file) for file in files]) == expected_size


COLLECT_NUMBER_OF_REQUIREMENTS = [
    ([], {}),
    (['empty_file.txt'], {}),
    (['empty_requirements.txt'], {Language.PYTHON: 0}),
    (['requirements.txt'], {Language.PYTHON: 3}),
    (['build.gradle.kts'], {Language.KOTLIN: 9}),  # 8 implementations + 1 false positive
    (
        [
            'empty_file.txt',
            'one_line_file.txt',
            'multi_line_file.txt',
            'file_with_trailing_newline.txt',
            'file_with_non_utf8_encoding.txt',
            'one_line_comments.py',
            'one_line_comments.kt',
            'requirements.txt',
            'build.gradle.kts',
        ],
        {Language.PYTHON: 3, Language.KOTLIN: 9},
    ),
]


@pytest.mark.parametrize(('files', 'expected_number'), COLLECT_NUMBER_OF_REQUIREMENTS)
def test_collect_number_of_dependencies(files: List[str], expected_number: Dict[Language, int]):
    assert (
        NumberOfDependencies.collect_from_files([Path(METRICS_TEST_DATA_FOLDER / file) for file in files])
        == expected_number
    )
