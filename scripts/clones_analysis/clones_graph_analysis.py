from typing import List, Set, Tuple, Dict
import networkx as nx
import pandas as pd

from column_names_utils import Clones_columns, Methods_columns


def add_edge(clones_graph: nx.Graph, row: pd.Series) -> Tuple[int, int, Dict]:
    clones_graph.add_node(row[Clones_columns.method1_id.value], project_id=row[Clones_columns.project1_id.value])
    clones_graph.add_node(row[Clones_columns.method2_id.value], project_id=row[Clones_columns.project2_id.value])
    return row[Clones_columns.method1_id.value], row[Clones_columns.method2_id.value], {
        Clones_columns.is_in_project.value: row[Clones_columns.is_in_project.value],
        Clones_columns.closeness.value: row[Clones_columns.closeness.value]}


def get_graph(clones: pd.DataFrame) -> nx.Graph:
    graph = nx.Graph()
    edges = clones.apply(lambda row: add_edge(graph, row), axis=1)
    graph.add_edges_from(edges)
    return graph


def add_graph_features(methods_df: pd.DataFrame, components: List[Set[int]]) -> None:
    method_to_repr = {}
    method_to_component_size = {}
    for component in components:
        representative = list(component)[0]
        method_to_repr[representative] = representative
        method_to_component_size[representative] = len(component)

        for method in list(component)[1:]:
            method_to_repr[method] = representative
            method_to_component_size[method] = len(component)

    methods_df[Methods_columns.leader_method_id.value] = methods_df.index.map(
        lambda id: method_to_repr[id] if id in method_to_repr else id)
    methods_df[Methods_columns.component_size.value] = methods_df.index.map(
        lambda id: method_to_component_size[id] if id in method_to_component_size else 1)

    grouped = methods_df.groupby([Methods_columns.leader_method_id.value])[
        Methods_columns.project_id.value].nunique().to_dict()
    methods_df[Methods_columns.n_unique_projects_component.value] = methods_df[
        Methods_columns.leader_method_id.value].map(lambda id: grouped[id])
