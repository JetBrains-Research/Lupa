from typing import Any, Dict, List, Optional

import pandas as pd
import pytest

from benchmark.sampling.stratified_sampling import (
    convert_to_intervals,
    get_stratified_sample,
    read_metrics,
    read_project_metrics,
)
from test.benchmark.sampling import STRATIFIED_SAMPLING_TEST_DATA_FOLDER, PROJECTS_DATA_FOLDER
from utils.language import Language


def _assert_df_equals(
    actual: Optional[pd.DataFrame],
    expected: Optional[pd.DataFrame],
    sort_by_column: Optional[str] = None,
) -> None:
    if actual is None:
        # assert_frame_equal(None, None) will raise an error, but None equals None
        assert expected is None
    else:
        if sort_by_column is not None:
            actual = actual.sort_values(by=[sort_by_column]).reset_index(drop=True)
            expected = expected.sort_values(by=[sort_by_column]).reset_index(drop=True)

        actual = actual.reindex(sorted(actual.columns), axis=1)
        expected = expected.reindex(sorted(expected.columns), axis=1)

        pd.testing.assert_frame_equal(actual, expected)


READ_PROJECT_METRICS_TEST_DATA = [
    ('non_existent_project', Language.UNKNOWN, None),
    ('project_without_metric_file', Language.UNKNOWN, None),
    ('project_with_single_language', Language.PYTHON, {'number_of_lines': 42, 'number_of_files': 10, 'file_size': 24}),
    ('project_with_single_language', Language.UNKNOWN, {}),
    (
        'project_with_several_languages',
        Language.PYTHON,
        {'number_of_lines': 42, 'number_of_files': 10, 'file_size': 24},
    ),
    (
        'project_with_several_languages',
        Language.KOTLIN,
        {'number_of_lines': 20, 'number_of_files': 53, 'file_size': 74},
    ),
    ('project_with_several_languages', Language.UNKNOWN, {}),
]


@pytest.mark.parametrize(('project_name', 'language', 'expected_metrics'), READ_PROJECT_METRICS_TEST_DATA)
def test_read_project_metrics(
    project_name: str,
    language: Language,
    expected_metrics: Optional[Dict[str, int]],
) -> None:
    dataset_path = PROJECTS_DATA_FOLDER / project_name
    assert read_project_metrics(dataset_path, language) == expected_metrics


READ_METRICS_TEST_DATA = [
    ('non_existent_dataset', Language.PYTHON, None),
    ('dataset_without_metrics', Language.PYTHON, None),
    ('dataset_without_metrics', Language.KOTLIN, None),
    ('dataset_without_metrics', Language.UNKNOWN, None),
    ('dataset_with_python', Language.KOTLIN, None),
    ('dataset_with_python', Language.UNKNOWN, None),
    (
        'dataset_with_python',
        Language.PYTHON,
        pd.DataFrame.from_dict(
            {
                'project': ['project_A', 'project_B'],
                'number_of_lines': [42, None],
                'number_of_files': [10, None],
                'number_of_dependencies': [None, 3],
                'file_size': [None, 93],
            },
        ),
    ),
    ('dataset_with_several_languages', Language.UNKNOWN, None),
    (
        'dataset_with_several_languages',
        Language.PYTHON,
        pd.DataFrame.from_dict(
            {
                'project': ['project_A', 'project_B'],
                'number_of_lines': [42, None],
                'number_of_files': [10, None],
                'file_size': [None, 93],
                'number_of_dependencies': [None, 3],
            },
        ),
    ),
    (
        'dataset_with_several_languages',
        Language.KOTLIN,
        pd.DataFrame.from_dict(
            {
                'project': ['project_A', 'project_B'],
                'number_of_lines': [24, None],
                'number_of_files': [None, 12],
                'file_size': [24, None],
                'number_of_dependencies': [None, 10],
            },
        ),
    ),
]


@pytest.mark.parametrize(('dataset_name', 'language', 'expected_metrics'), READ_METRICS_TEST_DATA)
def test_read_metrics(dataset_name: str, language: Language, expected_metrics: Optional[pd.DataFrame]) -> None:
    dataset_path = STRATIFIED_SAMPLING_TEST_DATA_FOLDER / dataset_name
    _assert_df_equals(read_metrics(dataset_path, language), expected_metrics, 'project')


