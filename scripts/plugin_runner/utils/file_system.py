import os
from enum import Enum, unique
from pathlib import Path
from typing import Callable, List


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
