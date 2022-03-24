from enum import Enum, unique
from typing import Set


@unique
class PythonVersion(Enum):
    PYTHON_3 = 'PYTHON_3'
    PYTHON_2 = 'PYTHON_2'


@unique
class PythonClassifiers(Enum):
    """
    Python classifiers consistent with PyPI.

    See: https://pypi.org/classifiers/
    """

    # Python Only classifiers
    PYTHON_3_ONLY = 'Programming Language :: Python :: 3 :: Only'
    PYTHON_2_ONLY = 'Programming Language :: Python :: 2 :: Only'

    # Python 3 classifiers
    PYTHON_3 = 'Programming Language :: Python :: 3'
    Python_3_0 = 'Programming Language :: Python :: 3.0'
    Python_3_1 = 'Programming Language :: Python :: 3.1'
    Python_3_2 = 'Programming Language :: Python :: 3.2'
    Python_3_3 = 'Programming Language :: Python :: 3.3'
    Python_3_4 = 'Programming Language :: Python :: 3.4'
    Python_3_5 = 'Programming Language :: Python :: 3.5'
    Python_3_6 = 'Programming Language :: Python :: 3.6'
    Python_3_7 = 'Programming Language :: Python :: 3.7'
    Python_3_8 = 'Programming Language :: Python :: 3.8'
    Python_3_9 = 'Programming Language :: Python :: 3.9'
    Python_3_10 = 'Programming Language :: Python :: 3.10'
    Python_3_11 = 'Programming Language :: Python :: 3.11'

    # Python 2 classifiers
    PYTHON_2 = 'Programming Language :: Python :: 2'
    PYTHON_2_3 = 'Programming Language :: Python :: 2.3'
    PYTHON_2_4 = 'Programming Language :: Python :: 2.4'
    PYTHON_2_5 = 'Programming Language :: Python :: 2.5'
    PYTHON_2_6 = 'Programming Language :: Python :: 2.6'
    PYTHON_2_7 = 'Programming Language :: Python :: 2.7'

    @classmethod
    def get_classifiers_by_version(cls, python_versions: PythonVersion) -> Set[str]:
        version_to_classifiers = {
            PythonVersion.PYTHON_2: {
                cls.PYTHON_2_ONLY.value,
                cls.PYTHON_2.value,
                cls.PYTHON_2_3.value,
                cls.PYTHON_2_4.value,
                cls.PYTHON_2_5.value,
                cls.PYTHON_2_6.value,
                cls.PYTHON_2_7.value,
            },
            PythonVersion.PYTHON_3: {
                cls.PYTHON_3_ONLY.value,
                cls.PYTHON_3.value,
                cls.Python_3_0.value,
                cls.Python_3_1.value,
                cls.Python_3_2.value,
                cls.Python_3_3.value,
                cls.Python_3_4.value,
                cls.Python_3_5.value,
                cls.Python_3_6.value,
                cls.Python_3_7.value,
                cls.Python_3_8.value,
                cls.Python_3_9.value,
                cls.Python_3_10.value,
                cls.Python_3_11.value,
            },
        }

        return version_to_classifiers.get(python_versions, {})
