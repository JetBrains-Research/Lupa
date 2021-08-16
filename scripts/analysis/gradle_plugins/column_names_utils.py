import enum


class GradlePluginsColumn(str, enum.Enum):
    PROJECT_NAME = "project_name"
    PLUGIN_ID = "plugin_id"


class GradlePluginsStatsColumn(str, enum.Enum):
    PLUGIN_ID = "plugin_id"
    COUNT = "count"
