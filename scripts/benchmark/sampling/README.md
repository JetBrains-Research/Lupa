# Stratified sampling

This script will extract a stratified sample from a dataset where the distribution of the selected metrics is saved.

## Usage
Run the [stratified_sampling.py](stratified_sampling.py) with the arguments from command line.

**Required arguments**:
- `dataset_path` — Path to a dataset with projects.
- `config_path` — Path to a yaml config. More information about the config can be found in [this](#config) section.
- `output_path` — Path to a csv file with selected projects.

**Optional arguments**:

| Argument                             | Description                                                                                     |
|--------------------------------------|-------------------------------------------------------------------------------------------------|
| **&#8209;&#8209;random&#8209;state** | Seed for random number generator.                                                               |
| **&#8209;&#8209;logs&#8209;path**    | Path to a file where you want to save the logs. By default, the logs will be written to stderr. |

You can also use the [sampling_visualization.py](sampling_visualization.py) to interactively work with stratified sampling. 
To do this, run the following command:
```
streamlit run sampling_visualization.py
```

In this visualization you can interactively select different histograms for metrics and download the resulting config. 
Also, if you specify the sample size, you can start sampling and download the result. 
An example of working with visualization is shown below:

### Config

**Required fields**:
- `sample_size` — The expected number of projects in the sample.
- `language` — Language whose metrics need to be read.
- `strata` — Dictionary, where the bins for each metric for which you want to save the distribution are specified. 
  The possible values of the bins are given [here](https://numpy.org/doc/stable/reference/generated/numpy.histogram_bin_edges.html). 
  By default, bins equals to "auto".

#### Example
```yaml
sample_size: 1000
language: python
strata:
  number_of_lines: 10
  number_of_files: fd
  file_size: [1, 20, 40, 90]
  # Same as number_of_requirements: auto
  number_of_requirements: 
```
