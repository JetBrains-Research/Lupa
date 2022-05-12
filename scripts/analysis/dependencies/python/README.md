# Python imports analysis

This script will allow you to count the number of unique imports in python projects. 
It is also possible to group statistics by language version of Python.

This script is a wrapper over [import_directives_analysis.py](../import_directives_analysis.py).
## Usage

Run the [`imports_analysis.py`](./imports_analysis.py) with the arguments from command line.

**Required arguments:**
- `--input` — Path to csv file with imports. Must contain columns: `project_name` and `import`.
- `--output` — Path to the folder where to save the stats.

**Optional arguments:**

| Argument                                | Description                                                                                                              |
|-----------------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| **&#8209;&#8209;python&#8209;versions** | Path to the csv file with labeled projects by python version. Must contain columns: `project_name` and `python_version`. |
