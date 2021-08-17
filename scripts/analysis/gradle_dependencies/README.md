### Project Gradle Dependencies Preprocessing

This module contains scripts for the dependencies section content for build.gradle/build.gradle.kts files analysis.

### Metadata for libraries collection

Metadata includes gradle dependency source (url to github) and language (main language in github). To collect it run:

``` 
python3 -m analysis/gradle_dependencies/gradle_dependencies_meta.py 
                    --input path/to/gradle_dependencies_data.csv 
                    --output path/to/result/dir
                    --plugins path/to/gradle_plugins_data.csv 
```

To get plugins data you need to:

1. Create file with libraries names which have github links
2. Run load_data.py on in to clone all libraries' repos
3. On this dataset run kotlin-gradle-plugins-analysis analyzer to get `gradle_plugins_data.csv`

### Statistics calculation for gradle dependencies occurrence

To count gradle dependencies occurrence statistics (including statistics for each configuration) and dependency type (
community/multiplatform) run:

``` 
python3 -m analysis/gradle_dependencies/gradle_dependencies.py 
                    --input path/to/gradle_dependencies_data.csv 
                    --output path/to/result/dir
                    --meta path/to/gradle_dependencies_meta_data.csv 
                    --tagged_projects path/to/tagged_projects_data.csv
                    --tags android other ... 
```

The result of this script in `path/to/gradle_dependencies_stats.csv`, which contains information:

| full_name | count | url | language | community | multiplatform |implementation | testImplementation | androidTestImplementation | kapt | classpath | api | annotationProcessor | compile | compileOnly | testCompile | testRuntimeOnly | testRuntime | runtimeOnly  |
| ----- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- |
| org.jetbrains.kotlin:kotlin-gradle-plugin | 1165 | https://github.com/JetBrains/kotlin | Kotlin | False |  False | 109 | 2 | 0 | 0 | 1014 | 19 | 0 | 3 | 18 | 0 | 0 | 0 | 0 |
| org.jetbrains.kotlin:kotlin-stdlib-jdk8 | 1037 | https://github.com/JetBrains/kotlin | Kotlin | False |  False | 652 | 4 | 0 | 0 | 2 | 44 | 0 | 286 | 47 | 0 | 0 | 0 | 2 | 
| org.jetbrains.kotlin:kotlin-stdlib | 690 | https://github.com/JetBrains/kotlin | Kotlin | False |  False | 284 | 5 | 0 | 0 | 1 | 33 | 0 | 328 | 35 | 2 | 1 | 0 | 1 | 
| org.jetbrains.kotlin:kotlin-reflect | 605 | https://github.com/JetBrains/kotlin | Kotlin | False |  True | 312 | 22 | 0 | 0 | 4 | 39 | 0 | 183 | 18 | 16 | 8 | 0 | 3 | 
| junit:junit | 601 | https://github.com/junit-team/junit4 | Java | True |  False | 30 | 266 | 5 | 0 | 0 | 5 | 0 | 12 | 2 | 281 | 0 | 0 | 0 | 
| ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... |