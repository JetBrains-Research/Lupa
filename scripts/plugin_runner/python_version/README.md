# Python version detection script

This module contains a script to determine the versions of Python used in projects from the dataset.

## Usage

Run the [`determine_python_version.py`](./determine_python_version.py) with the arguments from command line.

**Required arguments:**
- `dataset_path` — Path to the dataset with the projects for which you want to determine the python version used.
- `output_path` — Path where you want to save the csv table with the versions.

**Optional arguments:**
| Argument                           | Description                                                                                  |
|------------------------------------|----------------------------------------------------------------------------------------------|
| **&#8209;&#8209;log&#8209;output** | Path to the file where you want to save the logs. By default, the logs are output to stdout. |
