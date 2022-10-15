# Metrics collection

This module contains script to collect metrics for each project from a dataset.

## Usage

Run the [`collect_project_metrics.py`](collect_project_metrics.py) with the arguments from command line.

**Required arguments:**
- `dataset_path` — Path to the dataset with projects.
- `config_path` — Path to a yaml config. More information about the config can be found in [this](#config) section.

**Optional arguments:**

| Argument                          | Description                                                                                              |
|-----------------------------------|----------------------------------------------------------------------------------------------------------|
| **&#8209;&#8209;n&#8209;cpu**     | Number of workers for a parallel execution. By default, it is equal to the number of CPUs in the system. |
| **&#8209;&#8209;logs&#8209;path** | Path to a file where you want to save the logs. By default, the logs will be written to stderr.          |

### Config
In the config field `metrics` you specify the names of the metrics that you want to collect from the projects. The list 
of available names is specified in the [`MetricName`](metrics.py) enum.

For the following metrics you can specify additional parameters:
* `number_of_lines`:
  * `ignore_empty_lines` — Is it necessary to ignore empty lines. By default, false.
  * `ignore_comments` — Is it necessary to ignore comments. By default, false. Only Python and Kotlin comment 
    processing is supported.

#### Config example
```yaml
metrics:
  number_of_files:
  number_of_dependencies:
  number_of_lines:
    ignore_comments: True
```
