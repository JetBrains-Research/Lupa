from test.benchmark.metrics_collection import METRICS_TEST_DATA_FOLDER

from benchmark.metrics_collection.metrics import _get_file_size, _get_number_of_files, _get_number_of_lines

import pytest

GET_NUMBER_OF_FILES_TEST_DATA = [
    'non_existent_file.txt',
    'empty_file.txt',
    'one_line_file.txt',
    'multi_line_file.txt',
    'file_with_trailing_newline.txt',
    'file_with_non_utf8_encoding',
]


@pytest.mark.parametrize('file', GET_NUMBER_OF_FILES_TEST_DATA)
def test_get_number_of_files(file: str):
    assert _get_number_of_files(METRICS_TEST_DATA_FOLDER / file) == 1


GET_NUMBER_OF_LINES_TEST_DATA = [
    ('non_existent_file.txt', 0),
    ('empty_file.txt', 0),
    ('one_line_file.txt', 1),
    ('multi_line_file.txt', 12),
    ('file_with_trailing_newline.txt', 2),
    ('file_with_non_utf8_encoding.txt', 4),
]


@pytest.mark.parametrize(('file', 'expected_number_of_lines'), GET_NUMBER_OF_LINES_TEST_DATA)
def test_get_number_of_lines(file: str, expected_number_of_lines: int):
    assert _get_number_of_lines(METRICS_TEST_DATA_FOLDER / file) == expected_number_of_lines


GET_FILE_SIZE_TEST_DATA = [
    ('non_existent_file.txt', 0),
    ('empty_file.txt', 0),
    ('one_line_file.txt', 13),
    ('multi_line_file.txt', 23),
    ('file_with_trailing_newline.txt', 14),
    ('file_with_non_utf8_encoding.txt', 50),
]


@pytest.mark.parametrize(('file', 'expected_size'), GET_FILE_SIZE_TEST_DATA)
def test_get_file_size(file: str, expected_size: int):
    assert _get_file_size(METRICS_TEST_DATA_FOLDER / file) == expected_size
