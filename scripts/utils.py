import os
from typing import List


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
