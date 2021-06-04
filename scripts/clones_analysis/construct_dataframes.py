import pandas as pd
import os


def get_methods_df(folder):
    methods_columns = ['project_id', 'method_id', 'file', 'start_line', 'end_line']
    methods_df = pd.read_csv(os.path.join(folder, 'method_index.csv'), sep='\t', header=None, names=methods_columns)
    methods_tokens = {}
    with open(os.path.join(folder, 'method_data.txt'), 'r') as fp:
        for line in fp:
            _, method_id, tokens_amount, _ = line.split(',', maxsplit=3)
            methods_tokens[int(method_id)] = int(tokens_amount)
    methods_df['tokens'] = methods_df.index.map(lambda id: methods_tokens[id] if id in methods_tokens else -1)
    return methods_df


def get_projects_df(folder):
    return pd.read_csv(os.path.join(folder, 'project_index.csv'), header=None, names=['project_id', 'project_name'])


def get_clones_df(path80, path100):
    columns = ['project1_id', 'method1_id', 'project2_id', 'method2_id']
    clones80df = pd.read_csv(path80, header=None, names=columns)
    clones100df = pd.read_csv(path100, header=None, names=columns)
    clones80df['closeness'] = 80
    clones100df['closeness'] = 100
    clones = pd.concat([clones80df, clones100df])
    clones['is_in_project'] = clones['project1_id'] == clones['project2_id']
    return clones


def get_unique_clones_df(methods_df, graph):
    methods_records = methods_df.sort_values(by=['n_unique_projects'], ascending=False)[
        ['method_id', 'n_unique_projects']][methods_df.n_unique_projects > 1].to_dict('records')

    visited = {}
    for node in graph.nodes:
        visited[node] = False

    ordered_methods = []
    for record in methods_records:
        method_id = record['method_id']
        if not visited[method_id]:
            ordered_methods.append(method_id)

            for method_clone, _, _ in graph[method_id]:
                visited[method_clone] = True

    unique_clones = methods_df[methods_df['method_id'].isin(ordered_methods)]
    return unique_clones

