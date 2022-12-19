from dataclasses import dataclass
from typing import List

from utils.language import Language


@dataclass
class Analyzer:
    name: str
    output_file: str
    language: Language

    @staticmethod
    def get_analyzers_names(analyzers: List['Analyzer']) -> List[str]:
        return [analyzer.name for analyzer in analyzers]

    @staticmethod
    def get_analyzer_by_name(analyzers: List['Analyzer'], name: str) -> 'Analyzer':
        analyzers = list(filter(lambda a: a.name == name, analyzers))
        if len(analyzers) != 1:
            raise ValueError(f'Error during searching analyzer with name {name} '
                             f'in the analyzers list: {Analyzer.get_analyzers_names(analyzers)}')
        return analyzers[0]


AVAILABLE_ANALYZERS = [
    Analyzer('kotlin-dependencies', 'import_directives_data.csv', Language.KOTLIN),
    Analyzer('kotlin-clones', 'method_data.txt', Language.KOTLIN),
    Analyzer('kotlin-ranges', 'ranges_data.csv', Language.KOTLIN),
    Analyzer('kotlin-project-tags', 'project_tags_data.csv', Language.KOTLIN),
    Analyzer('kotlin-gradle-dependencies', 'gradle_dependencies_data.csv', Language.KOTLIN),
    Analyzer('kotlin-gradle-dependencies-by-modules', 'gradle_dependencies_by_modules_data.csv', Language.KOTLIN),
    Analyzer('kotlin-gradle-properties', 'gradle_properties_data.csv', Language.KOTLIN),
    Analyzer('kotlin-gradle-plugins', 'gradle_plugins_data.csv', Language.KOTLIN),
    Analyzer('kotlin-project-metrics', 'project_metrics_data.csv', Language.KOTLIN),
    Analyzer('java-reflections', 'reflection_data.csv', Language.KOTLIN),
    Analyzer('kotlin-stdlib-interfaces', 'stdlib_interfaces_data.csv', Language.KOTLIN),

    Analyzer('python-imports', 'import_statements_data.csv', Language.PYTHON),
    Analyzer('python-call-expressions', 'call_expressions_data.csv', Language.PYTHON),
]
