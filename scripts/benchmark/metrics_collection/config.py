from typing import Dict

from benchmark.metrics_collection.metrics import MetricArgument, MetricName

from cerberus import Validator
from cerberus.errors import ErrorList

SCHEMA = {
    'metrics': {
        'type': 'dict',
        'schema': {
            MetricName.NUMBER_OF_FILES.value: {
                'type': 'dict',
                'empty': True,
                'nullable': True,
                'schema': {},
            },
            MetricName.NUMBER_OF_DEPENDENCIES.value: {
                'type': 'dict',
                'empty': True,
                'nullable': True,
                'schema': {},
            },
            MetricName.FILE_SIZE.value: {
                'type': 'dict',
                'empty': True,
                'nullable': True,
                'schema': {},
            },
            MetricName.NUMBER_OF_LINES.value: {
                'type': 'dict',
                'empty': True,
                'nullable': True,
                'schema': {
                    MetricArgument.IGNORE_COMMENTS.value: {'type': 'boolean'},
                    MetricArgument.IGNORE_EMPTY_LINES.value: {'type': 'boolean'},
                },
            },
        },
    },
}


def check_config(config: Dict) -> ErrorList:
    v = Validator()
    v.validate(config, SCHEMA)
    return v.errors
