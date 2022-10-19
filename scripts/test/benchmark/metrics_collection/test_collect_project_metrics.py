from test.benchmark.metrics_collection import COLLECT_PROJECT_METRICS_TEST_DATA_FOLDER
from typing import Dict

from benchmark.metrics_collection.collect_project_metrics import TOTAL_FIELD_NAME, collect_project_metrics
from benchmark.metrics_collection.metrics import MetricName

import pytest

from utils.language import Language

COMMON_CONFIG = {
    MetricName.FILE_SIZE: {},
    MetricName.NUMBER_OF_FILES: {},
    MetricName.NUMBER_OF_LINES: {},
    MetricName.NUMBER_OF_DEPENDENCIES: {},
}


COLLECT_PROJECT_METRICS_TEST_DATA = [
    ('non_existent_project', COMMON_CONFIG, None),
    (
        'project_with_one_language',
        COMMON_CONFIG,
        {
            MetricName.FILE_SIZE.value: {Language.PYTHON.value: 233, Language.TEXT.value: 28, TOTAL_FIELD_NAME: 261},
            MetricName.NUMBER_OF_FILES.value: {Language.PYTHON.value: 2, Language.TEXT.value: 1, TOTAL_FIELD_NAME: 3},
            MetricName.NUMBER_OF_LINES.value: {Language.PYTHON.value: 15, Language.TEXT.value: 2, TOTAL_FIELD_NAME: 17},
            MetricName.NUMBER_OF_DEPENDENCIES.value: {Language.PYTHON.value: 2, TOTAL_FIELD_NAME: 2},
        },
    ),
    (
        'project_with_multiple_languages',
        COMMON_CONFIG,
        {
            MetricName.FILE_SIZE.value: {
                Language.PYTHON.value: 233,
                Language.KOTLIN.value: 644,
                Language.TEXT.value: 28,
                TOTAL_FIELD_NAME: 905,
            },
            MetricName.NUMBER_OF_FILES.value: {
                Language.PYTHON.value: 2,
                Language.KOTLIN.value: 2,
                Language.TEXT.value: 1,
                TOTAL_FIELD_NAME: 5,
            },
            MetricName.NUMBER_OF_LINES.value: {
                Language.PYTHON.value: 15,
                Language.KOTLIN.value: 40,
                Language.TEXT.value: 2,
                TOTAL_FIELD_NAME: 57,
            },
            MetricName.NUMBER_OF_DEPENDENCIES.value: {
                Language.PYTHON.value: 2,
                Language.KOTLIN.value: 1,
                TOTAL_FIELD_NAME: 3,
            },
        },
    ),
    (
        'project_with_unknown_language',
        COMMON_CONFIG,
        {
            MetricName.FILE_SIZE.value: {
                Language.PYTHON.value: 233,
                Language.KOTLIN.value: 644,
                Language.TEXT.value: 28,
                Language.UNKNOWN.value: 138,
                TOTAL_FIELD_NAME: 1043,
            },
            MetricName.NUMBER_OF_FILES.value: {
                Language.PYTHON.value: 2,
                Language.KOTLIN.value: 2,
                Language.TEXT.value: 1,
                Language.UNKNOWN.value: 2,
                TOTAL_FIELD_NAME: 7,
            },
            MetricName.NUMBER_OF_LINES.value: {
                Language.PYTHON.value: 15,
                Language.KOTLIN.value: 40,
                Language.TEXT.value: 2,
                Language.UNKNOWN.value: 4,
                TOTAL_FIELD_NAME: 61,
            },
            MetricName.NUMBER_OF_DEPENDENCIES.value: {
                Language.PYTHON.value: 2,
                Language.KOTLIN.value: 1,
                TOTAL_FIELD_NAME: 3,
            },
        },
    ),
]


@pytest.mark.parametrize(('project', 'metric_configs', 'expected_values'), COLLECT_PROJECT_METRICS_TEST_DATA)
def test_collect_project_metrics(project: str, metric_configs: Dict, expected_values: Dict[str, Dict[str, int]]):
    assert (
        collect_project_metrics(COLLECT_PROJECT_METRICS_TEST_DATA_FOLDER / project, metric_configs) == expected_values
    )
