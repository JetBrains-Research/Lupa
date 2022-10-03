from test.benchmark.metrics_collection import COLLECT_PROJECT_METRICS_TEST_DATA_FOLDER
from typing import Dict, Set

from benchmark.metrics_collection.collect_project_metrics import collect_project_metrics
from benchmark.metrics_collection.metrics import Metric

import pytest


COLLECT_PROJECT_METRICS_TEST_DATA = [
    ('non_existent_project', None),
    (
        'project_with_one_language',
        {
            'file_size': {'python': 233, 'total': 233},
            'number_of_files': {'python': 2, 'total': 2},
            'number_of_lines': {'python': 15, 'total': 15},
        },
    ),
    (
        'project_with_multiple_languages',
        {
            'file_size': {'python': 233, 'kotlin': 228, 'total': 461},
            'number_of_files': {'python': 2, 'kotlin': 1, 'total': 3},
            'number_of_lines': {'python': 15, 'kotlin': 11, 'total': 26},
        },
    ),
    (
        'project_with_unknown_language',
        {
            'file_size': {'python': 233, 'kotlin': 228, 'unknown': 138, 'total': 599},
            'number_of_files': {'python': 2, 'kotlin': 1, 'unknown': 2, 'total': 5},
            'number_of_lines': {'python': 15, 'kotlin': 11, 'unknown': 4, 'total': 30},
        },
    ),
]


@pytest.mark.parametrize(('project', 'expected_values'), COLLECT_PROJECT_METRICS_TEST_DATA)
def test_collect_project_metrics(project: str, expected_values: Dict[str, Dict[str, int]]):
    assert collect_project_metrics(COLLECT_PROJECT_METRICS_TEST_DATA_FOLDER / project, set(Metric)) == expected_values


COLLECT_SEVERAL_PROJECT_METRICS_TEST_DATA = [
    (set(), {}),
    ({Metric.FILE_SIZE}, {'file_size': {'python': 233, 'kotlin': 228, 'unknown': 138, 'total': 599}}),
    ({Metric.NUMBER_OF_FILES}, {'number_of_files': {'python': 2, 'kotlin': 1, 'unknown': 2, 'total': 5}}),
    ({Metric.NUMBER_OF_LINES}, {'number_of_lines': {'python': 15, 'kotlin': 11, 'unknown': 4, 'total': 30}}),
    (
        {Metric.NUMBER_OF_FILES, Metric.NUMBER_OF_LINES},
        {
            'number_of_files': {'python': 2, 'kotlin': 1, 'unknown': 2, 'total': 5},
            'number_of_lines': {'python': 15, 'kotlin': 11, 'unknown': 4, 'total': 30},
        },
    ),
]


@pytest.mark.parametrize(('metrics', 'expected_values'), COLLECT_SEVERAL_PROJECT_METRICS_TEST_DATA)
def test_collect_several_project_metrics(metrics: Set[Metric], expected_values: Dict[str, Dict[str, int]]):
    project = COLLECT_PROJECT_METRICS_TEST_DATA_FOLDER / 'project_with_unknown_language'
    assert collect_project_metrics(project, metrics) == expected_values
