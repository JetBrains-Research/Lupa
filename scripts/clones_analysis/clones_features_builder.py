def add_number_of_clones_features(methods_df, graph=None):
    methods_df['n_clones'] = methods_df['method_id'].apply(lambda id: number_of_clones(id, graph))
    methods_df['n_inter_clones'] = methods_df['method_id'].apply(
        lambda id: number_of_clones(id, graph, only_inter=True))
    methods_df['n_100_clones'] = methods_df['method_id'].apply(
        lambda id: number_of_clones(id, graph, only_100_closeness=True))
    methods_df['n_inter_100_clones'] = methods_df['method_id'].apply(
        lambda id: number_of_clones(id, graph, only_inter=True, only_100_closeness=True))


def number_of_clones(method_id, graph, only_inter=False, only_100_closeness=False):
    if method_id not in graph:
        return 0

    result = 0
    for method_end_id in graph[method_id]:
        edge = graph[method_id][method_end_id]
        if only_inter and edge['in-project']:
            continue

        if only_100_closeness and edge['closeness'] == 80:
            continue

        result += 1
    return result


def add_number_of_projects_features(methods_df, graph=None):
    methods_df['n_unique_projects'] = methods_df['method_id'].apply(lambda id: get_projects_amount(id, graph))
    methods_df['n_unique_100_projects'] = methods_df['method_id'].apply(
        lambda id: get_projects_amount(id, graph, only_exact=True))


def get_projects_amount(node_start, graph, only_exact=False):
    if node_start not in graph:
        return 1

    all_projects = set()
    project_start = graph.nodes[node_start]['project_id']
    all_projects.add(project_start)
    for node_end in graph[node_start]:
        project_end = graph.nodes[node_end]['project_id']
        if not only_exact or graph[node_start][node_end]['closeness'] == 100:
            all_projects.add(project_end)
    return len(all_projects)