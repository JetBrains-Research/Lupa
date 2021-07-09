# Kotlin-Analysis plugin

The plugin to run various project analysis from the CLI:
* ```kotlin-clones-analysis``` - command process code clones analysis 
* ```kotlin-dependencies-analysis``` - command process project dependencies and imports analysis

### Usage

To run analysis execute task 'cli' with analysis runner name and args:

``` 
gradle :kotlin-analysis-plugin:cli -Prunner="kotlin-object-analysis" -Pinput="path/to/dir/with/projects" -Poutput="path/to/dir/with/results"
```
