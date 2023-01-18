import argparse
import os
from pathlib import Path

from utils.file_utils import get_file_content, write_to_file

LOCAL_PROPERTIES_FILE_NAME = 'local.properties'
SDK_PROPERTY_NAME = 'sdk.dir'


def get_new_sdk_path(sdk_path: str) -> str:
    return f'{SDK_PROPERTY_NAME}={sdk_path}'


def replace_sdk_path(file_path: str, sdk_path: str):
    new_lines = []
    for line in get_file_content(Path(file_path)).splitlines():
        if line.startswith(SDK_PROPERTY_NAME):
            new_lines.append(get_new_sdk_path(sdk_path))
        else:
            new_lines.append(line)
    write_to_file(file_path, os.linesep.join(new_lines))


def preprocess_dataset(dataset_path: str, sdk_path: str):
    projects = [name for name in os.listdir(dataset_path) if os.path.isdir(os.path.join(dataset_path, name))]
    for project in projects:
        full_path = os.path.join(dataset_path, project)
        properties_path = os.path.join(full_path, LOCAL_PROPERTIES_FILE_NAME)
        if os.path.exists(properties_path):
            replace_sdk_path(properties_path, sdk_path)
        else:
            write_to_file(properties_path, f'{get_new_sdk_path(sdk_path)}{os.linesep}')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('dataset_path', help='Path to folder with projects')
    parser.add_argument('sdk_path', help='Absolute path to Android sdk')

    args = parser.parse_args()
    preprocess_dataset(args.dataset_path, args.sdk_path)


if __name__ == '__main__':
    main()
