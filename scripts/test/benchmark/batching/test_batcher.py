from pathlib import Path
from typing import Dict, List, Type

import pytest

from plugin_runner.batcher import (
    BatcherArgument,
    DummyBatcher,
    OneDimensionalAnyFitBatcher,
    OneDimensionalBestFitDecreasingBatcher,
    OneDimensionalFirstFitDecreasingBatcher,
    OneDimensionalNextFitDecreasingBatcher,
    OneDimensionalWorstFitDecreasingBatcher,
)
from plugin_runner.batching_config import ConfigField
from benchmark.metrics_collection.metrics import MetricName


COMMON_PROJECTS = {
    Path('project0'): {MetricName.FILE_SIZE.value: 1, MetricName.NUMBER_OF_FILES.value: 9},
    Path('project1'): {MetricName.FILE_SIZE.value: 8, MetricName.NUMBER_OF_FILES.value: 5},
    Path('project2'): {MetricName.FILE_SIZE.value: 3, MetricName.NUMBER_OF_FILES.value: 6},
    Path('project3'): {MetricName.FILE_SIZE.value: 9, MetricName.NUMBER_OF_FILES.value: 8},
    Path('project4'): {MetricName.FILE_SIZE.value: 2, MetricName.NUMBER_OF_FILES.value: 7},
    Path('project5'): {MetricName.FILE_SIZE.value: 9, MetricName.NUMBER_OF_FILES.value: 3},
    Path('project6'): {MetricName.FILE_SIZE.value: 7, MetricName.NUMBER_OF_FILES.value: 1},
    Path('oversized_project'): {MetricName.FILE_SIZE.value: 5, MetricName.NUMBER_OF_FILES.value: 20},
    Path('project_without_number_of_files'): {MetricName.FILE_SIZE.value: 6},
}

EMPTY_BATCH_CONSTRAINTS = {}
SINGLE_BATCH_CONSTRAINTS = {MetricName.NUMBER_OF_FILES.value: 10}
SEVERAL_BATCH_CONSTRAINTS = {MetricName.NUMBER_OF_FILES.value: 10, MetricName.FILE_SIZE.value: 15}

EMPTY_KWARGS = {}


ONE_DIMENSIONAL_ANY_FIT_BATCHER_TEST_DATA = [
    # Empty project list
    (OneDimensionalFirstFitDecreasingBatcher, {}, SINGLE_BATCH_CONSTRAINTS, EMPTY_KWARGS, []),
    (OneDimensionalBestFitDecreasingBatcher, {}, SINGLE_BATCH_CONSTRAINTS, EMPTY_KWARGS, []),
    (OneDimensionalWorstFitDecreasingBatcher, {}, SINGLE_BATCH_CONSTRAINTS, EMPTY_KWARGS, []),
    (OneDimensionalNextFitDecreasingBatcher, {}, SINGLE_BATCH_CONSTRAINTS, EMPTY_KWARGS, []),
    (
        OneDimensionalNextFitDecreasingBatcher,
        {},
        SINGLE_BATCH_CONSTRAINTS,
        {BatcherArgument.MAX_OPEN_BATCHES.value: 3},
        [],
    ),
    # Non-empty project list
    (
        OneDimensionalFirstFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BATCH_CONSTRAINTS,
        EMPTY_KWARGS,
        [
            [Path('project4'), Path('project5')],
            [Path('project0'), Path('project6')],
            [Path('project3')],
            [Path('project2')],
            [Path('project1')],
        ],
    ),
    (
        OneDimensionalBestFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BATCH_CONSTRAINTS,
        EMPTY_KWARGS,
        [
            [Path('project4'), Path('project5')],
            [Path('project0'), Path('project6')],
            [Path('project3')],
            [Path('project2')],
            [Path('project1')],
        ],
    ),
    (
        OneDimensionalWorstFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BATCH_CONSTRAINTS,
        EMPTY_KWARGS,
        [
            [Path('project2'), Path('project6')],
            [Path('project4')],
            [Path('project1'), Path('project5')],
            [Path('project3')],
            [Path('project0')],
        ],
    ),
    (
        OneDimensionalNextFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BATCH_CONSTRAINTS,
        EMPTY_KWARGS,
        [
            [Path('project0')],
            [Path('project3')],
            [Path('project4')],
            [Path('project2')],
            [Path('project1'), Path('project5'), Path('project6')],
        ],
    ),
    (
        OneDimensionalNextFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BATCH_CONSTRAINTS,
        {BatcherArgument.MAX_OPEN_BATCHES.value: 3},
        [
            [Path('project0')],
            [Path('project3')],
            [Path('project4'), Path('project5')],
            [Path('project2'), Path('project6')],
            [Path('project1')],
        ],
    ),
    # Non-empty project list without ignoring oversized projects
    (
        OneDimensionalFirstFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BATCH_CONSTRAINTS,
        {ConfigField.IGNORE_OVERSIZED_PROJECTS.value: False},
        [
            [Path('project4'), Path('project5')],
            [Path('project0'), Path('project6')],
            [Path('project3')],
            [Path('project2')],
            [Path('project1')],
            [Path('oversized_project')],
        ],
    ),
    (
        OneDimensionalBestFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BATCH_CONSTRAINTS,
        {ConfigField.IGNORE_OVERSIZED_PROJECTS.value: False},
        [
            [Path('project4'), Path('project5')],
            [Path('project0'), Path('project6')],
            [Path('project3')],
            [Path('project2')],
            [Path('project1')],
            [Path('oversized_project')],
        ],
    ),
    (
        OneDimensionalWorstFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BATCH_CONSTRAINTS,
        {ConfigField.IGNORE_OVERSIZED_PROJECTS.value: False},
        [
            [Path('project2'), Path('project6')],
            [Path('project4')],
            [Path('project1'), Path('project5')],
            [Path('project3')],
            [Path('project0')],
            [Path('oversized_project')],
        ],
    ),
    (
        OneDimensionalNextFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BATCH_CONSTRAINTS,
        {ConfigField.IGNORE_OVERSIZED_PROJECTS.value: False},
        [
            [Path('project0')],
            [Path('project3')],
            [Path('project4')],
            [Path('project2')],
            [Path('project1'), Path('project5'), Path('project6')],
            [Path('oversized_project')],
        ],
    ),
    (
        OneDimensionalNextFitDecreasingBatcher,
        COMMON_PROJECTS,
        SINGLE_BATCH_CONSTRAINTS,
        {ConfigField.IGNORE_OVERSIZED_PROJECTS.value: False, BatcherArgument.MAX_OPEN_BATCHES.value: 3},
        [
            [Path('project0')],
            [Path('project3')],
            [Path('project4'), Path('project5')],
            [Path('project2'), Path('project6')],
            [Path('project1')],
            [Path('oversized_project')],
        ],
    ),
]


