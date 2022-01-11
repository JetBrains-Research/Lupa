# Lupa 🔍

[![JetBrains Research](https://jb.gg/badges/research.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![Kotlin build](https://github.com/JetBrains-Research/Lupa/actions/workflows/kotlin-build.yml/badge.svg)](https://github.com/JetBrains-Research/Lupa/actions/workflows/kotlin-build.yml)
[![Python build](https://github.com/JetBrains-Research/Lupa/actions/workflows/python-build.yml/badge.svg)](https://github.com/JetBrains-Research/Lupa/actions/workflows/python-build.yml)

Lupa 🔍 is an extendable framework for analyzing fine-grained language usage on the base of the [IntelliJ Platform](https://www.jetbrains.com/opensource/idea/).
Lupa 🔍 is a command line tool that uses the power of the IntelliJ Platform under 
the hood to perform code analysis using the same industry-level tools that are employed in IntelliJ-based IDEs,
such as [IntelliJ IDEA](https://www.jetbrains.com/idea/), 
[PyCharm](https://www.jetbrains.com/pycharm/), 
or [CLion](https://www.jetbrains.com/clion/).

Currently, our framework supports analyzing two languages: Python --- 
a mature language [most popular](https://octoverse.github.com/#top-languages-over-the-years) 
in data science and machine learning, 
and Kotlin --- a relatively young but [quickly growing](https://developer-economics.cdn.prismic.io/developer-economics/dbf9f36f-a31a-440a-9c22-c599cc235fa4_20th+edition+-+State+of+the+developer+Nation.pdf) language.

## How it works

Lupa 🔍 is a platform for large-scale analysis of the programming language usage.
Specifically, Lupa 🔍 is implemented as a plugin for the IntelliJ Platform that reuses 
its API to launch the IDE in the background (without user interface) and 
run the necessary analysis on every project in the given dataset.

The main pipeline of Lupa 🔍 is demonstrated bellow:

<img src="https://github.com/JetBrains-Research/Lupa/tree/main/assets/readme-pictures/pipeline.png" width="500">

To perform the analysis, the tool needs two obvious components: 
a _dataset_ and _analyzers_, _i.e._, sets of instructions of what [PSI tree](https://plugins.jetbrains.com/docs/intellij/psi.html) nodes need to be analyzed and how.
To get more information about data collection see the [data_collection](./scripts/data_collection) module.
The repository contains several core-modules:
- [`kotlin-analysis-core`](./kotlin-analysis-core) - functions common to all modules and analyzers;
- [`kotlin-analysis-test`](./kotlin-analysis-test) - common tests' architecture for all modules;
- [`kotlin-analysis-plugin`](./kotlin-analysis-plugin) - the module with runners for all analyzers;
- [`scripts`](./scripts) - common functionality for data gathering, processing and visualization (written in Python).

And several examples of analyzers that we used for our purposes:
- [`kotlin-analysis-clones`](./kotlin-analysis-clones) - functionality related to clones analysis in Kotlin projects;
- [`kotlin-analysis-dependencies`](./kotlin-analysis-dependencies) - functionality related to dependency analysis in Kotlin projects;
- [`kotlin-analysis-gradle`](./kotlin-analysis-gradle) - functionality related to code analysis of the Gradle files in Kotlin projects;
- [`kotlin-analysis-statistic`](./kotlin-analysis-statistic) - functionality related to different code analysis in Kotlin projects, like range analysis;
- [`python-analysis-call-expressions`](./python-analysis-call-expressions) - functionality related to call expressions (functions, classes) analysis in Python projects;
- [`python-analysis-call-dependencies`](./python-analysis-dependencies) - functionality related to imports analysis in Python projects.

To get more information see these modules.

## Installation

Clone the repo by `git clone https://github.com/JetBrains-Research/Lupa.git`.
   
For analyzers modules and core architecture you should have Kotlin at least `1.5.21` version.
For functionality for data gathering, processing and visualization ([`scripts`](./scripts) module) 
you should have Python 3+ and also run:
- `pip install -r scripts/requirements.txt`
- `pip install -r scripts/requirements-test.txt` - for tests (optional)
- `pip install -r scripts/requirements-test.txt` - for code style checkers (optional)

## Usage

1. For analyzers:
    - Go to the [`kotlin-analysis-plugin`](./kotlin-analysis-plugin) module and follow its [README file](./kotlin-analysis-plugin/README.md).
2. For functionality for data gathering, processing and visualization:
    - Go to the [`scripts`](./scripts) module and follow its [README file](./scripts/README.md).

## Contribution

Please be sure to review project's [contributing guidelines](./docs/contributing.md) to learn how to help the project.

