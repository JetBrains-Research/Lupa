group = rootProject.group
version = rootProject.version

plugins {
    groovy
}

dependencies {
    implementation(project(":kotlin-analysis-core"))
    implementation(project(":kotlin-analysis-test"))
}
