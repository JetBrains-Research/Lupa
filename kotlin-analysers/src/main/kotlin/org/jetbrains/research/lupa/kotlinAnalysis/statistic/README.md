# Kotlin-Analysis statistic

Functionality related to code analysis in Kotlin projects

### Ranges analysis
For range analysis run
```
gradle :kotlin-analysis-plugin:cli -Prunner=kotlin-ranges-analysis -Pinput=/path/to/dataset/dir -Poutput=path/to/results/dir
```


#### Results for 10k Kotlin repositories:


|             |  for  | property |  map |  if  | when | forEach | toList | require | while | other | TOTAL |
|:-----------:|:-----:|:--------:|:----:|:----:|:----:|:-------:|:------:|:-------:|:-----:|:-----:|:-----:|
|   **dots**  | 13512 |     5383 | 2653 | 3399 | 2943 |    1451 |    612 |     459 |    82 |  6035 | 36529 |
|  **until**  | 25216 |      923 | 2115 |  993 |  345 |    1188 |    184 |      73 |    36 |  1485 | 32558 |
|  **downTo** |  1718 |       40 |   47 |   49 |    2 |      78 |     25 |       0 |     0 |    38 |  1997 |
| **rangeTo** |    18 |       52 |   43 |    4 |   12 |      65 |     13 |       1 |     0 |    71 |   279 |
|  **TOTAL**  | 40464 |     6398 | 4858 | 4445 | 3302 |    2782 |    834 |     533 |   118 |  7629 | 71363 |