@pytest.mark.parametrize(
    ('batcher', 'projects', 'batches_constraints', 'kwargs', 'expected_batches'),
    ONE_DIMENSIONAL_ANY_FIT_BATCHER_TEST_DATA,
)
def test_one_dimensional_any_fit_batcher(
    batcher: Type[OneDimensionalAnyFitBatcher],
    projects: Dict[Path, Dict[str, int]],
    batches_constraints: Dict[str, int],
    kwargs: Dict,
    expected_batches: List[List[str]],
) -> None:
    assert batcher.split_into_batches(projects, batches_constraints, **kwargs) == expected_batches


ONE_DIMENSIONAL_ANY_FIT_BATCHER_INVALID_BATCH_CONSTRAINTS_TEST_DATA = [
    # Empty batch constraints
    (OneDimensionalFirstFitDecreasingBatcher, EMPTY_BATCH_CONSTRAINTS),
    (OneDimensionalBestFitDecreasingBatcher, EMPTY_BATCH_CONSTRAINTS),
    (OneDimensionalWorstFitDecreasingBatcher, EMPTY_BATCH_CONSTRAINTS),
    (OneDimensionalNextFitDecreasingBatcher, EMPTY_BATCH_CONSTRAINTS),
    # Several batch constraints
    (OneDimensionalFirstFitDecreasingBatcher, SEVERAL_BATCH_CONSTRAINTS),
    (OneDimensionalBestFitDecreasingBatcher, SEVERAL_BATCH_CONSTRAINTS),
    (OneDimensionalWorstFitDecreasingBatcher, SEVERAL_BATCH_CONSTRAINTS),
    (OneDimensionalNextFitDecreasingBatcher, SEVERAL_BATCH_CONSTRAINTS),
]


@pytest.mark.parametrize(
    ('batcher', 'batch_constraints'),
    ONE_DIMENSIONAL_ANY_FIT_BATCHER_INVALID_BATCH_CONSTRAINTS_TEST_DATA,
)
def test_one_dimensional_any_fit_batcher_invalid_batch_constraints(
    batcher: Type[OneDimensionalAnyFitBatcher],
    batch_constraints: Dict[str, int],
) -> None:
    with pytest.raises(ValueError):
        batcher.split_into_batches(COMMON_PROJECTS, batch_constraints)


DUMMY_BATCHER_TEST_DATA = [
    ([], EMPTY_KWARGS, []),
    ([], {BatcherArgument.BATCH_SIZE.value: 4}, []),
    (
        COMMON_PROJECTS,
        EMPTY_KWARGS,
        [
            [
                Path('project0'),
                Path('project1'),
                Path('project2'),
                Path('project3'),
                Path('project4'),
                Path('project5'),
                Path('project6'),
                Path('oversized_project'),
                Path('project_without_number_of_files'),
            ],
        ],
    ),
    (
        COMMON_PROJECTS,
        {BatcherArgument.BATCH_SIZE.value: 4},
        [
            [Path('project0'), Path('project1'), Path('project2'), Path('project3')],
            [Path('project4'), Path('project5'), Path('project6'), Path('oversized_project')],
            [Path('project_without_number_of_files')],
        ],
    ),
    (
        COMMON_PROJECTS,
        {BatcherArgument.BATCH_SIZE.value: 10},
        [
            [
                Path('project0'),
                Path('project1'),
                Path('project2'),
                Path('project3'),
                Path('project4'),
                Path('project5'),
                Path('project6'),
                Path('oversized_project'),
                Path('project_without_number_of_files'),
            ],
        ],
    ),
]


@pytest.mark.parametrize(('projects', 'kwargs', 'expected_batches'), DUMMY_BATCHER_TEST_DATA)
def test_dummy_batcher(
    projects: Dict[Path, Dict[str, int]],
    kwargs: Dict,
    expected_batches: List[List[str]],
) -> None:
    assert DummyBatcher.split_into_batches(projects, {}, **kwargs) == expected_batches
