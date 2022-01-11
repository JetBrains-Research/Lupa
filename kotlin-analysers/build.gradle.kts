group = rootProject.group
version = rootProject.version

plugins {
    groovy
}

dependencies {
    implementation(project(":lupa-core"))
    implementation(project(":lupa-test"))
}
