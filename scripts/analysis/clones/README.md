### Clones Preprocessing

If the number of clones is large, clone detection tool output has to be preprocessed
(converted from the list of edges to the adjacency list).

``` 
python3 scripts/analysis/clones/save_adjacency_list.py  /path/to/clones/dir/clones80.pairs /path/to/clones/dir/clones100.pairs /path/to/clones/dir
```

### Clones Analysis

Run Jupyter Notebook (scripts/clones_analysis/clones_analysis.ipynb) with clones analysis.
Set correct paths to the dataset, extracted methods, and clones in the cell:
```python
dataset_path = "/path/to/dataset/dir"
plugin_output_path = "/path/to/extracted_methods/dir"
clones_folder_path = "/path/to/clones/dir"
```
The notebook contains two parts: first is the clones analysis based on the graph (it can be used without clones preprocessing),
and the second is based on the adjacency list (it can be used only after clones preprocessing,
and it is suitable for large number of clones).