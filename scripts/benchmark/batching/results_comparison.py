from typing import Dict

import pandas as pd
import plotly.graph_objs as go
import streamlit as st

from benchmark.batching.batcher_benchmark import BenchmarkResultColumn, RunType
from utils.df_utils import AggregationFunction
from utils.file_utils import get_all_file_system_items

SELECTBOX_DEFAULT_VALUE = 'Select'


def plot_per_batch_stats(
    result: pd.DataFrame,
    by_column: BenchmarkResultColumn,
    aggregate_function: AggregationFunction,
) -> go.Figure:
    stats_by_type = result.groupby(
        [BenchmarkResultColumn.BATCH.value, BenchmarkResultColumn.TYPE.value],
        as_index=False,
    ).agg(aggregate_function.value)

    fig = go.Figure()

    for run_type in RunType.values():
        type_stats = stats_by_type[stats_by_type[BenchmarkResultColumn.TYPE.value] == run_type]

        fig.add_scatter(
            x=type_stats[BenchmarkResultColumn.BATCH.value],
            y=type_stats[by_column.value],
            name=f'{run_type} (aggregated)',
            mode='lines+markers',
        )

        fig.add_scatter(
            x=result[result[BenchmarkResultColumn.TYPE.value] == run_type][BenchmarkResultColumn.BATCH.value],
            y=result[result[BenchmarkResultColumn.TYPE.value] == run_type][by_column.value],
            name=f'{run_type} (all)',
            mode='markers',
            marker={'size': 4},
        )

    if len(stats_by_type[BenchmarkResultColumn.TYPE.value].unique()) > 1:
        overall_stats = result.groupby(BenchmarkResultColumn.BATCH.value, as_index=False).agg(aggregate_function.value)

        fig.add_scatter(
            x=overall_stats[BenchmarkResultColumn.BATCH.value],
            y=overall_stats[by_column.value],
            name='Overall',
            mode='lines+markers',
        )

    fig.update_layout(xaxis_title='Batch', yaxis_title=by_column.value)
    return fig


def get_result_time_stats(
    result: pd.DataFrame,
    aggregate_function: AggregationFunction,
    dataset_size: int,
    sample_size: int,
) -> pd.Series:
    total_stats_by_type = (
        result.groupby([BenchmarkResultColumn.BATCH.value, BenchmarkResultColumn.TYPE.value])
        .agg(aggregate_function.value)
        .groupby(BenchmarkResultColumn.TYPE.value)
        .sum()
    )

    stats = {}

    for run_type, row in total_stats_by_type.iterrows():
        stats[f'{run_type}_total'] = row[BenchmarkResultColumn.TIME.value]

    if len(total_stats_by_type) > 1:
        stats['total'] = (
            result.groupby(BenchmarkResultColumn.BATCH.value)[BenchmarkResultColumn.TIME.value]
            .agg(aggregate_function.value)
            .sum()
        )

    for run_type in total_stats_by_type.index:
        stats[f'{run_type}_per_project'] = stats[f'{run_type}_total'] / sample_size

    if len(total_stats_by_type) > 1:
        stats['per_project'] = stats['total'] / sample_size

    if dataset_size != sample_size:
        for run_type in total_stats_by_type.index:
            stats[f'{run_type}_dataset'] = stats[f'{run_type}_per_project'] * dataset_size

        if len(total_stats_by_type) > 1:
            stats['dataset'] = stats['per_project'] * dataset_size

    return pd.Series(stats)


def show_time_comparison_table(
    results: Dict[str, pd.DataFrame],
    aggregate_function: AggregationFunction,
    dataset_size: int,
    sample_size: int,
) -> None:
    rows = []
    for name, result in results.items():
        result_stats = get_result_time_stats(result, aggregate_function, dataset_size, sample_size)
        result_stats.name = name
        rows.append(result_stats)

    comparison_table = pd.concat(rows, axis=1).T
    st.write(comparison_table.style.highlight_min().set_precision(2))

    with st.expander('Table description:'):
        st.markdown(
            r"""
            There are 3 types of stats:
            - `total` — Sum of time for all the batches (the time is pre-aggregated within each batch).
            - `per_project` — Approximate time it will take to analyze one project. Calculated by the formula:
              $$\texttt{total} \div \texttt{Sample size}$$.
            - `dataset` — Approximate time it will take to analyze the entire dataset. Calculated by the formula:
              $$\texttt{per\_project} \cdot \texttt{Sample size}$$.

              If the dataset size is equal to the sample size, this statistic is omitted.

            If the results contain several run types, the stats for the specific run type will be shown in addition
            to the overall stats.

            The minimum values of each statistic are highlighted in yellow.
            """,
        )


