import enum
import os
import shutil
from enum import Enum, unique
from pathlib import Path

from typing import List, Callable


class Extensions(str, enum.Enum):
    CSV = "csv"
    TXT = "txt"
    JSON = "json"
    PNG = "png"


def create_directory(path: str):
    if not os.path.exists(path):
        os.makedirs(path)


def clear_directory(dir_path: str):
    for files in os.listdir(dir_path):
        path = os.path.join(dir_path, files)
        try:
            shutil.rmtree(path)
        except OSError:
            os.remove(path)


def get_subdirectories(path: str) -> List[str]:
    dirs = []
    for name in os.listdir(path):
        fullname = os.path.join(path, name)
        if os.path.isdir(fullname):
            dirs.append(fullname)
    return dirs


def get_file_lines(path: str) -> List[str]:
    with open(path) as fin:
        return fin.readlines()


def write_to_file(path: str, content: str):
    with open(path, "w+") as file:
        file.write(content)


@unique
class FileSystemItem(Enum):
    PATH = 0
    SUBDIR = 1
    FILE = 2


def get_all_file_system_items(
        root: Path,
        item_condition: Callable[[str], bool] = lambda name: True,
        item_type: FileSystemItem = FileSystemItem.FILE,
) -> List[Path]:
    """
    Returns the paths to all file system items from the root that satisfy the condition.

    :param root: Path to the folder where to find the file system items.
    :param item_condition: Predicate that the file system items must satisfy.
    :param item_type: Type of file system items to be processed.
    :return: List of paths.
    """

    if not root.is_dir():
        raise ValueError(f'The {root} is not a directory.')

    items = []
    for fs_tuple in os.walk(root):
        for item in fs_tuple[item_type.value]:
            if item_condition(item):
                items.append(Path(os.path.join(fs_tuple[FileSystemItem.PATH.value], item)))
    return items
