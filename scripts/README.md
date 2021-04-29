## Description

Module contains script for downloading repositories from GitHub.

### Usage

1. Download csv file containing names of repositories from [https://seart-ghs.si.usi.ch/](https://seart-ghs.si.usi.ch/)
   
2. Run script for cleaning all duplicated repositories with different names: ``` python3 scripts/clean_duplicates.py /path/to/csvFile/results.csv /path/to/output/directory```. 
   You can use ```--save_metadata``` flag to download metadata about all projects. 
   This script makes requests to GitHub API, so you should add your GitHub Token to environment variables.

3. Run ``` python3 scripts/load_dataset.py /path/to/cleaned/results.csv /path/to/output/directory/with/dataset --allowed_extensions kt``` 
   to download the dataset.
