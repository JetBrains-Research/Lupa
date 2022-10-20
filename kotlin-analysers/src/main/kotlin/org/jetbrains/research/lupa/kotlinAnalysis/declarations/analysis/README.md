# Kotlin's analysers: internal declarations and usages

Functionality related to internal declarations and usages analysis in Kotlin projects.
This analyzer allows extracting all internal declarations and their usages from the Kotlin files

#### Running usage

Run the analyzer directly:

``` 
./gradlew :lupa-runner:cli -Prunner=kotlin-internal-declaration-psi-analysis -Pinput=/path/to/dataset/dir -Poutput=path/to/results/dir
```

In the result you will get two files: `internal_declarations.csv` and `internal_usages.csv`.
