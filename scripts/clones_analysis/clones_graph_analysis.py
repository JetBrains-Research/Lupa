from typing import List, Set, Tuple, Dict
import networkx as nx
import pandas as pd

from column_names_utils import ClonesColumn, MethodsColumn


def add_edge(clones_graph: nx.Graph, row: pd.Series) -> Tuple[int, int, Dict]:
    clones_graph.add_node(row[ClonesColumn.METHOD1_ID.value], project_id=row[ClonesColumn.PROJECT1_ID.value])
    clones_graph.add_node(row[ClonesColumn.METHOD2_ID.value], project_id=row[ClonesColumn.PROJECT2_ID.value])
    return row[ClonesColumn.METHOD1_ID.value], row[ClonesColumn.METHOD2_ID.value], {
        ClonesColumn.IS_IN_PROJECT.value: row[ClonesColumn.IS_IN_PROJECT.value],
        ClonesColumn.CLOSENESS.value: row[ClonesColumn.CLOSENESS.value]}


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

    methods_df[MethodsColumn.LEADER_METHOD_ID.value] = methods_df.index.map(
        lambda id: method_to_repr[id] if id in method_to_repr else id)
    methods_df[MethodsColumn.COMPONENT_SIZE.value] = methods_df.index.map(
        lambda id: method_to_component_size[id] if id in method_to_component_size else 1)

    grouped = methods_df.groupby([MethodsColumn.LEADER_METHOD_ID.value])[
        MethodsColumn.PROJECT_ID.value].nunique().to_dict()
    methods_df[MethodsColumn.N_UNIQUE_PROJECTS_COMPONENT.value] = methods_df[
        MethodsColumn.LEADER_METHOD_ID.value].map(lambda id: grouped[id])
