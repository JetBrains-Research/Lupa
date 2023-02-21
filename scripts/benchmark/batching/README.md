## Batcher benchmark

This module contains scripts for benchmarking different batching algorithms.

### Usage

Run the [batcher_benchmark.py](batcher_benchmark.py) with the arguments from command line.

**Basic required arguments**:

- `dataset` — Path to a dataset with projects.
- `output` — Path to the output directory.
- `batching_config` — Path to a yaml config. More information about the config can be found in 
  [this](../../plugin_runner/README.md#batching-config) section in the [plugin_runner](../../plugin_runner) module.

**Analysis-specific required arguments**:

The analysis-specific required arguments are the same as those used in the [plugin_runner](../../plugin_runner) module.

**Basic optional arguments**:

| Argument                                     | Description                                                                                                                                                                           |
|----------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **&#8209;&#8209;sample**                     | Path to a csv file with a sample of projects. Must consist of one column `project` containing the project names. If not specified, the entire dataset will be used for the benchmark. |
| **&#8209;&#8209;warmup&#8209;runs**          | Number of warm-up runs. By default, 1.                                                                                                                                                |
| **&#8209;&#8209;benchmark&#8209;runs**       | Number of benchmark runs. By default, 3.                                                                                                                                              |
| **&#8209;&#8209;start&#8209;from**           | Index of batch to start processing from. By default, 0.                                                                                                                               |
| **&#8209;&#8209;save&#8209;every&#8209;run** | If specified, the analysis data for all runs will be saved, otherwise only the data for the last run will be saved.                                                                   |
| **&#8209;&#8209;logs&#8209;path**            | Path to a file where you want to save the logs. By default, the logs will be written to stderr.                                                                                       |

**Analysis-specific optional arguments**:

The analysis-specific optional arguments are the same as those used in the [plugin_runner](../../plugin_runner) module.

### Benchmark results comparison

You can visually compare several benchmark runs. To do this, run the following command:
```bash
streamlit run results_comparison.py
```

This visualization takes a folder with benchmark results as input and compares them by different stats.
An example of working with visualization is shown below:

