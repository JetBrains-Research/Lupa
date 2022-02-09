# Lupa 🔍: core functionality

This module contains functions common to all modules and analyzers.

Each analyzer has:
- an [analyzer](./src/main/kotlin/org/jetbrains/research/lupa/kotlinAnalysis/Analyzers.kt) that describes a filter that should be applied to the 
  [PSI tree](https://plugins.jetbrains.com/docs/intellij/psi.html) to obtain the necessary result;
- an [executor](./src/main/kotlin/org/jetbrains/research/lupa/kotlinAnalysis/AnalysisExecutor.kt) - a simple class that executes one or more analyzers 
  and manage of the resources (e.g. csv file) to write all results into it;
- a runner that runs an executor with the necessary data (see the [`lupa-runner`](../lupa-runner) module).

For each of them we have the common base classes that stores in this module. 
All these classes have detailed documentation.

## Analyzer example

Consider a base example of analyzer and executor. Consider the following code:

```kotlin
object WhileStatementAnalyzer : PsiAnalyzer<PyWhileStatement, String> {
  // The analyzer method to find all unreachable while loops:
  // Evaluate the 'while' condition and check if the 'while' 
  // condition is always false, then return its body
  // Return null otherwise
  override fun analyze(
    psiElement: PyWhileStatement,
  ) = if (PyEvaluator
      .evaluateAsBoolean(psiElement.whilePart.condition) == false
  ) {
    psiElement.whilePart.text
  } else null
}
```

This code demonstrates an analyzer for Python code that searches all unreachable while loops.
Besides using different PSI elements, it can be seen that we can use the `evaluateAsBoolean` 
method (already included in the IntelliJ Platform along with other such methods) 
to check the boolean value of a statement. So while something trivial 
as `while False` can be found using regular expression,
Lupa 🔍  would be able to find something like `while 2+2 != 4` as well, 
which simpler tools would not be able to find.
This simple analyzer demonstrates the power of the PSI for code analysis.
Also, this code is short and reuse the existing core architecture.

For this analyzer we should also implement simple executor to define the output csv format:

```kotlin
class WhileStatementAnalyzerExecutor(outputDir: Path, filename: String = "unreachable_while_data.csv") :
    AnalysisExecutor() {

    private val dataWriter = PrintWriterResourceManager(
        outputDir, filename,
        header = listOf("project_name", "when_body").joinToString(separator = ",")
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(dataWriter)

    override fun analyse(project: Project) {
        val whileStatements = project.extractKtElementsOfType(PyWhileStatement::class.java)
        val whileStatementsBodies = whileStatements.map { WhileStatementAnalyzer.analyze(it) }
        whileStatementsBodies.ifNotEmpty {
          dataWriter.writer.println(joinToString(separator = System.getProperty("line.separator")) {
                listOf(project.name, it).joinToString(separator = ",")
            })
        }
    }
}

```

As we can see, this class does not contain difficult logic, this class just extract 
all while statements and write data in the necessary format. This class also reuse the core architecture.

## Other functions

Also, this module contains some utilities, for example common functions for different PSI elements 
or utilities for open projects that are used in the [`lupa-runner`](../lupa-runner) module.

