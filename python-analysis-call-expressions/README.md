# Python-Analysis call expressions

Functionality related to call expressions analysis in Python projects.

## Run the project

#### 1. Download list of repositories

You can use [seart-ghs.si.usi.ch]( https://seart-ghs.si.usi.ch/) (main language: Python) for this purpose. The file has
to be in csv format with ```name``` column, containing repositories' names in the following
format: ```username/project_name```

#### 2. Clean duplicated repositories

Run script for cleaning all duplicated repositories with different names:

``` shell script
python3 scripts/data_collection/clean_duplicates.py /path/to/csv_file/results.csv /path/to/cleaned/data/dir
```

You can use ```--save-metadata``` flag to download metadata about all projects. Metadata includes information about
repository's full name, owner, etc. This script makes requests to GitHub API, so you should add your GitHub Token to
environment variables (variable name is ```GITHUB_TOKEN```).

#### 3. Load dataset

Run the following command to download the dataset:

``` 
python3 scripts/data_collection/load_dataset.py /path/to/cleaned/data/dir/results.csv /path/to/dataset/dir --allowed-extensions py
```

#### 4. Set up a virtual environment (Optional)

Run the following command to set up a virtual environment:

``` 
python3 scripts/plugin_runner/create_venv.py /path/to/dataset/dir /path/to/venv
```

#### 5. Extract import directives full-qualified names from dataset

``` 
gradle :kotlin-analysis-plugin:python-cli -Prunner=python-call-expressions-analysis -Pinput=/path/to/dataset/dir -Poutput=path/to/results/dir -Pvenv=/path/to/venv
```

Note: if you don't want to run the analysis with a virtual environment, just pass an empty string as path. For example: `-Pvenv=""`

#### 6. Run statistics visualisation

**TODO**
