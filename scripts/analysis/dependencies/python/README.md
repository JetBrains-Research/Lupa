# Python-Analysis dependencies
These scripts will help you better analyze python imports.

## Filter unnecessary imports
This script allows you to filter out unnecessary imports (private imports, stdlib imports).

### Usage
Run the [filter_unnecessary_imports.py](filter_unnecessary_imports.py) with the arguments from command line.

**Required arguments:**
- `--input` — path to csv file with imports.
- `--output` — path to output csv file with filtered imports.

**Optional arguments:**
| Argument | Description |
|----------|-------------|
| **&#8209;&#8209;column&#8209;name** | The name of the column to filter by. Default: `import`. |
| **&#8209;&#8209;filter&#8209;private&#8209;imports** | If specified, private imports will be filtered out. |
| **&#8209;&#8209;filter&#8209;stdlib&#8209;imports** | If specified, Python Standard Library imports will be filtered out. |
