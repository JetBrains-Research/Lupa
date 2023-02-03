from pathlib import Path
from tempfile import TemporaryDirectory
from typing import Dict, List

import pytest

from benchmark.metrics_collection.metrics import MetricName
from plugin_runner.analyzers import AVAILABLE_ANALYZERS, Analyzer
from plugin_runner.batch_processing import create_batches, split_into_batches
from plugin_runner.batcher import BatcherArgument, BatcherName
from plugin_runner.batching_config import ConfigField
from test.plugin_runner.batch_processing import COMMON_DATASET, DUMMY_CONFIG
from test.plugin_runner.batch_processing.command_builder import CommandBuilder
from utils.file_utils import get_file_content, get_subdirectories
from utils.language import Language
from utils.run_process_utils import run_in_subprocess

START_FROM_TEST_DATA = [
    (0, 9),  # start_from = 0, expected_number_of_projects = 8 projects + 1 header rows
    (3, 6),  # start_from = 3, expected_number_of_projects = 5 projects + 1 header rows
    (10, 0),  # start_from = 10, expected_number_of_projects = 0 projects + 0 header rows
]


@pytest.mark.parametrize(('start_from', 'expected_number_of_projects'), START_FROM_TEST_DATA)
def test_start_from(start_from: int, expected_number_of_projects: int) -> None:
    analyzer = Analyzer.get_analyzer_by_name(AVAILABLE_ANALYZERS, 'python-imports')

    with TemporaryDirectory() as tmpdir:
        command_builder = CommandBuilder(
            COMMON_DATASET,
            Path(tmpdir),
            DUMMY_CONFIG,
            analyzer.name,
            start_from=start_from,
        )

        run_in_subprocess(command_builder.build())

        output = get_file_content(Path(tmpdir) / analyzer.output_file).strip()
        assert len(output.splitlines()) == expected_number_of_projects


SPLIT_INTO_BATCHES_TEST_DATA = [
    (
        {
            ConfigField.BATCHER_CONFIG.value: {
                ConfigField.NAME.value: BatcherName.DUMMY_BATCHER.value,
                BatcherArgument.BATCH_SIZE.value: 1,
            },
        },
        [
            ['project_1'],
            ['project_2'],
            ['project_3'],
            ['project_4'],
            ['project_5'],
            ['project_6'],
            ['project_7'],
            ['project_without_metric_file'],
        ],
    ),
    (
        {
            ConfigField.BATCHER_CONFIG.value: {
                ConfigField.NAME.value: BatcherName.DUMMY_BATCHER.value,
                BatcherArgument.BATCH_SIZE.value: 3,
            },
        },
        [
            ['project_1', 'project_2', 'project_3'],
            ['project_4', 'project_5', 'project_6'],
            ['project_7', 'project_without_metric_file'],
        ],
    ),
    (
        {
            ConfigField.BATCHER_CONFIG.value: {
                ConfigField.NAME.value: BatcherName.ONE_DIMENSIONAL_NEXT_FIT_DECREASING.value,
            },
            ConfigField.METRIC.value: MetricName.FILE_SIZE.value,
            ConfigField.LANGUAGE.value: Language.PYTHON.value,
            ConfigField.BATCH_CONSTRAINTS.value: {MetricName.FILE_SIZE.value: 10},
        },
        [['project_7'], ['project_6'], ['project_3', 'project_5'], ['project_2', 'project_1']],
    ),
    (
        {
            ConfigField.BATCHER_CONFIG.value: {
                ConfigField.NAME.value: BatcherName.ONE_DIMENSIONAL_NEXT_FIT_DECREASING.value,
            },
            ConfigField.METRIC.value: MetricName.FILE_SIZE.value,
            ConfigField.LANGUAGE.value: Language.PYTHON.value,
            ConfigField.BATCH_CONSTRAINTS.value: {MetricName.FILE_SIZE.value: 10},
            ConfigField.IGNORE_OVERSIZED_PROJECTS.value: False,
        },
        [['project_7'], ['project_6'], ['project_3', 'project_5'], ['project_2', 'project_1'], ['project_4']],
    ),
    (
        {
            ConfigField.BATCHER_CONFIG.value: {
                ConfigField.NAME.value: BatcherName.ONE_DIMENSIONAL_NEXT_FIT_DECREASING.value,
                BatcherArgument.MAX_OPEN_BATCHES.value: 3,
            },
            ConfigField.METRIC.value: MetricName.FILE_SIZE.value,
            ConfigField.LANGUAGE.value: Language.PYTHON.value,
            ConfigField.BATCH_CONSTRAINTS.value: {MetricName.FILE_SIZE.value: 10},
        },
        [['project_3', 'project_5'], ['project_6', 'project_2'], ['project_7', 'project_1']],
    ),
    (
        {
            ConfigField.BATCHER_CONFIG.value: {
                ConfigField.NAME.value: BatcherName.ONE_DIMENSIONAL_NEXT_FIT_DECREASING.value,
                BatcherArgument.MAX_OPEN_BATCHES.value: 3,
            },
            ConfigField.METRIC.value: MetricName.FILE_SIZE.value,
            ConfigField.LANGUAGE.value: Language.PYTHON.value,
            ConfigField.BATCH_CONSTRAINTS.value: {MetricName.FILE_SIZE.value: 10},
            ConfigField.IGNORE_OVERSIZED_PROJECTS.value: False,
        },
        [['project_3', 'project_5'], ['project_6', 'project_2'], ['project_7', 'project_1'], ['project_4']],
    ),
]


@pytest.mark.parametrize(('batching_config', 'batches'), SPLIT_INTO_BATCHES_TEST_DATA)
def test_split_into_batches(batching_config: Dict, batches: List[List[str]]) -> None:
    expected_batches = [[COMMON_DATASET / project for project in batch] for batch in batches]

    projects = list(sorted(Path(project) for project in get_subdirectories(str(COMMON_DATASET))))
    actual_batches = split_into_batches(projects, batching_config)

    assert actual_batches == expected_batches


CREATE_BATCHES_TEST_DATA = [
    [],
    [['project_1', 'project_2'], ['project_3'], ['project_4', 'project_5', 'project_6'], ['project_7']],
]


@pytest.mark.parametrize('batches', CREATE_BATCHES_TEST_DATA)
def test_create_batches(batches: List[List[str]]):
    expected_batch_paths = [[COMMON_DATASET / project for project in batch] for batch in batches]

    with TemporaryDirectory() as tmpdir:
        actual_batch_paths = create_batches(expected_batch_paths, Path(tmpdir))
        resolved_actual_batch_paths = [
            list(sorted(Path(project).resolve() for project in get_subdirectories(str(batch))))
            for batch in actual_batch_paths
        ]

        assert resolved_actual_batch_paths == expected_batch_paths
