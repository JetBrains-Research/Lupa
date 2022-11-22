from typing import Dict, List, Type

import pytest

from benchmark.batching.batcher import (
    OneDimensionalAnyFitBatcher,
    OneDimensionalBestFitDecreasingBatcher,
    OneDimensionalFirstFitDecreasingBatcher,
    OneDimensionalNextFitDecreasingBatcher,
    OneDimensionalWorstFitDecreasingBatcher,
)
from benchmark.batching.config import ConfigField
from benchmark.metrics_collection.metrics import MetricName


COMMON_PROJECTS = {
    'project0': {MetricName.FILE_SIZE: 1, MetricName.NUMBER_OF_FILES: 9},
    'project1': {MetricName.FILE_SIZE: 8, MetricName.NUMBER_OF_FILES: 5},
    'project2': {MetricName.FILE_SIZE: 3, MetricName.NUMBER_OF_FILES: 6},
    'project3': {MetricName.FILE_SIZE: 9, MetricName.NUMBER_OF_FILES: 8},
    'project4': {MetricName.FILE_SIZE: 2, MetricName.NUMBER_OF_FILES: 7},
    'project5': {MetricName.FILE_SIZE: 9, MetricName.NUMBER_OF_FILES: 3},
    'project6': {MetricName.FILE_SIZE: 7, MetricName.NUMBER_OF_FILES: 1},
    'oversized_project': {MetricName.FILE_SIZE: 5, MetricName.NUMBER_OF_FILES: 20},
    'project_without_number_of_files': {MetricName.FILE_SIZE: 6},
}

EMPTY_BIN_CONSTRAINTS = {}
SINGLE_BIN_CONSTRAINTS = {MetricName.NUMBER_OF_FILES: 10}
SEVERAL_BIN_CONSTRAINTS = {MetricName.NUMBER_OF_FILES: 10, MetricName.FILE_SIZE: 15}

EMPTY_KWARGS = {}


ONE_DIMENSIONAL_ANY_FIT_BATCHER_TEST_DATA = [
    # Empty project list
    (OneDimensionalFirstFitDecreasingBatcher, {}, SINGLE_BIN_CONSTRAINTS, EMPTY_KWARGS, []),
    (OneDimensionalBestFitDecreasingBatcher, {}, SINGLE_BIN_CONSTRAINTS, EMPTY_KWARGS, []),
    (OneDimensionalWorstFitDecreasingBatcher, {}, SINGLE_BIN_CONSTRAINTS, EMPTY_KWARGS, []),
    (OneDimensionalNextFitDecreasingBatcher, {}, SINGLE_BIN_CONSTRAINTS, EMPTY_KWARGS, []),
    (OneDimensionalNextFitDecreasingBatcher, {}, SINGLE_BIN_CONSTRAINTS, {ConfigField.MAX_OPEN_BINS.value: 3}, []),
    # Non-empty project list
    (
        OneDimensionalFirstFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BIN_CONSTRAINTS,
        EMPTY_KWARGS,
        [['project4', 'project5'], ['project0', 'project6'], ['project3'], ['project2'], ['project1']],
    ),
    (
        OneDimensionalBestFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BIN_CONSTRAINTS,
        EMPTY_KWARGS,
        [['project4', 'project5'], ['project0', 'project6'], ['project3'], ['project2'], ['project1']],
    ),
    (
        OneDimensionalWorstFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BIN_CONSTRAINTS,
        EMPTY_KWARGS,
        [['project2', 'project6'], ['project4'], ['project1', 'project5'], ['project3'], ['project0']],
    ),
    (
        OneDimensionalNextFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BIN_CONSTRAINTS,
        EMPTY_KWARGS,
        [['project0'], ['project3'], ['project4'], ['project2'], ['project1', 'project5', 'project6']],
    ),
    (
        OneDimensionalNextFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BIN_CONSTRAINTS,
        {ConfigField.MAX_OPEN_BINS.value: 3},
        [['project0'], ['project3'], ['project4', 'project5'], ['project2', 'project6'], ['project1']],
    ),
    # Non-empty project list without ignoring oversized projects
    (
        OneDimensionalFirstFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BIN_CONSTRAINTS,
        {ConfigField.IGNORE_OVERSIZED_PROJECTS.value: False},
        [
            ['project4', 'project5'],
            ['project0', 'project6'],
            ['project3'],
            ['project2'],
            ['project1'],
            ['oversized_project'],
        ],
    ),
    (
        OneDimensionalBestFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BIN_CONSTRAINTS,
        {ConfigField.IGNORE_OVERSIZED_PROJECTS.value: False},
        [
            ['project4', 'project5'],
            ['project0', 'project6'],
            ['project3'],
            ['project2'],
            ['project1'],
            ['oversized_project'],
        ],
    ),
    (
        OneDimensionalWorstFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BIN_CONSTRAINTS,
        {ConfigField.IGNORE_OVERSIZED_PROJECTS.value: False},
        [
            ['project2', 'project6'],
            ['project4'],
            ['project1', 'project5'],
            ['project3'],
            ['project0'],
            ['oversized_project'],
        ],
    ),
    (
        OneDimensionalNextFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BIN_CONSTRAINTS,
        {ConfigField.IGNORE_OVERSIZED_PROJECTS.value: False},
        [
            ['project0'],
            ['project3'],
            ['project4'],
            ['project2'],
            ['project1', 'project5', 'project6'],
            ['oversized_project'],
        ],
    ),
    (
        OneDimensionalNextFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BIN_CONSTRAINTS,
        {ConfigField.IGNORE_OVERSIZED_PROJECTS.value: False, ConfigField.MAX_OPEN_BINS.value: 3},
        [
            ['project0'],
            ['project3'],
            ['project4', 'project5'],
            ['project2', 'project6'],
            ['project1'],
            ['oversized_project'],
        ],
    ),
]


