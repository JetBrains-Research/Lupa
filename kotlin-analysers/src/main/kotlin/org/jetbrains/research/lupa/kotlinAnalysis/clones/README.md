# Kotlin's analysers: code clones

Functionality related to code clones analysis in Kotlin projects.
We extract all methods from the Kotlin projects, 
unify them to the special format for the [SourcererCC](https://github.com/Mondego/SourcererCC) tool 
and then process and visualize results.

## The pipeline of the running this module

#### 1. Download list of repositories
   
You can use [seart-ghs.si.usi.ch]( https://seart-ghs.si.usi.ch/) (main language: Kotlin) for this purpose.
The file has to be in csv format with ```name``` column, 
containing repositories' names in the following format: ```username/project_name``` 

#### 2. Clean duplicated repositories

Run the script for cleaning all duplicated repositories with different names: 
``` shell script
python3 -m scripts/data_collection/clean_duplicates /path/to/csv_file/results.csv /path/to/cleaned/data/dir
```
   You can use the ```--save-metadata``` flag to download metadata about all projects. 
   Metadata includes the information about repository's full name, owner, etc.
   This script makes requests to GitHub API, 
   so you should add your GitHub Token to environment variables 
   (variable name is ```GITHUB_TOKEN```).
   To get more information, please see the [README](../../../../../../../../../../scripts/data_collection/README.md)
   file from the [`data_collection`](../../../../../../../../../../scripts/data_collection) module.

#### 3. Load dataset 

Run the following command to download the dataset:

``` 
python3 -m scripts/data_collection/load_dataset /path/to/cleaned/data/dir/results.csv /path/to/dataset/dir --allowed-extensions kt
```

To get more information, please see the [README](../../../../../../../../../../scripts/data_collection/README.md)
file from the [`data_collection`](../../../../../../../../../../scripts/data_collection) module.

#### 4. Extract methods from dataset

``` 
python3 -m scripts/plugin_runner/batch_processing /path/to/dataset/dir /path/to/extracted_methods/dir kotlin-clones
```

You can use the ```--batch-size n``` option (default value is 300). 
Also, there is ```--start-from n``` option to start from batch with given number.

To get more information, please see the [README](../../../../../../../../../../scripts/plugin_runner/README.md)
file from the [`plugin_runner`](../../../../../../../../../../scripts/plugin_runner) module.

**Note**: This process runs IntelliJ Idea plugin and may get stack for some reason. 
If so, kill the process and rerun it with ```--start-from n``` option (using number of the last processed batch).


#### 5. Run Clone Detection Tool ([SourcererCC](https://github.com/Mondego/SourcererCC))

Use [README](https://github.com/Mondego/SourcererCC#run-sourcerercc) to run the tool 
and pass ```/path/to/extracted_methods/dir/method_data.txt``` as ```block.file```.

#### 6. Clones Preprocessing 

If the number of clones is large, clone detection tool output has to be preprocessed 
(converted from list of edges to adjacency list).

``` 
python3 -m scripts/analysis/clones/save_adjacency_list  /path/to/clones/dir/clones80.pairs /path/to/clones/dir/clones100.pairs /path/to/clones/dir
```

To get more information, please see the [README](../../../../../../../../../../scripts/analysis/clones/README.md)
file from the [`clones`](../../../../../../../../../../scripts/analysis/clones) module.

#### 7. Clones Analysis

Run [Jupyter Notebook](../../../../../../../../../../scripts/analysis/clones/clones_analysis.ipynb) with clones analysis.
Set correct paths to dataset, extracted methods and clones in the cell:
```python
dataset_path = "/path/to/dataset/dir"
plugin_output_path = "/path/to/extracted_methods/dir"
clones_folder_path = "/path/to/clones/dir"
```
Notebook contains two parts: first is clones analysis based on the graph (it can be used without step#6), 
and second is based on the adjacency list (it can be used only after step#6,
and it is suitable for large number of clones).
