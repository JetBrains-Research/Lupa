import networkx as nx
import pandas as pd
from typing import Callable, Dict, List

from column_names_utils import ClonesColumn, MethodsColumn


class EdgeFilter:
    def __init__(self, predicate: Callable[[Dict], bool]):
        self.predicate = predicate

    def apply(self, edge: Dict) -> bool:
        return self.predicate(edge)


is_clone_inter_project: EdgeFilter = EdgeFilter(lambda edge: not edge[ClonesColumn.IS_IN_PROJECT.value])
is_clone_exact: EdgeFilter = EdgeFilter(lambda edge: edge[ClonesColumn.CLOSENESS.value] == 100)


def add_number_of_clones_features(methods_df: pd.DataFrame, clones_graph: nx.Graph = None) -> None:
    methods_df[MethodsColumn.N_CLONES.value] = methods_df[MethodsColumn.METHOD_ID.value].apply(
        lambda id: number_of_clones(id, clones_graph, []))
    methods_df[MethodsColumn.N_INTER_CLONES.value] = methods_df[MethodsColumn.METHOD_ID.value].apply(
        lambda id: number_of_clones(id, clones_graph, [is_clone_inter_project]))
    methods_df[MethodsColumn.N_100_CLONES.value] = methods_df[MethodsColumn.METHOD_ID.value].apply(
        lambda id: number_of_clones(id, clones_graph, [is_clone_exact]))
    methods_df[MethodsColumn.N_INTER_100_CLONES.value] = methods_df[MethodsColumn.METHOD_ID.value].apply(
        lambda id: number_of_clones(id, clones_graph, [is_clone_inter_project, is_clone_exact]))


def number_of_clones(method_id: int, clones_graph: nx.Graph, predicates: List[EdgeFilter]) -> int:
    if method_id not in clones_graph:
        return 0

    edges = map(lambda method_end_id: clones_graph[method_id][method_end_id], clones_graph[method_id])
    return sum(map(lambda edge: all(map(lambda predicate: predicate.apply(edge), predicates)), edges))


def add_number_of_projects_features(methods_df: pd.DataFrame, clones_graph: nx.Graph = None) -> None:
    methods_df[MethodsColumn.N_UNIQUE_PROJECTS.value] = methods_df[MethodsColumn.METHOD_ID.value].apply(
        lambda id: get_projects_amount(id, clones_graph, []))
    methods_df[MethodsColumn.N_UNIQUE_100_PROJECTS.value] = methods_df[MethodsColumn.METHOD_ID.value].apply(
        lambda id: get_projects_amount(id, clones_graph, [is_clone_exact]))


def get_projects_amount(node_start: int, clones_graph: nx.Graph, predicates: List[EdgeFilter]) -> int:
    if node_start not in clones_graph:
        return 1

    all_projects = set()
    project_start = clones_graph.nodes[node_start][MethodsColumn.PROJECT_ID.value]
    all_projects.add(project_start)
    for node_end in clones_graph[node_start]:
        project_end = clones_graph.nodes[node_end][MethodsColumn.PROJECT_ID.value]
        edge = clones_graph[node_start][node_end]
        if all(map(lambda predicate: predicate.apply(edge), predicates)):
            all_projects.add(project_end)
    return len(all_projects)