@pytest.mark.parametrize(
    ('batcher', 'projects', 'metric_constraints', 'kwargs', 'expected_bins'),
    ONE_DIMENSIONAL_ANY_FIT_BATCHER_TEST_DATA,
)
def test_one_dimensional_any_fit_batcher(
    batcher: Type[OneDimensionalAnyFitBatcher],
    projects: Dict[str, Dict[MetricName, int]],
    metric_constraints: Dict[MetricName, int],
    kwargs: Dict,
    expected_bins: List[List[str]],
) -> None:
    assert batcher().split_into_batches(projects, metric_constraints, **kwargs) == expected_bins


ONE_DIMENSIONAL_ANY_FIT_BATCHER_INVALID_METRIC_CONSTRAINTS_TEST_DATA = [
    # Empty bin constraints
    (OneDimensionalFirstFitDecreasingBatcher, EMPTY_BIN_CONSTRAINTS),
    (OneDimensionalBestFitDecreasingBatcher, EMPTY_BIN_CONSTRAINTS),
    (OneDimensionalWorstFitDecreasingBatcher, EMPTY_BIN_CONSTRAINTS),
    (OneDimensionalNextFitDecreasingBatcher, EMPTY_BIN_CONSTRAINTS),
    # Several bin constraints
    (OneDimensionalFirstFitDecreasingBatcher, SEVERAL_BIN_CONSTRAINTS),
    (OneDimensionalBestFitDecreasingBatcher, SEVERAL_BIN_CONSTRAINTS),
    (OneDimensionalWorstFitDecreasingBatcher, SEVERAL_BIN_CONSTRAINTS),
    (OneDimensionalNextFitDecreasingBatcher, SEVERAL_BIN_CONSTRAINTS),
]


@pytest.mark.parametrize(
    ('batcher', 'metric_constraints'),
    ONE_DIMENSIONAL_ANY_FIT_BATCHER_INVALID_METRIC_CONSTRAINTS_TEST_DATA,
)
def test_one_dimensional_any_fit_batcher_invalid_metric_constraints(
    batcher: Type[OneDimensionalAnyFitBatcher],
    metric_constraints: Dict[MetricName, int],
) -> None:
    with pytest.raises(ValueError):
        batcher().split_into_batches(COMMON_PROJECTS, {})
