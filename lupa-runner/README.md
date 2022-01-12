# Lupa 🔍: set of runners for the analyzers

This module contains all runners for analyzers from the repository. 
We separated the runners from analyzers since we use the IntelliJ Platform, and the module 
with runners extensions points should be separated.

The plugin to run various project analysis from the CLI:
1. [Kotlin's analysers](./src/main/kotlin/org/jetbrains/research/lupa/kotlinAnalysis):
    * [```java-reflections-analysis```](./src/main/kotlin/org/jetbrains/research/lupa/kotlinAnalysis/JavaReflectionsFunctionsAnalysisRunner.kt) - search for Kava reflections calls
    * [```kotlin-clones-analysis```](./src/main/kotlin/org/jetbrains/research/lupa/kotlinAnalysis/KotlinClonesAnalysisRunner.kt) - command process code clones analysis
    * [```kotlin-dependencies-analysis```](./src/main/kotlin/org/jetbrains/research/lupa/kotlinAnalysis/KotlinDependenciesAnalysisRunner.kt) - command process project dependencies and imports analysis
    * [```kotlin-gradle-dependencies-analysis```](./src/main/kotlin/org/jetbrains/research/lupa/kotlinAnalysis/KotlinGradleDependenciesAnalysisRunner.kt) - command process Gradle dependencies and imports analysis
    * [```kotlin-gradle-dependencies-by-modules-analysis```](./src/main/kotlin/org/jetbrains/research/lupa/kotlinAnalysis/KotlinGradleDependenciesByModulesAnalysisRunner.kt) - command process Gradle dependencies and imports analysis by each module in the projects
    * [```kotlin-gradle-plugins-analysis```](./src/main/kotlin/org/jetbrains/research/lupa/kotlinAnalysis/KotlinGradlePluginsAnalysisRunner.kt) - command process Gradle plugins 
    * [```kotlin-gradle-properties-analysis```](./src/main/kotlin/org/jetbrains/research/lupa/kotlinAnalysis/KotlinGradlePropertiesAnalysisRunner.kt) - command process Gradle properties 
    * [```kotlin-project-metrics-analysis```](./src/main/kotlin/org/jetbrains/research/lupa/kotlinAnalysis/KotlinProjectMetricsAnalysisAnalysisRunner.kt) - command process projects metrics
    * [```kotlin-ranges-analysis```](./src/main/kotlin/org/jetbrains/research/lupa/kotlinAnalysis/KotlinRangesAnalysisRunner.kt) - command process various ranges implementations usage analysis
    * [```kotlin-project-tags-analysis```](./src/main/kotlin/org/jetbrains/research/lupa/kotlinAnalysis/ProjectsTaggingRunner.kt) - command process projects from dataset tagging according to theme or content
2. [Python's analysers](./src/main/kotlin/org/jetbrains/research/lupa/pythonAnalysis):
    * [```python-call-expressions-analysis```](./src/main/kotlin/org/jetbrains/research/lupa/pythonAnalysis/PythonCallExpressionsAnalysisRunner.kt) - command process python call expressions analysis
    * [```python-imports-analysis```](./src/main/kotlin/org/jetbrains/research/lupa/pythonAnalysis/PythonImportsAnalysisRunner.kt) - command process python imports analysis

### Usage

[comment]: <> (TODO: maybe reorganize this section)

To run analysis execute task 'cli' with analysis runner name and args:

1. [Kotlin's analysers](./src/main/kotlin/org/jetbrains/research/lupa/kotlinAnalysis):
``` 
gradle :lupa-runner:cli -Prunner="analysis-key" -Pinput="path/to/dir/with/projects" -Poutput="path/to/dir/with/results"
```

where `analysis-key` is a key from the list of the analysers.

2. [Python's analysers](./src/main/kotlin/org/jetbrains/research/lupa/pythonAnalysis):
    * [```python-call-expressions-analysis```](./src/main/kotlin/org/jetbrains/research/lupa/pythonAnalysis/PythonCallExpressionsAnalysisRunner.kt):
        ``` 
        gradle :lupa-runner:cli -Prunner="python-call-expressions-analysis" -Pinput="path/to/dir/with/projects" -Poutput="path/to/dir/with/results"
        ```
      **Note**: the additional parameter `-Pvenv` may be passed.
      Details on running `python-call-expressions-analysis` can be found in its README in the  `python-analysers` module.

    * [```python-imports-analysis```](./src/main/kotlin/org/jetbrains/research/lupa/pythonAnalysis/PythonImportsAnalysisRunner.kt):
        ``` 
        gradle :lupa-runner:cli -Prunner="python-imports-analysis" -Pinput="path/to/dir/with/projects" -Poutput="path/to/dir/with/results"
        ```

