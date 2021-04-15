group = rootProject.group
version = rootProject.version

tasks.register<Jar>("testJar") {
    archiveClassifier.set("tests")
    from(sourceSets.test.get().output)
}

configurations.create("tests") {
    extendsFrom(configurations.testRuntime.get())
}

artifacts {
    add("tests", tasks.named("testJar"))
}
