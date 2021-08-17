# Kotlin-Analysis gradle

Functionality related to Gradle dependencies analysis in Kotlin projects.

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

#### 4. Extract data

### Gradle Dependencies

Extract gradle dependencies form build.gradle/build.gradle.kts projects' files from dataset.
``` 
gradle :kotlin-analysis-plugin:cli -Prunner=kotlin-gradle-dependencies-analysis -Pinput=/path/to/dataset/dir -Poutput=path/to/results/dir
```

The result is `gradle_dependencies_data.csv` file with columns:

| project_name | group_id | artifact_id | configuration |
| ---- | ---- | ---- | ---- |
| ... | ... | ... | (compile, implementation, ...)

### Project Tags

For projects and modules tagging according to content, stack of technologies or theme we use gradle dependencies. Now we
support only android projects tagging.
``` 
gradle :kotlin-analysis-plugin:cli -Prunner=kotlin-projects-tagging -Pinput=/path/to/dataset/dir -Poutput=path/to/results/dir
```

The result is `projetcs_tagging_data.csv` file with columns:

| project_name | tag |
| ---- | ---- |
| ... | android / other |
  
where:\
`android` - for android project (which has `com.android.tools.build` dependency in gradle file) \
`other` - for other project, algorithm for them is not implemented now

### Gradle Properties
Extract gradle properties from gradle.properties projects' file from dataset. 
``` 
gradle :kotlin-analysis-plugin:cli -Prunner=kotlin-gradle-properties-analysis -Pinput=/path/to/dataset/dir -Poutput=path/to/results/dir
```

The result is `gradle_properties_data.csv` file with columns:

| project_name | property_key | property_value |
| ---- | ---- | ---- |
| ... | ... | ... |