def show_rss_comparison_table(results: Dict[str, pd.DataFrame], aggregate_function: AggregationFunction) -> None:
    rows = []
    for name, result in results.items():
        max_stats_by_type = (
            result.groupby([BenchmarkResultColumn.BATCH.value, BenchmarkResultColumn.TYPE.value])
            .agg(aggregate_function.value)
            .groupby(BenchmarkResultColumn.TYPE.value)
            .max()
        )

        stats = {}

        for run_type, row in max_stats_by_type.iterrows():
            stats[f'{run_type}_max'] = row[BenchmarkResultColumn.RSS.value]

        if len(max_stats_by_type) > 1:
            stats['max'] = (
                result.groupby(BenchmarkResultColumn.BATCH.value)[BenchmarkResultColumn.RSS.value]
                .agg(aggregate_function.value)
                .max()
            )

        rows.append(pd.Series(stats, name=name))

    comparison_table = pd.concat(rows, axis=1).T
    st.write(comparison_table.style.highlight_min().set_precision(2))

    with st.expander('Table description:'):
        st.markdown(
            """
            There is one type of stats — `sum`, which is equal to the maximum value of the
            [resident set size](https://en.wikipedia.org/wiki/Resident_set_size) among all batches
            (the RSS is pre-aggregated within each batch).

            If the results contain several run types, the stats for the specific run type will be shown in addition
            to the overall stats.

            The minimum values of each statistic are highlighted in yellow.
            """,
        )


def show_comparison_table(
    results: Dict[str, pd.DataFrame],
    by_column: BenchmarkResultColumn,
    aggregate_function: AggregationFunction,
    dataset_size: int,
    sample_size: int,
) -> None:
    if by_column == BenchmarkResultColumn.TIME:
        show_time_comparison_table(results, aggregate_function, dataset_size, sample_size)
    elif by_column == BenchmarkResultColumn.RSS:
        show_rss_comparison_table(results, aggregate_function)
    else:
        st.error('Comparison by this metric has not yet been implemented.')


def main() -> None:
    st.title('Benchmark Results Comparison')

    results_dir = st.text_input('Results path:', help='Path to a folder with benchmark results.')

    left_column, right_column = st.columns(2)

    with left_column:
        dataset_size = st.number_input('Dataset size:', min_value=0, step=1, help='Number of projects in a dataset.')

        metric = st.selectbox(
            'Metric:',
            options=[SELECTBOX_DEFAULT_VALUE] + BenchmarkResultColumn.metrics(),
            help='Benchmark metric by which the comparison will be made.',
        )

    with right_column:
        sample_size = st.number_input(
            'Sample size:',
            min_value=0,
            max_value=dataset_size,
            step=1,
            help=(
                'Number of projects in a sample. '
                'If you did not use a sample, then set the value equal to the dataset size.'
            ),
        )

        aggregation_function = AggregationFunction(
            st.selectbox(
                'Aggregation function:',
                options=AggregationFunction.values(),
                index=AggregationFunction.values().index(AggregationFunction.MEAN.value),
                help='Aggregation function that will be used when grouping results.',
            ),
        )

    if results_dir and dataset_size != 0 and sample_size != 0 and metric != SELECTBOX_DEFAULT_VALUE:
        results = {
            result_path.stem: pd.read_csv(result_path)
            for result_path in get_all_file_system_items(results_dir, with_subdirs=False)
        }

        st.header('Comparison table')
        show_comparison_table(
            results,
            BenchmarkResultColumn(metric),
            aggregation_function,
            dataset_size,
            sample_size,
        )

        st.header('Per batch stats')
        result_name = st.selectbox('Result name:', options=results.keys())
        st.plotly_chart(
            plot_per_batch_stats(results[result_name], BenchmarkResultColumn(metric), aggregation_function),
            use_container_width=True,
        )


if __name__ == '__main__':
    main()
