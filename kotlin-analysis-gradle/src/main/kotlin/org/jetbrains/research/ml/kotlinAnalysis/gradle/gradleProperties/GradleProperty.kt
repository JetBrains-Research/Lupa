package org.jetbrains.research.ml.kotlinAnalysis.gradle.gradleProperties

/** Properties in gradle gradle.properties file. */
enum class GradlePropertyKey(val key: String) {
    // ==== BUILD PERFORMANCE ====
    ORG_GRADLE_CACHING("org.gradle.caching"),
    ORG_GRADLE_PARALLEL("org.gradle.parallel"),
    ORG_GRADLE_VFS_WATCH("org.gradle.vfs.watch"),
    ORG_GRADLE_WORKERS_MAX("org.gradle.workers.max"),
    ORG_GRADLE_PRIORITY("org.gradle.priority"),

    // == Daemon ==
    ORG_GRADLE_DAEMON("org.gradle.daemon"),
    ORG_GRADLE_DAEMON_IDLETIMEOUT("org.gradle.daemon.idletimeout"),
    ORG_GRADLE_CONFIGUREONDEMAND("org.gradle.configureondemand"),

    // ==== DEBUG ====
    ORG_GRADLE_DEBUG("org.gradle.debug"),
    ORG_GRADLE_CACHING_DEBUG("org.gradle.caching.debug"),

    // ==== LOGGING & CONSOLE EXPERIENCE ====
    ORG_GRADLE_CONSOLE("org.gradle.console"),
    ORG_GRADLE_LOGGING_LEVEL("org.gradle.logging.level"),
    ORG_GRADLE_VERBOSE("org.gradle.vfs.verbose"),
    ORG_GRADLE_WARNING_MODE("org.gradle.warning.mode"),

    // ==== ENVIRONMENT ====
    ORG_GRADLE_JAVA_HOME("org.gradle.java.home"),
    ORG_GRADLE_JVMARGS("org.gradle.jvmargs");


    companion object {
        fun fromKey(key: String) = values().firstOrNull { it.key.equals(key, ignoreCase = true) }

        fun availableKeys() = values().map { it.key }
    }
}

data class GradleProperty(val key: String?, val value: String?)
