import enum
import os
import shutil
from enum import Enum, unique
from pathlib import Path
from typing import Callable, List, Union


class Extensions(str, enum.Enum):
    CSV = 'csv'
    TXT = 'txt'
    JSON = 'json'
    PNG = 'png'


def create_directory(path: Union[str, Path]) -> None:
    if not os.path.exists(path):
        os.makedirs(path)


def clear_directory(dir_path: Union[str, Path]) -> None:
    if not os.path.exists(dir_path):
        return

    for files in os.listdir(dir_path):
        path = os.path.join(dir_path, files)
        try:
            shutil.rmtree(path)
        except OSError:
            os.remove(path)


def get_subdirectories(path: Union[str, Path]) -> List[str]:
    dirs = []
    for name in os.listdir(path):
        fullname = os.path.join(path, name)
        if os.path.isdir(fullname):
            dirs.append(fullname)
    return dirs


def get_file_lines(path: Union[str, Path]) -> List[str]:
    with open(path) as fin:
        return fin.readlines()


def write_to_file(path: Union[str, Path], content: str, mode: str = 'w+') -> None:
    with open(path, mode) as file:
        file.write(content)


@unique
class FileSystemItem(Enum):
    PATH = 0
    SUBDIR = 1
    FILE = 2


def get_all_file_system_items(
    root: Union[str, Path],
    item_condition: Callable[[str], bool] = lambda name: True,
    item_type: FileSystemItem = FileSystemItem.FILE,
    with_subdirs: bool = True,
) -> List[Path]:
    """
    Return the paths to all file system items from the root that satisfy the condition.

    :param root: Path to the folder where to find the file system items.
    :param item_condition: Predicate that the file system items must satisfy.
    :param item_type: Type of file system items to be processed.
    :param with_subdirs: Whether it is necessary to process items in subdirectories.

    :raises ValueError: If root is not a directory.

    :return: List of paths.
    """
    if item_type == FileSystemItem.PATH:
        # FileSystemItem.PATH cannot be used, due to the meaninglessness of the result.
        raise ValueError(f'Only {FileSystemItem.FILE} or {FileSystemItem.SUBDIR} are allowed.')

    if not os.path.isdir(root):
        raise ValueError(f'The {root} is not a directory.')

    items = []
    for fs_tuple in os.walk(root):
        for item in fs_tuple[item_type.value]:
            if item_condition(item):
                items.append(Path(os.path.join(fs_tuple[FileSystemItem.PATH.value], item)))

        if not with_subdirs:
            break

    return items


def get_file_content(file_path: Union[str, Path]) -> str:
    """
    Get the content of the file.

    :param file_path: Path to the file you want to read.
    :return: File content.
    """
    with open(file_path, encoding='utf-8', errors='ignore') as file:
        return file.read()


def get_files_by_name(dir_path: Union[str, Path], file_name: str) -> List[str]:
    file_paths = []
    for root, dirs, files in os.walk(dir_path):
        for file in files:
            file_path = os.path.join(root, file)
            if os.path.isfile(file_path) and os.path.basename(file_path) == file_name:
                file_paths.append(file_path)
    return file_paths
