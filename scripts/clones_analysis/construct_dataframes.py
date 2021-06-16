import pandas as pd
import os
import networkx as nx

from column_names_utils import Clones_columns, Methods_columns


def get_methods_df(folder: str) -> pd.DataFrame:
    methods_columns = [Methods_columns.project_id.value, Methods_columns.method_id.value, Methods_columns.file.value,
                       Methods_columns.start_line.value, Methods_columns.end_line.value]
    methods_df = pd.read_csv(os.path.join(folder, 'method_index.csv'), sep='\t', header=None, names=methods_columns)
    methods_tokens = {}
    with open(os.path.join(folder, 'method_data.txt'), 'r') as fp:
        for line in fp:
            _, method_id, tokens_amount, _ = line.split(',', maxsplit=3)
            methods_tokens[int(method_id)] = int(tokens_amount)
    methods_df[Methods_columns.tokens.value] = methods_df.index.map(
        lambda id: methods_tokens[id] if id in methods_tokens else -1)
    return methods_df


def get_projects_df(folder: str) -> pd.DataFrame:
    return pd.read_csv(os.path.join(folder, 'project_index.csv'), header=None, names=['project_id', 'project_name'])


def get_clones_df(path80: str, path100: str) -> pd.DataFrame:
    columns = [Clones_columns.project1_id.value, Clones_columns.method1_id.value,
               Clones_columns.project2_id.value, Clones_columns.method2_id.value]
    clones80df = pd.read_csv(path80, header=None, names=columns)
    clones100df = pd.read_csv(path100, header=None, names=columns)
    clones80df[Clones_columns.closeness.value] = 80
    clones100df[Clones_columns.closeness.value] = 100
    clones = pd.concat([clones80df, clones100df])
    clones[Clones_columns.is_in_project.value] = \
        clones[Clones_columns.project1_id.value] == clones[Clones_columns.project2_id.value]
    return clones


def get_unique_clones_df(methods_df: pd.DataFrame, graph: nx.Graph) -> pd.DataFrame:
    methods_records = methods_df.sort_values(by=[Methods_columns.n_unique_projects.value], ascending=False)[
        [Methods_columns.method_id.value, Methods_columns.n_unique_projects.value]][methods_df.n_unique_projects > 1]. \
        to_dict('records')

    visited = {}
    for node in graph.nodes:
        visited[node] = False

    ordered_methods = []
    for record in methods_records:
        method_id = record[Methods_columns.method_id.value]
        if not visited[method_id]:
            ordered_methods.append(method_id)

            for method_clone in graph[method_id]:
                visited[method_clone] = True

    unique_clones = methods_df[methods_df[Methods_columns.method_id.value].isin(ordered_methods)]
    return unique_clones
