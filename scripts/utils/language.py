import os
from collections import defaultdict
from enum import Enum, unique
from pathlib import Path
from typing import Dict, List, Optional


@unique
class Language(Enum):
    KOTLIN = 'kotlin'
    PYTHON = 'python'
    JSON = 'json'
    YAML = 'yaml'
    DOCKERFILE = 'dockerfile'
    TEXT = 'text'
    UNKNOWN = 'unknown'

    @staticmethod
    def from_file_path(file_path: Path) -> 'Language':
        if file_path.name == 'Dockerfile' or file_path.name.endswith('.dockerfile'):
            return Language.DOCKERFILE

        extension = Extension.from_file_path(file_path)
        if extension is None:
            return Language.UNKNOWN

        return _EXTENSIONS_TO_LANGUAGE.get(extension, Language.UNKNOWN)


@unique
class Extension(Enum):
    # Kotlin
    KT = '.kt'
    KTS = '.kts'
    KTM = '.ktm'
    # Python
    PY = '.py'
    # YAML
    YAML = '.yaml'
    YML = '.yml'
    # JSON
    JSON = '.json'
    JSONL = '.jsonl'
    # Text
    TXT = '.txt'

    @staticmethod
    def from_file_path(file_path: Path) -> Optional['Extension']:
        try:
            return Extension(os.path.splitext(file_path)[1])
        except ValueError:
            return None


_EXTENSIONS_TO_LANGUAGE = {
    Extension.KT: Language.KOTLIN,
    Extension.KTS: Language.KOTLIN,
    Extension.KTM: Language.KOTLIN,
    Extension.PY: Language.PYTHON,
    Extension.YAML: Language.YAML,
    Extension.YML: Language.YAML,
    Extension.JSON: Language.JSON,
    Extension.JSONL: Language.JSON,
    Extension.TXT: Language.TEXT,
}


def group_files_by_language(files: List[Path]) -> Dict[Language, List[Path]]:
    files_by_language = defaultdict(list)
    for file in files:
        files_by_language[Language.from_file_path(file)].append(file)

    return files_by_language
