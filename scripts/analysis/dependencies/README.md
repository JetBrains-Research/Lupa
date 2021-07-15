### Project Dependencies Preprocessing

Analysis and visualization project dependencies, which include fully-qualified names of import directives and their methods 
and classes usage statistics.

### Import Directives Analysis

Import directives analysis consists in processing collected from dataset of project fully-qualified names of import directives.

``` 
python3 scripts/analysis/dependencies/import_directives_analysis.py [-h] [--input INPUT] [--ignore IGNORE] 
                           [--max-package-len MAX_PACKAGE_LEN] [--max-subpackages MAX_SUBPACKAGES]
                           [--max-leaf-subpackages MAX_LEAF_SUBPACKAGES] [--min-occurrence MIN_OCCURRENCE] 
                           [--max-occurrence MAX_OCCURRENCE] [--max-u-occurrence MAX_U_OCCURRENCE]
```

Run to get the description of the above flags run:
``` 
python3 scripts/analysis/dependencies/import_directives_analysis.py 
```