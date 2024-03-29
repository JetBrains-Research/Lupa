### Description

This module contains scripts for downloading repositories from GitHub and cleaning duplicated repositories.

### Usage

#### 1. Download list of repositories

You can use the resource [seart-ghs.si.usi.ch]( https://seart-ghs.si.usi.ch/) for this purpose (choose Kotlin as the main language).
The file has to be in the CSV format with the ```name``` column,
containing repositories' names in the following format: ```username/project_name```

#### 2. Clean duplicated repositories

Run the script for cleaning all duplicated repositories with different names:
``` shell script
python3 -m data_collection.clean_duplicates /path/to/csv_file/results.csv /path/to/cleaned/data/dir
```
You can use the ```--save-metadata``` flag to download the metadata about all projects. Metadata includes information about repository's full name, owner, etc.
This script makes requests to GitHub API, so you should add your [GitHub Token](https://github.com/settings/tokens) to environment variables (variable name is ```GITHUB_TOKEN```).

#### 3. Load dataset

Run the following command to download the dataset:

``` 
python3 -m data_collection.load_dataset /path/to/cleaned/data/dir/results.csv /path/to/dataset/dir --allowed-extensions kt
```
The optional argument ```--allowed-extensions``` accepts a list of extensions. 
Only files with these extensions will be saved (without this argument all files will be saved).

Or

#### 4. Update dataset

Run the following command to update the dataset:

``` 
python3 -m data_collection.update_dataset /path/to/cleaned/data/dir/results.csv /path/to/dataset/dir
```
The optional argument ```--save-to-db``` is used to save the date of last pull for each repository to the database.
In this case, you need to add "database.ini" to the project a file, containing config settings for some postgres database.


#### 5. Preprocess Android projects

Add `local.properties` file with Android SDK into each project. Run the following command:
``` 
python3 -m data_collection.preprocess_android_projects /path/to/folder/with/projects /absolute/path/to/android/sdk
```


### Save GitHub metrics

Run the script for saving different GitHub metrics (f.e. number of repository stars) every day:

``` shell script
python3 -m data_collection.save_metrics /path/to/csv_file/results.csv /path/to/output/metrics/dir --time hh:mm
```


Or with nohup:
``` shell script
nohup python3 -m data_collection.save_metrics /path/to/csv_file/results.csv /path/to/output/metrics/dir --time hh:mm > output.log &
```
This script makes requests to GitHub API, so you should add your GitHub Token to environment variables (variable name is ```GITHUB_TOKEN```).

The optional argument ```--time``` accepts time the data will be saved at.
### Save all data from GitHub

Run the script for saving all GitHub JSON responses every day:

``` shell script
python3 -m data_collection.save_github_daily /path/to/csv_file/results.csv /path/to/output/jsons/dir --time hh:mm
```
GitHub token is also required to run this script.
The optional argument ```--time``` accepts time the data will be saved at.
