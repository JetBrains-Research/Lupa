from typing import Tuple, Dict
import pandas as pd


# one pass over adjacency list to count all statistics
def count_clones_statistics(file_path: str, min_projects: int = 10) -> Tuple[Dict, Dict]:
    methods_stats = {"method_id": [], "n_clones": [], "n_inter_clones": [], "n_100_clones": [],
                     "n_inter_100_clones": [], "n_unique_projects": [], "n_unique_100_projects": []}
    clones_adjacency_top = {}

    with open(file_path, 'r') as fin:
        for line in fin:
            method_from, methods_to = line.strip().split(":")
            method_id, project_id = map(int, method_from.split(","))
            clones_list = methods_to.split(";")

            n_inter_100_clones, n_inter_clones, n_100_clones = 0, 0, 0
            projects, projects_100 = set(), set()
            projects.add(project_id)
            projects_100.add(project_id)
            clones_ids = []

            for clone in clones_list:
                method_clone_id, project_clone_id, closeness = map(int, clone.split(","))
                projects.add(project_clone_id)
                clones_ids.append((method_clone_id, project_clone_id, closeness))

                if project_id != project_clone_id and closeness == 100:
                    n_inter_100_clones += 1
                if project_id != project_clone_id:
                    n_inter_clones += 1
                if closeness == 100:
                    n_100_clones += 1
                    projects_100.add(project_clone_id)

            # save adjacency list only for methods, that has at least min_projects unique projects in clones
            if len(projects) > min_projects:
                clones_adjacency_top[method_id] = clones_ids

            methods_stats["method_id"].append(method_id)
            methods_stats["n_clones"].append(len(clones_list))
            methods_stats["n_inter_clones"].append(n_inter_clones)
            methods_stats["n_100_clones"].append(n_100_clones)
            methods_stats["n_inter_100_clones"].append(n_inter_100_clones)
            methods_stats["n_unique_projects"].append(len(projects))
            methods_stats["n_unique_100_projects"].append(len(projects_100))
    return methods_stats, clones_adjacency_top


def get_unique_clones_df_large(methods_df: pd.DataFrame, clones_adjacency_top: Dict) -> pd.DataFrame:
    methods_sorted = [k for k, v in sorted(clones_adjacency_top.items(), key=lambda item: len(item[1]), reverse=True)]

    visited = {}
    for node in clones_adjacency_top.keys():
        visited[node] = False

    ordered_unique_methods = []
    for method_id in methods_sorted:
        if not visited[method_id]:
            ordered_unique_methods.append(method_id)

            for method_clone, _, _ in clones_adjacency_top[method_id]:
                visited[method_clone] = True
    unique_clones = methods_df[methods_df['method_id'].isin(ordered_unique_methods)]
    return unique_clones


def add_features_from_stats(methods_df: pd.DataFrame, methods_stats: Dict) -> pd.DataFrame:
    methods_stats_df = pd.DataFrame.from_dict(methods_stats)
    return pd.merge(methods_df, methods_stats_df, on="method_id", how="inner")
