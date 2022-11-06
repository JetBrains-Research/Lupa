"""Visualization for stratified sampling."""
import ast
from enum import Enum, unique
from pathlib import Path
from typing import List, Optional, Union

import numpy as np
import pandas as pd
import plotly.express as px
import plotly.graph_objects as go
import streamlit as st

from benchmark.metrics_collection.metrics import MetricName
from benchmark.sampling.config import BinsEstimator
from benchmark.sampling.stratified_sampling import (
    PROJECT_COLUMN,
    convert_to_intervals,
    get_stratified_sample,
    read_metrics,
)
from utils.language import Language


BINS = Union[int, str, List[float]]


@unique
class BinsType(Enum):
    COUNT = 'Count'
    ESTIMATOR = 'Estimator'
    EDGES = 'Edges'

    @classmethod
    def values(cls) -> List[str]:
        return [bin_type.value for bin_type in cls]


@st.cache
def load_data(dataset: Path, language: Language) -> Optional[pd.DataFrame]:
    return read_metrics(dataset, language)


def get_bins(key: str) -> BINS:
    left_column, right_column = st.columns(2)

    with left_column:
        bins_type = st.radio(
            'Bins type:',
            options=BinsType.values(),
            index=BinsType.values().index(BinsType.ESTIMATOR.value),
            help=f"""
                Possible bins types:
                * `{BinsType.COUNT.value}` means a number of the bins by which to group the data.
                * `{BinsType.ESTIMATOR.value}` means a name of an estimator which automatically determines the
                  optimal number of the bins.
                * `{BinsType.EDGES.value}` means an array of edges by which the bins are defined.
            """,
            key=f'{key}_bins_type',
        )

    with right_column:
        if bins_type == BinsType.COUNT.value:
            return st.number_input(
                'Count:',
                value=50,
                min_value=1,
                help='The number of the bins by which to group the data. The number must be greater than 1.',
                key=f'{key}_bins_value',
            )

        if bins_type == BinsType.ESTIMATOR.value:
            return st.selectbox(
                'Name:',
                options=BinsEstimator.values(),
                index=BinsEstimator.values().index(BinsEstimator.AUTO.value),
                help=(
                    'The name of an estimator. Consistent with [`numpy.histogram_bin_edges`]'
                    '(https://numpy.org/doc/stable/reference/generated/numpy.histogram_bin_edges.html).'
                ),
                key=f'{key}_bins_value',
            )

        return ast.literal_eval(
            st.text_input(
                'Edges:',
                value='[0, 10, 20, 30, 40, 50]',
                help=(
                    'The array of edges by which the bins are defined. The values must be specified in ascending order.'
                ),
                key=f'{key}_bins_value',
            ),
        )


def get_middle_of_bins(bin_edges: np.ndarray) -> np.ndarray:
    return 0.5 * (bin_edges[:-1] + bin_edges[1:])


def compare_histograms(
    data: pd.DataFrame,
    sample: pd.DataFrame,
    metric: str,
    bins: BINS,
) -> None:
    fig = go.Figure()

    values, bin_edges = np.histogram(data[metric].to_numpy(), bins)

    fig.add_bar(
        x=get_middle_of_bins(bin_edges),
        y=np.round(values * len(sample) / len(data)),
        name='Target',
    )

    values, _ = np.histogram(
        data.merge(sample[PROJECT_COLUMN], on=PROJECT_COLUMN)[metric].to_numpy(),
        bin_edges,
    )

    fig.add_bar(x=get_middle_of_bins(bin_edges), y=values, name='Actual')
    fig.update_layout(xaxis_title='Bin', yaxis_title='Number of projects')

    st.plotly_chart(fig, use_container_width=True)


def main():
    st.title('Stratified Project Sampling')

    left_column, right_column = st.columns(2)

    with left_column:
        file_path = Path(st.text_input('Dataset path:'))

    with right_column:
        language = st.selectbox(
            'Language:',
            options=Language.values(),
            index=Language.values().index(Language.PYTHON.value),
        )

    metrics = st.multiselect('Metrics:', options=MetricName.values())
    if not metrics:
        st.stop()

    raw_data = load_data(file_path, Language(language))
    if raw_data is None:
        st.error('Metrics not found.')
        st.stop()

    data = raw_data.dropna(subset=metrics)

    left_column, right_column = st.columns(2)

    with left_column:
        st.metric('Total number of projects:', len(raw_data), help='The number of projects in the dataset.')

    with right_column:
        st.metric(
            'Actual number of projects:',
            len(data),
            delta=len(data) - len(raw_data),
            help='The number of projects in which all the specified metrics are present.',
        )

    st.header('Histograms')

    metric_bins = {}
    for metric in metrics:
        st.subheader(metric)

        metric_bins[metric] = get_bins(metric)
        values, bin_edges = np.histogram(data[metric], bins=metric_bins[metric])

        number_of_bins = len(bin_edges) - 1
        number_of_non_empty_bins = len([value for value in values if value != 0])

        left_column, right_column = st.columns(2)

        with left_column:
            st.metric('Number of bins:', number_of_bins)

        with right_column:
            st.metric(
                'Number of non-empty bins:',
                number_of_non_empty_bins,
                delta=number_of_non_empty_bins - number_of_bins,
            )

        with st.expander('Histogram:'):
            fig = px.bar(x=get_middle_of_bins(bin_edges), y=values)
            fig.update_layout(xaxis_title='Bin', yaxis_title='Number of projects')
            st.plotly_chart(fig, use_container_width=True)

    st.header('Sampling')

    converted_data = convert_to_intervals(data, metric_bins)
    st.metric(
        'Number of non-empty groups:',
        len(converted_data.groupby(metrics, observed=True)),
        help='Number of non-empty groups, after grouping by all metrics.',
    )

    left_column, right_column = st.columns(2)

    with left_column:
        sample_size = st.number_input(
            'Sample size:',
            min_value=1,
            value=round(len(converted_data) * 0.2),
            help='The expected number of projects in the sample.',
        )

    with right_column:
        random_state = st.number_input('Random state:', value=42, help='Seed for random number generator.')

    if st.button('Sample'):
        with st.spinner('Sampling...'):
            sample = get_stratified_sample(converted_data, metrics, sample_size, random_state)

        st.metric(
            'Actual sample size:',
            len(sample),
            delta=len(sample) - sample_size,
            help=(
                'Since it is impossible to collect projects from some small groups with saving the distribution, '
                'the actual number of projects in the sample may be less than expected. '
            ),
        )

        for metric in metrics:
            with st.expander(metric):
                compare_histograms(data, sample, metric, metric_bins[metric])

        st.download_button('Download', sample[PROJECT_COLUMN].to_csv(index=False), 'sample.csv', 'text/csv')


if __name__ == '__main__':
    main()
