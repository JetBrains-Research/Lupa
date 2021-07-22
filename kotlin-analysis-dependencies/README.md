# Kotlin-Analysis dependencies

Functionality related to dependency analysis in Kotlin projects.

## Run the project

#### 1. Download list of repositories

You can use [seart-ghs.si.usi.ch]( https://seart-ghs.si.usi.ch/) (main language: Kotlin) for this purpose. The file has
to be in csv format with ```name``` column, containing repositories' names in the following
format: ```username/project_name```

#### 2. Clean duplicated repositories

Run script for cleaning all duplicated repositories with different names:

``` shell script
python3 scripts/clean_duplicates.py /path/to/csv_file/results.csv /path/to/cleaned/data/dir
```

You can use ```--save-metadata``` flag to download metadata about all projects. Metadata includes information about
repository's full name, owner, etc. This script makes requests to GitHub API, so you should add your GitHub Token to
environment variables (variable name is ```GITHUB_TOKEN```).

#### 3. Load dataset

Run the following command to download the dataset:

``` 
python3 scripts/load_dataset.py /path/to/cleaned/data/dir/results.csv /path/to/dataset/dir --allowed-extensions kt
```

#### 4. Extract import directives full-qualified names from dataset

``` 
gradle :kotlin-analysis-plugin:cli -Prunner=kotlin-import-directive-analysis -Pinput=/path/to/dataset/dir -Poutput=path/to/results/dir
```

#### 5. Run statistics visualisation

Not implemented now, wait for updates