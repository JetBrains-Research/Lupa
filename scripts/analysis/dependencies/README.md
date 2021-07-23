### Project Dependencies Preprocessing

Analysis and visualization project dependencies, which include fully-qualified names of import directives and their
methods and classes usage statistics.

### Import Directives Analysis

Import directives analysis consists in processing collected from dataset of project fully-qualified names of import
directives.

``` 
python3 scripts/analysis/dependencies/import_directives_analysis.py [-h] --input INPUT --output OUTPUT [--ignore IGNORE] [--max-package-len MAX_PACKAGE_LEN] [--max-subpackages MAX_SUBPACKAGES]
                                     [--max-leaf-subpackages MAX_LEAF_SUBPACKAGES] [--min-occurrence MIN_OCCURRENCE] [--max-occurrence MAX_OCCURRENCE]
                                     [--max-u-occurrence MAX_U_OCCURRENCE] [--show-dot-trees SHOW_DOT_TREES] [--show-txt-trees SHOW_TXT_TREES]
                                     [--show-bar-plots SHOW_BAR_PLOTS] [--show-csv SHOW_CSV] [--show-package-csv SHOW_PACKAGE_CSV]
``` 

Run to get the description of the above flags run:
``` 
python3 scripts/analysis/dependencies/import_directives_analysis.py -h
```

Examples of each import directives fq names representation type:

