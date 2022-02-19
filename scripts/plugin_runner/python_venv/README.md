# Python virtual environment creation scripts

This module contains scripts for creating a python virtual environment.

## Shared virtual environment

This script allows to create a shared virtual environment for all projects from a dataset. 
To do this, requirements are gathered from each project, which are then merged.
In the current version of the script, if we find different versions of the same library in different requirement files, we will choose the newest (largest) version.

### Usage

Run the [`create_shared_venv.py`](./create_shared_venv.py) with the arguments from command line.

**Required arguments:**
- `dataset_path` — Path to dataset with projects from which you want to get requirements and create a virtual environment.
- `venv_path` — Path to the folder where you want to create the virtual environment.

**Optional arguments:**
| Argument                                                   | Description                                                                                                                                         |
|------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| &#8209;&#8209;no&#8209;package&#8209;name&#8209;validation | If specified, no package name validation will be performed using PyPI.                                                                              |
| &#8209;&#8209;no&#8209;version&#8209;validation            | If specified, no version validation will be performed using PyPI.                                                                                   |
| &#8209;&#8209;no&#8209;package&#8209;dependencies          | If specified, no dependencies will be installed for each package (the `--no-deps` flag will be passed to pip).                                      |
| &#8209;&#8209;pip&#8209;for&#8209;each                     | Call `pip install` for each requirement individually. By default, `pip install` will be applied to the entire file with the collected requirements. |

## Virtual environment for each project
This script allows to create an individual virtual environment for each project from a dataset. 
The virtual environment is created in the root of the project in the `.venv` folder.

### Usage

Run the [`create_venv_for_each_project.py`](./create_venv_for_each_project.py) with the arguments from command line.

**Required arguments:**
- `dataset_path` — Path to the dataset with projects, for each of which a virtual environment must be created.

**Optional arguments:**
| Argument                                          | Description                                                                                                                                         |
|---------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| &#8209;&#8209;no&#8209;package&#8209;dependencies | If specified, no dependencies will be installed for each package (the `--no-deps` flag will be passed to pip).                                      |
| &#8209;&#8209;pip&#8209;for&#8209;each            | Call `pip install` for each requirement individually. By default, `pip install` will be applied to the entire file with the collected requirements. |
