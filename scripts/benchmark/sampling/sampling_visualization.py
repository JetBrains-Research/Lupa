import ast
from enum import Enum, unique
from pathlib import Path
from typing import List, Union

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


@unique
class BinsType(Enum):
    COUNT = 'Count'
    ESTIMATOR = 'Estimator'
    EDGES = 'Edges'

    @classmethod
    def values(cls) -> List[str]:
        return [bin_type.value for bin_type in cls]


@st.cache
def load_data(dataset: Path, language: Language) -> pd.DataFrame:
    return read_metrics(dataset, language)


def get_bins(key: str) -> Union[int, str, List[float]]:
    left_column, right_column = st.columns(2)

    with left_column:
        bins_type = st.radio(
            'Bins type:',
            options=BinsType.values(),
            index=BinsType.values().index(BinsType.ESTIMATOR.value),
            key=f'{key}_bins_type',
        )

    with right_column:
        if bins_type == BinsType.COUNT.value:
            return st.number_input('Count:', value=50, min_value=1, key=f'{key}_bins_value')

        if bins_type == BinsType.ESTIMATOR.value:
            return st.selectbox(
                'Name:',
                options=BinsEstimator.values(),
                index=BinsEstimator.values().index(BinsEstimator.AUTO.value),
                key=f'{key}_bins_value',
            )

        return ast.literal_eval(st.text_input('Edges:', value='[0, 10, 20, 30, 40, 50]', key=f'{key}_bins_value'))


def get_middle_of_bins(bin_edges: np.ndarray) -> np.ndarray:
    return 0.5 * (bin_edges[:-1] + bin_edges[1:])


def compare_histograms(
    data: pd.DataFrame,
    sample: pd.DataFrame,
    metric: str,
    bins: Union[int, str, List[float]],
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

    row_data = load_data(file_path, Language(language))
    data = row_data.dropna(subset=metrics)
    st.write(f'Number of projects: {len(data)}/{len(row_data)}')

    st.header('Histograms')

    metric_bins = {}
    for metric in metrics:
        st.subheader(metric)

        metric_bins[metric] = get_bins(metric)
        values, bin_edges = np.histogram(data[metric], bins=metric_bins[metric])

        left_column, right_column = st.columns(2)
        with left_column:
            st.write(f'Number of bins: {len(bin_edges) - 1}')
        with right_column:
            st.write(f'Number of non-empty bins: {len([value for value in values if value != 0])}')

        with st.expander('Histogram:'):
            st.plotly_chart(px.bar(x=get_middle_of_bins(bin_edges), y=values), use_container_width=True)

    st.header('Sampling')

    converted_data = convert_to_intervals(data, metric_bins)
    st.write(f'Number of groups: {len(converted_data.groupby(metrics, observed=True))}')

    left_column, right_column = st.columns(2)

    with left_column:
        number_of_samples = st.number_input(
            'Target number of samples:',
            min_value=1,
            value=round(len(converted_data) * 0.2),
        )

    with right_column:
        random_state = st.number_input('Random state:', value=42)

    if st.button('Sample'):
        with st.spinner('Sampling...'):
            sample = get_stratified_sample(converted_data, metrics, number_of_samples, random_state)

        st.write(f'Actual number of samples: {len(sample)}')

        for metric in metrics:
            with st.expander(metric):
                compare_histograms(data, sample, metric, metric_bins[metric])

        st.download_button('Download', sample[PROJECT_COLUMN].to_csv(index=False), 'sample.csv', 'text/csv')


if __name__ == '__main__':
    main()
