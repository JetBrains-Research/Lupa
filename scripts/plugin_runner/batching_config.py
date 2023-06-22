from enum import Enum, unique

from plugin_runner.batcher import BatcherArgument, BatcherName
from benchmark.metrics_collection.metrics import MetricName
from utils.language import Language


@unique
class ConfigField(Enum):
    BATCH_CONSTRAINTS = 'batch_constraints'

    LANGUAGE = 'language'
    IGNORE_OVERSIZED_PROJECTS = 'ignore_oversized_projects'
    METRIC = 'metric'

    BATCHER_CONFIG = 'batcher_config'
    NAME = 'name'


BATCHING_SCHEMA = {
    ConfigField.LANGUAGE.value: {
        'type': 'string',
        'required': False,
        'allowed': [*Language.values(), None],
        'dependencies': [ConfigField.METRIC.value, ConfigField.BATCH_CONSTRAINTS.value],
    },
    ConfigField.METRIC.value: {
        'type': 'string',
        'required': False,
        'allowed': MetricName.values(),
        'dependencies': [ConfigField.LANGUAGE.value, ConfigField.BATCH_CONSTRAINTS.value],
    },
    ConfigField.BATCH_CONSTRAINTS.value: {
        'type': 'dict',
        'required': False,
        'keysrules': {'type': 'string', 'allowed': MetricName.values()},
        'valuesrules': {'type': 'integer'},
        'dependencies': [ConfigField.LANGUAGE.value, ConfigField.METRIC.value],
    },
    ConfigField.IGNORE_OVERSIZED_PROJECTS.value: {'type': 'boolean', 'required': False},
    ConfigField.BATCHER_CONFIG.value: {
        'type': 'dict',
        'schema': {
            ConfigField.NAME.value: {'type': 'string', 'required': True, 'allowed': BatcherName.values()},
            BatcherArgument.BATCH_SIZE.value: {
                'type': 'integer',
                'required': False,
                'min': 1,
                'dependencies': {ConfigField.NAME.value: [BatcherName.DUMMY_BATCHER.value]},
            },
            BatcherArgument.MAX_OPEN_BATCHES.value: {
                'type': 'integer',
                'required': False,
                'min': 1,
                'dependencies': {ConfigField.NAME.value: [BatcherName.ONE_DIMENSIONAL_NEXT_FIT_DECREASING.value]},
            },
        },
    },
}
