# Python call expressions preprocessing
Analysis python call expressions, which include fully-qualified names of call expressions usage statistics.

## Python call expressions analysis
[call_expressions_analysis.py](call_expressions_analysis.py) will allow you to count the number of unique fq names in 
python projects, keeping their category. 

### Usage
```bash
python3 -m call_expressions_analysis --input /path/to/file/with/gathered/stats.csv --output /path/to/file/with/grouped/stats.csv
```
