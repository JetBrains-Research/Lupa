import os


def create_directory(path: str):
    if not os.path.exists(path):
        os.makedirs(path)
