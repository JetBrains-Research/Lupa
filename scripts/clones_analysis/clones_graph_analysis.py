from typing import List, Set, Tuple, Dict
import networkx as nx
import pandas as pd


def add_edge(graph: nx.Graph, row: pd.Series) -> Tuple[int, int, Dict]:
    graph.add_node(row['method1_id'], project_id=row['project1_id'])
    graph.add_node(row['method2_id'], project_id=row['project2_id'])
    return row['method1_id'], row['method2_id'], {'in-project': row['is_in_project'], 'closeness': row['closeness']}


def get_graph(clones: pd.DataFrame) -> nx.Graph:
    graph = nx.Graph()
    edges = clones.apply(lambda row: add_edge(graph, row), axis=1)
    graph.add_edges_from(edges)
    return graph


def add_graph_features(methods_df: pd.DataFrame, components: List[Set[int]]):
    method_to_repr = {}
    method_to_component_size = {}
    for component in components:
        representative = list(component)[0]
        method_to_repr[representative] = representative
        method_to_component_size[representative] = len(component)

        for method in list(component)[1:]:
            method_to_repr[method] = representative
            method_to_component_size[method] = len(component)

    methods_df['leader_method_id'] = methods_df.index.map(lambda id: method_to_repr[id] if id in method_to_repr else id)
    methods_df['component_size'] = methods_df.index.map(
        lambda id: method_to_component_size[id] if id in method_to_component_size else 1)

    grouped = methods_df.groupby(['leader_method_id'])['project_id'].nunique().to_dict()
    methods_df['n_unique_projects_component'] = methods_df['leader_method_id'].map(lambda id: grouped[id])
