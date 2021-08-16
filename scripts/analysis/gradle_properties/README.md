### Project Gradle Properties Preprocessing

This module contains scripts for properties from the gradle.properties files analysis.

### Statistics calculation for gradle properties keys occurrence

To count gradle properties keys occurrence statistics run:

``` 
python3 -m analysis/gradle_properties/gradle_properties_analysis.py 
                    --input path/to/gradle_properties_data.csv 
                    --output path/to/result/dir
                    --select path/to/selectes_properties_data.csv
```

The result of this script in `path/to/gradle_properties_stats.csv`, which contains information:


| property_key | count |
| ----- | ---- |
| org.gradle.caching | 6361 |
| org.gradle.parallel | 698 |
| org.gradle.vfs.watch | 234 |
| ... | ... |