import os


def create_directory(path):
    if not os.path.exists(path):
        os.makedirs(path)
