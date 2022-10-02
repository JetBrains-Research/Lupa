# Metrics collection

This module contains script to collect metrics for each project from a dataset.

## Usage

Run the [`collect_project_metrics.py`](./collect_project_metrics.py) with the arguments from command line.

**Required arguments:**
- `dataset_path` — Path to the dataset with projects.

**Optional arguments:**

| Argument                      | Description                                                                                                           |
|-------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| **&#8209;&#8209;metrics**     | Metric names to be collected. By default, all metrics specified in the [Metric](./metrics.py) enum will be collected. |
| **&#8209;&#8209;n&#8209;cpu** | Number of workers for a parallel execution. By default, it is equal to the number of CPUs in the system.              |
| **&#8209;&#8209;&#8209;**     | Path to a file where you want to save the logs. By default, the logs will be written to stderr.                       |
