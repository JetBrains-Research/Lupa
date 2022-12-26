# Kotlin's analysers: stdlib interfaces usages

Functionality related to stdlib interfaces usages analysis in Kotlin projects.
This analyzer allows extracting all interfaces that inherits several stdlib analyzers,
and override their functions with `super` calls.

The list of stdlib interfaces:
- kotlin.coroutines.ContinuationInterceptor
- kotlin.coroutines.CoroutineContext
- kotlin.coroutines.CoroutineContext.Element
- kotlin.ranges.ClosedRange
- kotlin.ranges.OpenEndRange
- kotlin.ranges.ClosedFloatingPointRange
- kotlin.text.MatchResult
- kotlin.time.TimeMark
- kotlin.time.ComparableTimeMark

#### Running usage

Run the analyzer directly:

``` 
./gradlew :lupa-runner:cli -Prunner=kotlin-stdlib-interfaces-analysis -Pinput=/path/to/dataset/dir -Poutput=path/to/results/dir
```

In the result you will the `stdlib_interfaces_data.csv` file.
