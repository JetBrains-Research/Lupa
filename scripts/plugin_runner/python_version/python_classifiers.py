from collections import defaultdict
from enum import Enum, unique
from typing import Dict, Optional, Set


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
    PYTHON_3_0 = 'Programming Language :: Python :: 3.0'
    PYTHON_3_1 = 'Programming Language :: Python :: 3.1'
    PYTHON_3_2 = 'Programming Language :: Python :: 3.2'
    PYTHON_3_3 = 'Programming Language :: Python :: 3.3'
    PYTHON_3_4 = 'Programming Language :: Python :: 3.4'
    PYTHON_3_5 = 'Programming Language :: Python :: 3.5'
    PYTHON_3_6 = 'Programming Language :: Python :: 3.6'
    PYTHON_3_7 = 'Programming Language :: Python :: 3.7'
    PYTHON_3_8 = 'Programming Language :: Python :: 3.8'
    PYTHON_3_9 = 'Programming Language :: Python :: 3.9'
    PYTHON_3_10 = 'Programming Language :: Python :: 3.10'
    PYTHON_3_11 = 'Programming Language :: Python :: 3.11'

    # Python 2 classifiers
    PYTHON_2 = 'Programming Language :: Python :: 2'
    PYTHON_2_3 = 'Programming Language :: Python :: 2.3'
    PYTHON_2_4 = 'Programming Language :: Python :: 2.4'
    PYTHON_2_5 = 'Programming Language :: Python :: 2.5'
    PYTHON_2_6 = 'Programming Language :: Python :: 2.6'
    PYTHON_2_7 = 'Programming Language :: Python :: 2.7'

    @classmethod
    def _get_classifier_to_version_dict(cls) -> Dict[str, PythonVersion]:
        return {
            # Python Only classifiers
            cls.PYTHON_3_ONLY.value: PythonVersion.PYTHON_3,
            cls.PYTHON_2_ONLY.value: PythonVersion.PYTHON_2,
            # Python 3 classifiers
            cls.PYTHON_3.value: PythonVersion.PYTHON_3,
            cls.PYTHON_3_0.value: PythonVersion.PYTHON_3,
            cls.PYTHON_3_1.value: PythonVersion.PYTHON_3,
            cls.PYTHON_3_2.value: PythonVersion.PYTHON_3,
            cls.PYTHON_3_3.value: PythonVersion.PYTHON_3,
            cls.PYTHON_3_4.value: PythonVersion.PYTHON_3,
            cls.PYTHON_3_5.value: PythonVersion.PYTHON_3,
            cls.PYTHON_3_6.value: PythonVersion.PYTHON_3,
            cls.PYTHON_3_7.value: PythonVersion.PYTHON_3,
            cls.PYTHON_3_8.value: PythonVersion.PYTHON_3,
            cls.PYTHON_3_9.value: PythonVersion.PYTHON_3,
            cls.PYTHON_3_10.value: PythonVersion.PYTHON_3,
            cls.PYTHON_3_11.value: PythonVersion.PYTHON_3,
            # Python 2 classifiers
            cls.PYTHON_2.value: PythonVersion.PYTHON_2,
            cls.PYTHON_2_3.value: PythonVersion.PYTHON_2,
            cls.PYTHON_2_4.value: PythonVersion.PYTHON_2,
            cls.PYTHON_2_5.value: PythonVersion.PYTHON_2,
            cls.PYTHON_2_6.value: PythonVersion.PYTHON_2,
            cls.PYTHON_2_7.value: PythonVersion.PYTHON_2,
        }

    @classmethod
    def _get_version_to_classifiers_dict(cls) -> Dict[PythonVersion, Set[str]]:
        version_to_classifiers = defaultdict(set)
        for classifier, version in cls._get_classifier_to_version_dict().items():
            version_to_classifiers[version].add(classifier)
        return version_to_classifiers

    @classmethod
    def get_classifiers_by_version(cls, python_versions: PythonVersion) -> Set[str]:
        return cls._get_version_to_classifiers_dict().get(python_versions, {})

    @classmethod
    def get_version_by_classifier(cls, classifier: str) -> Optional[PythonVersion]:
        return cls._get_classifier_to_version_dict().get(classifier)

    @classmethod
    def get_versions_by_classifiers(cls, classifiers: Set[str]) -> Set[PythonVersion]:
        classifier_to_version = cls._get_classifier_to_version_dict()
        return {
            classifier_to_version[classifier] for classifier in classifiers.intersection(classifier_to_version.keys())
        }
