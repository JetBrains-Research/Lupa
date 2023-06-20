# What this repository is

This repository contains sources of:
1. Lupa 🔍 core modules: core architecture, common tests architecture, analyzers runners. (in Kotlin)
2. Examples of several analyzers for Kotlin and Python. (in Kotlin)
3. Common functionality for data gathering, processing and visualization. (in Python)   

# Contributing

We love contributions! We are happy to see new analyzers and improvements of our core functionality, 
and even supporting of new languages. 
The current tasks can be found in the [open issues](https://github.com/JetBrains-Research/Lupa/issues) in the project.
If you have some questions or feature requests, please do not hesitate to open new ones.

If you want to find some issues to start off with, please, [filter](https://github.com/JetBrains-Research/Lupa/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22) issues by the `good first issue` label.

Please, add a comment to the issue, if you're starting work on it.

It is important to add comments to the new core or common functionality as well as descriptions 
for running new analyzers and examples of its work.
This will help other developers and users to use them correctly.

Please, use the Kotlin language for new changes.

## Submitting patches

The best way to submit a patch is to [fork the project on GitHub](https://help.github.com/articles/fork-a-repo/) 
and then send us a [pull request](https://help.github.com/articles/creating-a-pull-request/) 
to the `main` branch via [GitHub](https://github.com).

If you create your own fork, it might help to enable rebase by default
when you pull by executing
``` bash
git config --global pull.rebase true
```
This will avoid your local repo having too many merge commits
which will help keep your pull request simple and easy to apply.

## Checklist

Before submitting the pull request, make sure that you can say "YES" to each point in this short checklist:

- You provided the link to the related issue(s) from the repository;
- You made a reasonable amount of changes related only to the provided issues;
- You can explain changes made in the pull request;
- You ran the build locally and verified new functionality/analyzers;
- You ran related tests locally (or add new ones) and they passed;
- You don't have code-style problems according the GitHub Actions 
  (for [Kotlin](https://github.com/JetBrains-Research/Lupa/blob/main/.github/workflows/kotlin-build.yml) 
  and for [Python](https://github.com/JetBrains-Research/Lupa/blob/main/.github/workflows/python-build.yml));
- You do not have merge conflicts in the pull request.
