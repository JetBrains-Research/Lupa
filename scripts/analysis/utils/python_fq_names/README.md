# Python FQ Names
These scripts will allow you to work with Python FQ names.

## Filter unnecessary FQ names
This script allows you to filter out unnecessary FQ names (private names, stdlib names, dunder names, builtin names).

### Usage
Run the [filter_unnecessary_fq_names.py](filter_unnecessary_fq_names.py) with the arguments from command line.

**Required arguments:**
- `--input` — path to csv file with FQ names.
- `--output` — path to output csv file with filtered FQ names.

**Optional arguments:**
| Argument | Description |
|----------|-------------|
| **&#8209;&#8209;column&#8209;name** | The name of the column to filter by. Default: `import`. |
| **&#8209;&#8209;filter&#8209;private&#8209;names** | If specified, private names will be filtered out. |
| **&#8209;&#8209;filter&#8209;stdlib&#8209;names** | If specified, Python Standard Library names will be filtered out. |
| **&#8209;&#8209;filter&#8209;dunder&#8209;names** | If specified, dunder names (names which begin and end with double underscores) will be filtered out. |