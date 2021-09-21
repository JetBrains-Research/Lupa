import pandas as pd
import os
import networkx as nx

from column_names_utils import ClonesColumn, MethodsColumn
from utils import Extensions


def get_methods_df(folder: str) -> pd.DataFrame:
    methods_columns = [
        MethodsColumn.PROJECT_ID.value,
        MethodsColumn.METHOD_ID.value,
        MethodsColumn.FILE.value,
        MethodsColumn.START_LINE.value,
        MethodsColumn.END_LINE.value,
    ]
    methods_df = pd.read_csv(
        os.path.join(folder, f'method_index.{Extensions.CSV}'),
        sep='\t',
        header=None,
        names=methods_columns,
    )
    methods_tokens = {}
    with open(os.path.join(folder, f'method_data.{Extensions.TXT}'), 'r') as fp:
        for line in fp:
            _, method_id, tokens_amount, _ = line.split(',', maxsplit=3)
            methods_tokens[int(method_id)] = int(tokens_amount)
    methods_df[MethodsColumn.TOKENS.value] = methods_df.index.map(
        lambda id: methods_tokens[id] if id in methods_tokens else -1,
    )
    return methods_df


def get_projects_df(folder: str) -> pd.DataFrame:
    return pd.read_csv(
        os.path.join(folder, f'project_index.{Extensions.CSV}'),
        header=None,
        names=['project_id', 'project_name'],
    )


def get_clones_df(path80: str, path100: str) -> pd.DataFrame:
    columns = [
        ClonesColumn.PROJECT1_ID.value,
        ClonesColumn.METHOD1_ID.value,
        ClonesColumn.PROJECT2_ID.value,
        ClonesColumn.METHOD2_ID.value,
    ]
    clones80df = pd.read_csv(path80, header=None, names=columns)
    clones100df = pd.read_csv(path100, header=None, names=columns)
    clones80df[ClonesColumn.CLOSENESS.value] = 80
    clones100df[ClonesColumn.CLOSENESS.value] = 100
    clones = pd.concat([clones80df, clones100df])
    clones[ClonesColumn.IS_IN_PROJECT.value] = (
        clones[ClonesColumn.PROJECT1_ID.value] == clones[ClonesColumn.PROJECT2_ID.value]
    )
    return clones


def get_unique_clones_df(methods_df: pd.DataFrame, graph: nx.Graph) -> pd.DataFrame:
    methods_records = (
        methods_df
        [[MethodsColumn.METHOD_ID.value, MethodsColumn.N_UNIQUE_PROJECTS.value]]
        [methods_df.n_unique_projects > 1]
        .sort_values(by=[MethodsColumn.N_UNIQUE_PROJECTS.value], ascending=False)
        .to_dict('records')
    )

    visited = {}
    for node in graph.nodes:
        visited[node] = False

    ordered_methods = []
    for record in methods_records:
        method_id = record[MethodsColumn.METHOD_ID.value]
        if not visited[method_id]:
            ordered_methods.append(method_id)

            for method_clone in graph[method_id]:
                visited[method_clone] = True

    unique_clones = methods_df[methods_df[MethodsColumn.METHOD_ID.value].isin(ordered_methods)]
    return unique_clones
