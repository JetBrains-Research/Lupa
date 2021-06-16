import enum


class Methods_columns(str, enum.Enum):
    method_id = "method_id"
    project_id = "project_id"

    n_clones = "n_clones"
    n_inter_clones = "n_inter_clones"
    n_100_clones = "n_100_clones"
    n_inter_100_clones = "n_inter_100_clones"

    n_unique_projects = "n_unique_projects"
    n_unique_100_projects = "n_unique_100_projects"

    file = "file"
    start_line = "start_line"
    end_line = "end_line"

    tokens = "tokens"
    method_text = "method_text"
    highlighted_code = "highlighted_code"

    leader_method_id = "leader_method_id"
    component_size = "component_size"
    n_unique_projects_component = "n_unique_projects_component"


class Clones_columns(str, enum.Enum):
    project1_id = "project1_id"
    project2_id = "project2_id"
    method1_id = "method1_id"
    method2_id = "method2_id"
    is_in_project = "is_in_project"
    closeness = "closeness"
