# Python call expressions analysis

This script will allow you to count the number of unique call expressions in python projects, keeping their category. 
It is also possible to group statistics by language version of Python.

## Usage

Run the [`call_expressions_analysis.py`](./call_expressions_analysis.py) with the arguments from command line.

**Required arguments:**
- `--input` — Path to csv file with FQ names. Must contain columns: `project_name`, `fq_name` and `category`.
- `--output` — Path to the folder where to save the stats.

**Optional arguments:**

| Argument                                | Description                                                                                                              |
|-----------------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| **&#8209;&#8209;python&#8209;versions** | Path to the csv file with labeled projects by python version. Must contain columns: `project_name` and `python_version`. |
