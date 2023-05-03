from enum import Enum, unique
from typing import List

from benchmark.metrics_collection.metrics import MetricName
from utils.language import Language


@unique
class ConfigField(Enum):
    SAMPLE_SIZE = 'sample_size'
    LANGUAGE = 'language'
    STRATA = 'strata'


# Consistent with: https://numpy.org/doc/stable/reference/generated/numpy.histogram_bin_edges.html
@unique
class BinsEstimator(Enum):
    AUTO = 'auto'
    FD = 'fd'
    DOANE = 'doane'
    SCOTT = 'scott'
    STONE = 'stone'
    RICE = 'rice'
    STURGES = 'sturges'
    SQRT = 'sqrt'

    @classmethod
    def values(cls) -> List[str]:
        return [estimator.value for estimator in cls]


SCHEMA = {
    ConfigField.SAMPLE_SIZE.value: {'required': True, 'type': 'integer', 'min': 1},
    ConfigField.LANGUAGE.value: {'required': True, 'nullable': True, 'type': 'string', 'allowed': Language.values()},
    ConfigField.STRATA.value: {
        'required': True,
        'type': 'dict',
        'keysrules': {'type': 'string', 'allowed': MetricName.values()},
        'valuesrules': {
            # For some reason, it's not enough to specify the "nullable" flag here,
            # you must also specify it in one of the schemes below.
            'nullable': True,
            'oneof': [
                {'type': 'integer', 'min': 1, 'nullable': True},
                {'type': 'string', 'allowed': BinsEstimator.values()},
                {'type': 'list', 'schema': {'type': 'number'}},
            ],
        },
    },
}
