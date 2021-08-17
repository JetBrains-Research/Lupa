import enum


class GradleDependenciesColumn(str, enum.Enum):
    PROJECT_NAME = "project_name"
    GROUP_ID = "group_id"
    ARTIFACT_ID = "artifact_id"
    CONFIG = "config"


class GradleDependenciesMetaColumn(str, enum.Enum):
    FULL_NAME = "full_name"
    REPO_NAME = "repo_name"
    URL = "url"
    LANGUAGE = "language"
    COMMUNITY = "community"
    MULTIPLATFORM = "multiplatform"


class GradleDependenciesConfigs(str, enum.Enum):
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


class GradleDependenciesStatsColumn(str, enum.Enum):
    FULL_NAME = "full_name"
    COUNT = "count"
    URL = "url"
    LANGUAGE = "language"
    COMMUNITY = "community"
    MULTIPLATFORM = "multiplatform"
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