DATAFRAME_CONTENT = {
    'A': [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
    'B': [1, 1, 1, 1, 2, 2, 2, 2, 3, 3],
}

CONVERT_TO_INTERVALS_TEST_DATA = [
    (pd.DataFrame(), {}, pd.DataFrame()),
    (pd.DataFrame.from_dict(DATAFRAME_CONTENT), {}, pd.DataFrame.from_dict(DATAFRAME_CONTENT)),
    (
        pd.DataFrame.from_dict(DATAFRAME_CONTENT),
        {'A': None},
        pd.DataFrame.from_dict(
            {
                'A': pd.Categorical(
                    [
                        pd.Interval(0.999, 2.8),
                        pd.Interval(0.999, 2.8),
                        pd.Interval(2.8, 4.6),
                        pd.Interval(2.8, 4.6),
                        pd.Interval(4.6, 6.4),
                        pd.Interval(4.6, 6.4),
                        pd.Interval(6.4, 8.2),
                        pd.Interval(6.4, 8.2),
                        pd.Interval(8.2, 10.0),
                        pd.Interval(8.2, 10.0),
                    ],
                    ordered=True,
                ),
                'B': DATAFRAME_CONTENT['B'],
            },
        ),
    ),
    (
        pd.DataFrame.from_dict(DATAFRAME_CONTENT),
        {'A': 'fd'},
        pd.DataFrame.from_dict(
            {
                'A': pd.Categorical(
                    [
                        pd.Interval(0.999, 4.0),
                        pd.Interval(0.999, 4.0),
                        pd.Interval(0.999, 4.0),
                        pd.Interval(0.999, 4.0),
                        pd.Interval(4.0, 7.0),
                        pd.Interval(4.0, 7.0),
                        pd.Interval(4.0, 7.0),
                        pd.Interval(7.0, 10.0),
                        pd.Interval(7.0, 10.0),
                        pd.Interval(7.0, 10.0),
                    ],
                    ordered=True,
                ),
                'B': DATAFRAME_CONTENT['B'],
            },
        ),
    ),
    (
        pd.DataFrame.from_dict(DATAFRAME_CONTENT),
        {'A': 4},
        pd.DataFrame.from_dict(
            {
                'A': pd.Categorical(
                    [
                        pd.Interval(0.999, 3.25),
                        pd.Interval(0.999, 3.25),
                        pd.Interval(0.999, 3.25),
                        pd.Interval(3.25, 5.5),
                        pd.Interval(3.25, 5.5),
                        pd.Interval(5.5, 7.75),
                        pd.Interval(5.5, 7.75),
                        pd.Interval(7.75, 10.0),
                        pd.Interval(7.75, 10.0),
                        pd.Interval(7.75, 10.0),
                    ],
                    ordered=True,
                ),
                'B': DATAFRAME_CONTENT['B'],
            },
        ),
    ),
    (
        pd.DataFrame.from_dict(DATAFRAME_CONTENT),
        {'A': [1, 8, 10]},
        pd.DataFrame.from_dict(
            {
                'A': pd.Categorical(
                    [
                        pd.Interval(0.999, 8.0),
                        pd.Interval(0.999, 8.0),
                        pd.Interval(0.999, 8.0),
                        pd.Interval(0.999, 8.0),
                        pd.Interval(0.999, 8.0),
                        pd.Interval(0.999, 8.0),
                        pd.Interval(0.999, 8.0),
                        pd.Interval(0.999, 8.0),
                        pd.Interval(8.0, 10.0),
                        pd.Interval(8.0, 10.0),
                    ],
                    ordered=True,
                ),
                'B': DATAFRAME_CONTENT['B'],
            },
        ),
    ),
    (
        pd.DataFrame.from_dict(DATAFRAME_CONTENT),
        {'A': 2, 'B': 'auto'},
        pd.DataFrame.from_dict(
            {
                'A': pd.Categorical(
                    [
                        pd.Interval(0.999, 5.5),
                        pd.Interval(0.999, 5.5),
                        pd.Interval(0.999, 5.5),
                        pd.Interval(0.999, 5.5),
                        pd.Interval(0.999, 5.5),
                        pd.Interval(5.5, 10.0),
                        pd.Interval(5.5, 10.0),
                        pd.Interval(5.5, 10.0),
                        pd.Interval(5.5, 10.0),
                        pd.Interval(5.5, 10.0),
                    ],
                    ordered=True,
                ),
                'B': pd.Categorical(
                    [
                        pd.Interval(0.999, 1.4),
                        pd.Interval(0.999, 1.4),
                        pd.Interval(0.999, 1.4),
                        pd.Interval(0.999, 1.4),
                        pd.Interval(1.8, 2.2),
                        pd.Interval(1.8, 2.2),
                        pd.Interval(1.8, 2.2),
                        pd.Interval(1.8, 2.2),
                        pd.Interval(2.6, 3.0),
                        pd.Interval(2.6, 3.0),
                    ],
                    categories=[
                        pd.Interval(0.999, 1.4),
                        pd.Interval(1.4, 1.8),
                        pd.Interval(1.8, 2.2),
                        pd.Interval(2.2, 2.6),
                        pd.Interval(2.6, 3.0),
                    ],
                    ordered=True,
                ),
            },
        ),
    ),
]


@pytest.mark.parametrize(('data', 'column_bins', 'expected_data'), CONVERT_TO_INTERVALS_TEST_DATA)
def test_convert_to_intervals(data: pd.DataFrame, column_bins: Dict[str, Any], expected_data: pd.DataFrame) -> None:
    _assert_df_equals(convert_to_intervals(data, column_bins), expected_data)


COMMON_DATAFRAME = pd.DataFrame.from_dict(
    {
        'A': ['x', 'x', 'x', 'y', 'y', 'z'],
        'B': ['x', 'y', 'y', 'z', 'z', 'z'],
    },
)


GET_STRATIFIED_SAMPLE_TEST_DATA = [
    (
        COMMON_DATAFRAME,
        [],
        42,
        pd.DataFrame.from_dict({'A': ['x', 'x', 'z', 'x', 'y', 'y'], 'B': ['x', 'y', 'z', 'y', 'z', 'z']}),
    ),
    (
        COMMON_DATAFRAME,
        [],
        6,
        pd.DataFrame.from_dict({'A': ['x', 'x', 'z', 'x', 'y', 'y'], 'B': ['x', 'y', 'z', 'y', 'z', 'z']}),
    ),
    (
        COMMON_DATAFRAME,
        [],
        3,
        pd.DataFrame.from_dict({'A': ['x', 'x', 'z'], 'B': ['x', 'y', 'z']}),
    ),
    (
        COMMON_DATAFRAME,
        [],
        0,
        pd.DataFrame.from_dict({'A': [], 'B': []}, dtype=object),
    ),
    (COMMON_DATAFRAME, ['A'], 42, COMMON_DATAFRAME),
    (COMMON_DATAFRAME, ['A'], 6, COMMON_DATAFRAME),
    (
        COMMON_DATAFRAME,
        ['A'],
        5,
        pd.DataFrame.from_dict({'A': ['x', 'x', 'y', 'y', 'z'], 'B': ['x', 'y', 'z', 'z', 'z']}),
    ),
    (
        COMMON_DATAFRAME,
        ['A'],
        4,
        pd.DataFrame.from_dict({'A': ['x', 'x', 'y', 'z'], 'B': ['x', 'y', 'z', 'z']}),
    ),
    (
        COMMON_DATAFRAME,
        ['A'],
        3,
        pd.DataFrame.from_dict({'A': ['x', 'x', 'y'], 'B': ['x', 'y', 'z']}),
    ),
    (
        COMMON_DATAFRAME,
        ['A'],
        2,
        pd.DataFrame.from_dict({'A': ['x', 'y'], 'B': ['x', 'z']}),
    ),
    (
        COMMON_DATAFRAME,
        ['A'],
        1,
        pd.DataFrame.from_dict({'A': [], 'B': []}, dtype=object),
    ),
    (
        COMMON_DATAFRAME,
        ['A'],
        0,
        pd.DataFrame.from_dict({'A': [], 'B': []}, dtype=object),
    ),
    (COMMON_DATAFRAME, ['A', 'B'], 42, COMMON_DATAFRAME),
    (COMMON_DATAFRAME, ['A', 'B'], 6, COMMON_DATAFRAME),
    (COMMON_DATAFRAME, ['A', 'B'], 5, COMMON_DATAFRAME),
    (
        COMMON_DATAFRAME,
        ['A', 'B'],
        4,
        pd.DataFrame.from_dict({'A': ['x', 'x', 'y', 'z'], 'B': ['x', 'y', 'z', 'z']}, dtype=object),
    ),
    (
        COMMON_DATAFRAME,
        ['A', 'B'],
        3,
        pd.DataFrame.from_dict({'A': ['x', 'y'], 'B': ['y', 'z']}, dtype=object),
    ),
    (
        COMMON_DATAFRAME,
        ['A', 'B'],
        2,
        pd.DataFrame.from_dict({'A': ['x', 'y'], 'B': ['y', 'z']}, dtype=object),
    ),
    (
        COMMON_DATAFRAME,
        ['A', 'B'],
        1,
        pd.DataFrame.from_dict({'A': [], 'B': []}, dtype=object),
    ),
    (
        COMMON_DATAFRAME,
        ['A', 'B'],
        0,
        pd.DataFrame.from_dict({'A': [], 'B': []}, dtype=object),
    ),
]


@pytest.mark.parametrize(('data', 'strata', 'size', 'expected_sample'), GET_STRATIFIED_SAMPLE_TEST_DATA)
def test_get_stratified_sample(data: pd.DataFrame, strata: List[str], size: int, expected_sample: pd.DataFrame) -> None:
    _assert_df_equals(get_stratified_sample(data, strata, size, 42), expected_sample)
