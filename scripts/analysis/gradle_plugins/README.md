### Project Gradle Plugins Preprocessing

This module contains scripts for plugins from the build.gradle/build.gradle.kts files analysis.

### Statistics calculation for gradle plugin ids occurrence

To count gradle plugin ids occurrence statistics run:

``` 
python3 -m analysis/gradle_properties/gradle_plugins_analysis.py 
                    --input path/to/gradle_plugins_data.csv 
                    --output path/to/result/dir 
```

The result of this script in `path/to/gradle_plugin_(by_module|by_project)_stats.csv`, which contains information:


| plugin_id | count |
| ----- | ---- |
| kotlin-android | 415 |
| com.android.application | 293 |
| kotlin-android-extensions | 269 |
| com.android.library | 172 |
| kotlin-kapt | 172 |
| ... | ... |
