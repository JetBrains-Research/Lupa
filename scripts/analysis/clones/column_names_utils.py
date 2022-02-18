import enum


class MethodsColumn(str, enum.Enum):
    METHOD_ID = 'method_id'
    PROJECT_ID = 'project_id'

    N_CLONES = 'n_clones'
    N_INTER_CLONES = 'n_inter_clones'
    N_100_CLONES = 'n_100_clones'
    N_INTER_100_CLONES = 'n_inter_100_clones'

    N_UNIQUE_PROJECTS = 'n_unique_projects'
    N_UNIQUE_100_PROJECTS = 'n_unique_100_projects'

    FILE = 'file'
    START_LINE = 'start_line'
    END_LINE = 'end_line'

    TOKENS = 'tokens'
    METHOD_TEXT = 'method_text'
    HIGHLIGHTED_CODE = 'highlighted_code'
    IS_EMPTY = 'is_empty'

    LEADER_METHOD_ID = 'leader_method_id'
    COMPONENT_SIZE = 'component_size'
    N_UNIQUE_PROJECTS_COMPONENT = 'n_unique_projects_component'


class ClonesColumn(str, enum.Enum):
    PROJECT1_ID = 'project1_id'
    PROJECT2_ID = 'project2_id'
    METHOD1_ID = 'method1_id'
    METHOD2_ID = 'method2_id'
    IS_IN_PROJECT = 'is_in_project'
    CLOSENESS = 'closeness'
