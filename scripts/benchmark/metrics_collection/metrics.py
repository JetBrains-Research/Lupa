from enum import Enum, unique
from os.path import getsize
from pathlib import Path
from typing import Set


@unique
class Metric(Enum):
    NUMBER_OF_FILES = 'number_of_files'
    NUMBER_OF_LINES = 'number_of_lines'
    FILE_SIZE = 'file_size'

    def execute(self, file: Path) -> int:
        return _METRIC_TO_FUNCTION[self](file)

    @classmethod
    def values(cls) -> Set[str]:
        return {metric.value for metric in cls}


def _get_number_of_files(file: Path) -> int:
    """
    Get number of files.

    This function always returns 1 regardless of the value of the ``file`` argument.
    This is necessary for compatibility with other metric functions that work with the contents of the file.

    :param file: Is not used.
    :return: Always 1.
    """
    return 1


def _get_number_of_lines(file: Path) -> int:
    """
    Get number of lines from ``file``.

    If the file does not exist, zero will be returned. If the file contains errors, they will be ignored.

    :param file: Path to the file.
    :return: Number of lines.
    """
    if not file.exists():
        return 0

    with open(file, encoding='utf-8', mode='r', errors='ignore') as file:
        return sum(1 for _ in file)


def _get_file_size(file: Path) -> int:
    """
    Get the size, in bytes, of ``file``.

    If the file does not exist, zero will be returned.

    :param file: Path to the file.
    :return: File size in bytes.
    """
    if not file.exists():
        return 0

    return getsize(file)


_METRIC_TO_FUNCTION = {
    Metric.NUMBER_OF_FILES: _get_number_of_files,
    Metric.NUMBER_OF_LINES: _get_number_of_lines,
    Metric.FILE_SIZE: _get_file_size,
}
