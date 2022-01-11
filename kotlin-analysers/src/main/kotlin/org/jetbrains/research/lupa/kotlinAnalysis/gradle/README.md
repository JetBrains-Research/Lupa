# Kotlin's analysers: Gradle files

Functionality related to Gradle files analysis in Kotlin projects.
This module has several analyzers:
1. Analysis of Gradle dependencies
2. Analysis of Gradle properties
3. Extract projects tags and categorize projects by them 

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

#### 4. Extract data (different analyzers)

##### 4.1 Gradle Dependencies

Extract gradle dependencies form `build.gradle/build.gradle.kts` and `build.gradle/build.gradle` projects' files from the dataset.
Run the analyzer directly:
``` 
./gradlew :lupa-runner:cli -Prunner=kotlin-gradle-dependencies-analysis -Pinput=/path/to/dataset/dir -Poutput=path/to/results/dir
```
Or you can handle the dataset via batches using batch processing:
``` 
python3 -m scripts/plugin_runner/batch_processing /path/to/dataset/dir /path/to/extracted_methods/dir kotlin-gradle-dependencies
```
In this case you can use the ```--batch-size n``` option (default value is 300).
Also, there is ```--start-from n``` option to start from batch with given number.
To get more information, please see the [README](../../../../../../../../../../scripts/plugin_runner/README.md)
file from the [`plugin_runner`](../../../../../../../../../../scripts/plugin_runner) module.

The result is `gradle_dependencies_data.csv` file with columns:

| project_name | group_id | artifact_id | configuration |
| ---- | ---- | ---- | ---- |
| ... | ... | ... | (compile, implementation, ...)


##### 4.2 Gradle Properties
Extract gradle properties from `gradle.properties` projects' file from dataset.
Run the analyzer directly:
``` 
./gradlew :lupa-runner:cli -Prunner=kotlin-gradle-properties-analysis -Pinput=/path/to/dataset/dir -Poutput=path/to/results/dir
```
Or you can handle the dataset via batches using batch processing:
``` 
python3 -m scripts/plugin_runner/batch_processing /path/to/dataset/dir /path/to/extracted_methods/dir kotlin-gradle-properties
```
In this case you can use the ```--batch-size n``` option (default value is 300).
Also, there is ```--start-from n``` option to start from batch with given number.
To get more information, please see the [README](../../../../../../../../../../scripts/plugin_runner/README.md)
file from the [`plugin_runner`](../../../../../../../../../../scripts/plugin_runner) module.

The result is `gradle_properties_data.csv` file with columns:

| project_name | property_key | property_value |
| ---- | ---- | ---- |
| ... | ... | ... |

##### 4.3 Project Tags

For projects and modules tagging according to content, stack of technologies or theme we use gradle dependencies. 
Now we support only android projects tagging.
Run the analyzer directly:
``` 
./gradlew :lupa-runner:cli -Prunner=kotlin-project-tags-analysis -Pinput=/path/to/dataset/dir -Poutput=path/to/results/dir
```
Or you can handle the dataset via batches using batch processing:
``` 
python3 -m scripts/plugin_runner/batch_processing /path/to/dataset/dir /path/to/extracted_methods/dir kotlin-project-tags
```
In this case you can use the ```--batch-size n``` option (default value is 300).
Also, there is ```--start-from n``` option to start from batch with given number.
To get more information, please see the [README](../../../../../../../../../../scripts/plugin_runner/README.md)
file from the [`plugin_runner`](../../../../../../../../../../scripts/plugin_runner) module.


The result is `projetcs_tagging_data.csv` file with columns:

| project_name | tag |
| ---- | ---- |
| ... | android / other |
  
where:\
`android` - for android project (which has `com.android.tools.build` dependency in gradle file) \
`other` - for other project, algorithm for them is not implemented now
