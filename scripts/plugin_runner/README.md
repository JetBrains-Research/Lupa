# Plugin runner

This module contains the scripts necessary to automatically run the plugin.

## Python venv

This module contains scripts for creating a python virtual environment.
For more information, see [this](python_venv/README.md) README file.

## Python version

This module contains a script to determine the versions of Python used in projects from the dataset.
For more information, see [this](python_version/README.md) README file.

## Batch processing

This script allows you to run dataset analysis in batches.

### Usage

Run the [batch_processing.py](batch_processing.py) with the arguments from command line.

**Basic required arguments**:

- `input` — Path to the dataset with the projects.
- `output` — Path to the output directory.
- `batching_config` — Path to a yaml config. More information about the config can be found in [this](#batching-config)
  section.

**Analysis-specific required arguments**:

- `data` — The name of the analysis to be run. The available values are listed in
  the [`AVAILABLE_ANALYZERS`](analyzers.py) list.

**Basic optional arguments**:

| Argument                           | Description                                                                                  |
|------------------------------------|----------------------------------------------------------------------------------------------|
| **&#8209;&#8209;use&#8209;db**     | Use database to analyse only updated repositories. By default, false.                        |
| **&#8209;&#8209;start&#8209;from** | Index of batch to start processing from (not for using with `--use-db` flag). By default, 0. |

**Analysis-specific optional arguments**:

| Argument                          | Description                                                                     |
|-----------------------------------|---------------------------------------------------------------------------------|
| **&#8209;&#8209;task&#8209;name** | The plugin task name. Might be: `cli` or `python-cli`. By default, `cli`.       |
| **&#8209;&#8209;kwargs**          | Map of additional plugin arguments. Usage example: `--kwargs venv=path/to/venv` |

### Batching config

The config must contain the `batcher_config` field, which contains the batcher name (the `name` field) and its
additional parameters.

Below is a list of supported batchers and their arguments:

- `dummy_batcher` — A simple batcher that groups projects into groups in the order in which they appear in the dataset.
  This batcher does not use project metrics for grouping.<br/>
  **Arguments**:
    - `batch_size` — Maximum number of projects that can be contained in a batch. By default, 50.
- `1d_first_fit_decreasing` — Batcher, based on
  the [Next Fit](https://en.wikipedia.org/wiki/Bin_packing_problem#Single-class_algorithms) algorithm, which is applied
  to a list of projects sorted in descending order. This batcher uses project metrics for grouping.<br/>
  **Arguments**:
    - `max_open_batches` — Maximum number of open batches. If specified,
      the [Next-k-Fit](https://en.wikipedia.org/wiki/Bin_packing_problem#Single-class_algorithms) algorithm is applied,
      where k equals the value of the argument. By default, 1.
- `1d_first_fit_decreasing` — Batcher, based on
  the [First Fit](https://en.wikipedia.org/wiki/Bin_packing_problem#Single-class_algorithms) algorithm, which is applied
  to a list of projects sorted in descending order. This batcher uses project metrics for grouping.
- `1d_best_fit_decreasing` — Batcher, based on
  the [Best Fit](https://en.wikipedia.org/wiki/Bin_packing_problem#Single-class_algorithms) algorithm, which is applied
  to a list of projects sorted in descending order. This batcher uses project metrics for grouping.
- `1d_worst_fit_decreasing` — Batcher, based on
  the [Worst Fit](https://en.wikipedia.org/wiki/Bin_packing_problem#Single-class_algorithms) algorithm, which is applied
  to a list of projects sorted in descending order. This batcher uses project metrics for grouping.

If you selected a batcher that uses project metrics, then additional fields must be specified:

- `batch_constraints` — A dictionary containing the constraints for each metric. The available metric values are
  specified in the [`MetricName`](../benchmark/metrics_collection/metrics.py) enum-class.
- `language` — The name of the language for which metrics will be used. The available values are specified in
  the [`Language`](../utils/language.py) enum-class
- `metric` — The name of the metric by which the grouping will be performed. The available metric values are
  specified in the [`MetricName`](../benchmark/metrics_collection/metrics.py) enum-class.

You can also specify additional arguments:

- `ignore_oversized_projects` — Whether to ignore projects whose metrics values exceed the batch constraints. By
  default, true. False means that each of such projects will be placed in a separate batch. This argument works only if
  you have selected a batcher that uses project metrics.

If you chose a batcher that uses project metrics, a file with project metrics must contain in each project from your
dataset. In order to create such files, you need to use the [metrics_collection](../benchmark/metrics_collection)
module.

Examples of configs you can see in the section below.

#### Config example

In this section you will find some examples of configs.

```yaml
batcher_config:
  name: dummy_batcher
  batch_size: 42
```

```yaml
batch_constraints:
    file_size: 1000000
    number_of_lines: 10000
    
language: python
metric: file_size

batcher_config:
  name: 1d_best_fit_decreasing
```

```yaml
batch_constraints:
    file_size: 1000000
    number_of_lines: 10000
    
language: python
metric: number_of_lines

batcher_config:
  name: 1d_first_fit_decreasing
  max_open_batches: 3
```

### Known issues

- This script runs IntelliJ Idea plugin and may get stuck for some reason. If so, kill the script process and rerun it
  with the ```--start-from n``` option (using number of the last processed batch).
