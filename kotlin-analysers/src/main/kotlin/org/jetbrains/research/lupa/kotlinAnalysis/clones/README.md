# Kotlin-Analysis code clones

Functionality related to code clones analysis in Kotlin projects

## Run the project

#### 1. Download list of repositories
   
You can use [seart-ghs.si.usi.ch]( https://seart-ghs.si.usi.ch/) (main language: Kotlin) for this purpose.
The file has to be in csv format with ```name``` column, 
containing repositories' names in the following format: ```username/project_name``` 

#### 2. Clean duplicated repositories

Run the script for cleaning all duplicated repositories with different names: 
``` shell script
python3 scripts/data_collection/clean_duplicates.py /path/to/csv_file/results.csv /path/to/cleaned/data/dir
```
   You can use the ```--save-metadata``` flag to download metadata about all projects. Metadata includes the information about repository's full name, owner, etc.
   This script makes requests to GitHub API, so you should add your GitHub Token to environment variables (variable name is ```GITHUB_TOKEN```).

#### 3. Load dataset 

Run the following command to download the dataset:

``` 
python3 scripts/data_collection/load_dataset.py /path/to/cleaned/data/dir/results.csv /path/to/dataset/dir --allowed-extensions kt
```

#### 4. Extract methods from dataset

``` 
python3 scripts/plugin_runner/clones_batch_processing.py /path/to/dataset/dir /path/to/extracted_methods/dir 
```

You can use the ```--batch-size n``` option (default value is 300). 
Also there is ```--start-from n``` option to start from batch with given number.

Known issues:

This process runs IntelliJ Idea plugin and may get stack for some reason. 
If so, kill the process and rerun it with ```--start-from n``` option (using number of the last processed batch).


#### 5. Run Clone Detection Tool ([SourcererCC](https://github.com/Mondego/SourcererCC))

Use [README](https://github.com/Mondego/SourcererCC#run-sourcerercc) to run the tool and pass ```/path/to/extracted_methods/dir/method_data.txt``` as ```block.file```.

#### 6. Clones Preprocessing 

If the number of clones is large, clone detection tool output has to be preprocessed 
(converted from list of edges to adjacency list).

``` 
python3 scripts/analysis/clones/save_adjacency_list.py  /path/to/clones/dir/clones80.pairs /path/to/clones/dir/clones100.pairs /path/to/clones/dir
```

#### 7. Clones Analysis

Run Jupyter Notebook (scripts/clones_analysis/clones_analysis.ipynb) with clones analysis.
Set correct paths to dataset, extracted methods and clones in the cell:
```python
dataset_path = "/path/to/dataset/dir"
plugin_output_path = "/path/to/extracted_methods/dir"
clones_folder_path = "/path/to/clones/dir"
```
Notebook contains two parts: first is clones analysis based on the graph (it can be used without step 6), 
and second is based on the adjacency list (it can be used only after step 6,
and it is suitable for large number of clones).
