# Kotlin's analysers: statistic

Functionality related to gather different statistics in Kotlin projects.
This module has several analyzers:
1. Extract projects metrics
2. Analyze usage of different ranges
3. Search projects with Java Reflection

## The pipeline of the running this module

#### 1. Download list of repositories

You can use [seart-ghs.si.usi.ch]( https://seart-ghs.si.usi.ch/) (main language: Kotlin) for this purpose. The file has
to be in csv format with ```name``` column, containing repositories' names in the following
format: ```username/project_name```

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

#### 4. Extract statistics (different analyzers)

##### 4.1 Extract projects metrics

This analyzer collects number of modules, files, dependencies in all modules' gradle files.
Run the analyzer directly:
``` 
./gradlew :lupa-runner:cli -Prunner=kotlin-project-metrics-analysis -Pinput=/path/to/dataset/dir -Poutput=path/to/results/dir
```
Or you can handle the dataset via batches using batch processing:
``` 
python3 -m scripts/plugin_runner/batch_processing /path/to/dataset/dir /path/to/extracted_methods/dir kotlin-project-metrics
```
In this case you can use the ```--batch-size n``` option (default value is 300).
Also, there is ```--start-from n``` option to start from batch with given number.
To get more information, please see the [README](../../../../../../../../../../scripts/plugin_runner/README.md)
file from the [`plugin_runner`](../../../../../../../../../../scripts/plugin_runner) module.

The result is `project_metrics_data.csv` file with columns:

| project_name | module_name | files_count | lines_count | symbols_count |
| ---- | ---- | ---- | ---- |
| ... | ... | ... | ... | ... |

##### 4.2 Analyze usage of different ranges
This analyzer allows collections statistics about usage ranges with their context.

Run the analyzer directly:
``` 
./gradlew :lupa-runner:cli -Prunner=kotlin-ranges-analysis -Pinput=/path/to/dataset/dir -Poutput=path/to/results/dir
```
Or you can handle the dataset via batches using batch processing:
``` 
python3 -m scripts/plugin_runner/batch_processing /path/to/dataset/dir /path/to/extracted_methods/dir kotlin-ranges
```
In this case you can use the ```--batch-size n``` option (default value is 300).
Also, there is ```--start-from n``` option to start from batch with given number.
To get more information, please see the [README](../../../../../../../../../../scripts/plugin_runner/README.md)
file from the [`plugin_runner`](../../../../../../../../../../scripts/plugin_runner) module.


Last results for 10k Kotlin repositories:


|             |  for  | property |  map |  if  | when | forEach | toList | require | while | other | TOTAL |
|:-----------:|:-----:|:--------:|:----:|:----:|:----:|:-------:|:------:|:-------:|:-----:|:-----:|:-----:|
|   **dots**  | 13512 |     5383 | 2653 | 3399 | 2943 |    1451 |    612 |     459 |    82 |  6035 | 36529 |
|  **until**  | 25216 |      923 | 2115 |  993 |  345 |    1188 |    184 |      73 |    36 |  1485 | 32558 |
|  **downTo** |  1718 |       40 |   47 |   49 |    2 |      78 |     25 |       0 |     0 |    38 |  1997 |
| **rangeTo** |    18 |       52 |   43 |    4 |   12 |      65 |     13 |       1 |     0 |    71 |   279 |
|  **TOTAL**  | 40464 |     6398 | 4858 | 4445 | 3302 |    2782 |    834 |     533 |   118 |  7629 | 71363 |

##### 4.3 Search projects with Java Reflection
This analyzer collects projects that use java reflection functions and stores them to the file

Run the analyzer directly:
``` 
./gradlew :lupa-runner:cli -Prunner=java-reflections-analysis -Pinput=/path/to/dataset/dir -Poutput=path/to/results/dir
```
Or you can handle the dataset via batches using batch processing:
``` 
python3 -m scripts/plugin_runner/batch_processing /path/to/dataset/dir /path/to/extracted_methods/dir java-reflections
```
In this case you can use the ```--batch-size n``` option (default value is 300).
Also, there is ```--start-from n``` option to start from batch with given number.
To get more information, please see the [README](../../../../../../../../../../scripts/plugin_runner/README.md)
file from the [`plugin_runner`](../../../../../../../../../../scripts/plugin_runner) module.

