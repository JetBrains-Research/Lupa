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

Examples of import directives fq names analysis results visualization:

* "total.csv" and plotly bar chart with raw import dependencies fq names occurrence statistics

| fq_name  | count |
| ------------- | ------------- |
| androidx.compose.runtime.Composable  | 75  |
| javax.inject.Inject  | 80  |
| org.assertj.core.api.Assertions.assertThat | 90  |
| org.junit.Test  | 97  |
| ...  | ...  |

* "total_by_package.csv" and plotly bar chart with import dependencies fq names occurrence statistics grouped by a package prefix got by building fq names tree and detecting import dependencies packages names

| fq_name  | count |
| ------------- | ------------- |
| androidx.compose.runtime  | 320  |
| javax  | 380  |
| org.assertj.core.api | 590  |
| org.junit  | 997  |
| ...  | ...  |   

* "root.png" - dot packages tree with occurrence on edges, build from import dependencies names according to filters
   and simplifications with selected parameters (MAX_SUBPACKAGES, MAX_LEAF_SUBPACKAGES, MIN_OCCURRENCE, MAX_OCCURRENCE,
   MAX_U_OCCURRENCE)
   
* "{package_name}.png" - dot import dependencies fq names subtrees with occurrence on edges for each package
   
* "root.txt" - txt packages tree with occurrence on edges 
   
├── org [14280] \
│   ├── gradle [10104] \
│   │   ├── api [8972] \
│   │   ├── kotlin.dsl [1028] \
│   │   ├── testkit.runner [18] \
│   │   ├── work.InputChanges [17] \
│   │   ├── plugins.signing [15] \
│   ├── jetbrains [1708] \
│   │   ├── kotlin [1684] \
│   │   │   ├── psi [511] \
│   │   │   ├── descriptors [131] \
│   │   │   ├── com.intellij [56] \
│   │   └── gradle.ext [8] \
│   ├── junit [649] \
│   ├── testng [633] \
│   ├── springframework [406] \
...

* "{package_name}.txt" - txt import dependencies fq names subtrees with occurrence on edges for each package

org.springframework [406] \
├── beans.factory [58] \
│   ├── annotation.Autowired [43] \
│   └── BeanFactory [15] \
├── stereotype [53] \
│   ├── Service [19] \
│   ├── Repository [16] \
│   └── Component [13] \
├── web.bind [52] \
│   └── annotation [42] \
│       ├── GetMapping [8] \
│       └── RequestMapping [6] \