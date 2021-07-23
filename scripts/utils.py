import enum
import os
from typing import List


class Extensions(str, enum.Enum):
    CSV = "csv"
    TXT = "txt"
    JSON = "json"
    PNG = "png"


def create_directory(path: str):
    if not os.path.exists(path):
        os.makedirs(path)


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
