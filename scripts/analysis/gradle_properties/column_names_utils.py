import enum


class GradlePropertiesColumn(str, enum.Enum):
    PROJECT_NAME = "project_name"
    PROPERTY_KEY = "property_key"
    PROPERTY_VALUE = "property_value"


class GradlePropertiesKeyStatsColumn(str, enum.Enum):
    PROPERTY_KEY = "property_key"
    COUNT = "count"
