import os
from enum import Enum, unique
from pathlib import Path
from typing import Optional


@unique
class Language(Enum):
    KOTLIN = 'kotlin'
    PYTHON = 'python'
    UNKNOWN = 'unknown'

    @staticmethod
    def from_file_path(file_path: Path) -> 'Language':
        extension = Extension.from_file_path(file_path)
        if extension is None:
            return Language.UNKNOWN

        return _EXTENSIONS_TO_LANGUAGE.get(extension, Language.UNKNOWN)


@unique
class Extension(Enum):
    KT = '.kt'
    PY = '.py'

    @staticmethod
    def from_file_path(file_path: Path) -> Optional['Extension']:
        try:
            return Extension(os.path.splitext(file_path)[1])
        except ValueError:
            return None


_EXTENSIONS_TO_LANGUAGE = {
    Extension.KT: Language.KOTLIN,
    Extension.PY: Language.PYTHON,
}
