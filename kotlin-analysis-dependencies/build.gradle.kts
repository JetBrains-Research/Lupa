group = rootProject.group
version = rootProject.version

plugins {
    groovy
}

dependencies {
    implementation(project(":kotlin-analysis-core"))
}
