import re
from abc import ABC, abstractmethod
from enum import Enum, unique
from os.path import getsize
from pathlib import Path
from typing import Dict, List, Optional, Set, Type

from utils.language import Language, group_files_by_language
from utils.python.requirements_utils import PYTHON_REQUIREMENTS_FILE_NAME_REGEXP, gather_requirements_from_file


@unique
class MetricArgument(Enum):
    IGNORE_EMPTY_LINES = 'ignore_empty_lines'
    IGNORE_COMMENTS = 'ignore_comments'


class Metric(ABC):
    """Abstract class for a metric."""

    @classmethod
    @abstractmethod
    def collect_from_files(cls, files: List[Path], **kwargs) -> Dict[Language, int]:
        """
        Collect a metric from ``files``.

        :param files: List of the file paths. All files must exist.
        :param kwargs: Additional arguments. By default, metric doesn't require any additional arguments.

        :return: Metric values grouped by language.
        """
        raise NotImplementedError


class NumberOfFiles(Metric):
    @classmethod
    def collect_from_files(cls, files: List[Path], **kwargs) -> Dict[Language, int]:
        """
        Collect the number of ``files``.

        :param files: List of the file paths. All files must exist.
        :param kwargs: Additional arguments. Not needed.

        :return: Number of files grouped by language.
        """
        return {language: len(file_group) for language, file_group in group_files_by_language(files).items()}


class NumberOfLines(Metric):
    @classmethod
    def collect_from_files(cls, files: List[Path], **kwargs) -> Dict[Language, int]:
        """
        Collect the number of lines from ``files``.

        List of keyword arguments:

        - ``ignore_empty_lines`` -- Is it necessary to ignore empty lines. By default, false.
        - ``ignore_comments`` -- Is it necessary to ignore comments. By default, false. Only Python and Kotlin comment
          processing is supported.

        :param files: List of the file paths. All files must exist.
        :param kwargs: Additional arguments. See above.

        :return: Number of lines grouped by language.
        """
        ignore_empty_lines = kwargs.get(MetricArgument.IGNORE_EMPTY_LINES.value, False)
        ignore_comments = kwargs.get(MetricArgument.IGNORE_COMMENTS.value, False)

        lines_by_language = {}
        for language, file_group in group_files_by_language(files).items():
            lines_by_language[language] = 0
            for file in file_group:
                with open(file, encoding='utf-8', mode='r', errors='ignore') as f:
                    for line in f:
                        if ignore_empty_lines and not line.strip():
                            continue

                        if ignore_comments and cls._is_single_line_comment(line):
                            continue

                        # TODO: add multi-line comments support

                        lines_by_language[language] += 1

        return lines_by_language

    @staticmethod
    def _is_single_line_comment(line: str) -> bool:
        stripped_line = line.strip()
        return (
            stripped_line.startswith(('#', '//'))
            or (stripped_line.startswith('"""') and stripped_line.endswith('"""'))
            or (stripped_line.startswith("'''") and stripped_line.endswith("'''"))
            or (stripped_line.startswith('/*') and stripped_line.endswith('*/'))
        )


class FileSize(Metric):
    @classmethod
    def collect_from_files(cls, files: List[Path], **kwargs) -> Dict[Language, int]:
        """
        Collect the size, in bytes, of ``files``.

        :param files: List of the file paths. All files must exist.
        :param kwargs: Additional arguments. Not needed.

        :return: File sizes in bytes grouped by language.
        """
        return {
            language: sum(getsize(file) for file in file_group)
            for language, file_group in group_files_by_language(files).items()
        }


class NumberOfDependencies(Metric):
    _KOTLIN_CONFIGURATION_ACCESSOR_KEYWORDS = [
        'annotationprocessor',
        'api',
        'archives',
        'compileclasspath',
        'compileonly',
        'default',
        'implementation',
        'intransitivedependenciesmetadata',
        'kotlincompilerclasspath',
        'kotlincompilerpluginclasspath',
        'kotlincompilerpluginclasspathmain',
        'kotlincompilerpluginclasspathtest',
        'kotlinklibcommonizerclasspath',
        'kotlinnativecompilerpluginclasspath',
        'kotlinscriptdef',
        'mainsourceelements',
        'runtimeclasspath',
        'runtimeelements',
        'runtimeonly',
        'sourceartifacts',
        'testresultselementsfortest',
    ]

    @classmethod
    def collect_from_files(cls, files: List[Path], **kwargs) -> Dict[Language, int]:
        """
        Collect the number of dependencies.

        Finds dependencies in ``files`` for the following languages:

        - Python
        - Kotlin

        :param files: List of the file paths. All files must exist.
        :param kwargs: Additional arguments. Not needed.

        :return: Number of dependencies by language.
        """
        number_of_requirements = {
            Language.PYTHON: cls._collect_number_of_python_requirements(files),
            Language.KOTLIN: cls._collect_number_of_kotlin_requirements(files),
        }

        return {
            langauge: number for langauge, number in number_of_requirements.items() if number is not None
        }

    @staticmethod
    def _collect_number_of_python_requirements(files: List[Path]) -> Optional[int]:
        """
        Collect the number of python requirements from ``files``.

        To do this, try to parse the files with the name like ``requirements.txt`` and count the number of requirements.

        :param files: List of the file paths.
        :return: Number of python requirements. If no files with dependencies are found, None will be returned
        """
        requirements_files = list(filter(
            lambda file: re.match(PYTHON_REQUIREMENTS_FILE_NAME_REGEXP, file.name) is not None,
            files,
        ))

        if not requirements_files:
            return None

        return sum(len(gather_requirements_from_file(file)) for file in requirements_files)

    @classmethod
    def _collect_number_of_kotlin_requirements(cls, files: List[Path]) -> Optional[int]:
        """
        Collect the number of kotlin requirements from ``files``.

        To do this, count the number of occurrences of the words like 'implementation', 'compilationOnly', 'api', etc.
        in the ``build.gradle.kts`` and ``build.gradle`` files.

        :param files: List of the file paths.
        :return: Number of kotlin requirements. If no files with dependencies are found, None will be returned
        """
        requirements_files = list(filter(lambda file: file.name in ('build.gradle.kts', 'build.gradle'), files))
        if not requirements_files:
            return None

        number_of_requirements = 0
        for requirements_file in requirements_files:
            with open(requirements_file, mode='r', encoding='utf-8', errors='ignore') as f:
                content = f.read().lower()
                number_of_requirements += sum(
                    content.count(statement) for statement in cls._KOTLIN_CONFIGURATION_ACCESSOR_KEYWORDS
                )

        return number_of_requirements


@unique
class MetricName(Enum):
    NUMBER_OF_FILES = 'number_of_files'
    NUMBER_OF_LINES = 'number_of_lines'
    FILE_SIZE = 'file_size'
    NUMBER_OF_DEPENDENCIES = 'number_of_dependencies'

    def execute(self, files: List[Path], **kwargs) -> Dict[Language, int]:
        return _METRIC_NAME_TO_CLASS[self].collect_from_files(files, **kwargs)

    @classmethod
    def values(cls) -> Set[str]:
        return {metric.value for metric in cls}


_METRIC_NAME_TO_CLASS: Dict[MetricName, Type[Metric]] = {
    MetricName.FILE_SIZE: FileSize,
    MetricName.NUMBER_OF_LINES: NumberOfLines,
    MetricName.NUMBER_OF_FILES: NumberOfFiles,
    MetricName.NUMBER_OF_DEPENDENCIES: NumberOfDependencies,
}
