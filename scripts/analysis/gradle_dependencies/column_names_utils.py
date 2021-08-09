import enum


class GradleDependenciesColumn(str, enum.Enum):
    PROJECT_NAME = "project_name"
    GROUP = "group_id"
    NAME = "artifact_id"
    CONFIG = "config"


class GradleDependenciesStatsColumn(str, enum.Enum):
    DEPENDENCY = "full_name"
    COUNT = "count"
    URL = "url"
    IMPLEMENTATION = "implementation"
    TEST_IMPLEMENTATION = "testImplementation"
    ANDROID_TEST_IMPLEMENTATION = "androidTestImplementation"
    KAPT = "kapt"
    CLASSPATH = "classpath"
    API = "api"
    ANNOTATION_PROCESSOR = "annotationProcessor"
    COMPILE = "compile"
    COMPILE_ONLY = "compileOnly"
    TEST_COMPILE = "testCompile"
    TEST_RUNTIME_ONLY = "testRuntimeOnly"
    TEST_RUNTIME = "testRuntime"
    RUNTIME_ONLY = "runtimeOnly"
