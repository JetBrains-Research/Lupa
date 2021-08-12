package org.jetbrains.research.ml.kotlinAnalysis.gradle.gradleProperties

/** Gradle property in gradle.properties file in [key]=[value] format (ex. org.gradle.caching=true). **/
data class GradleProperty(val key: String?, val value: String?)
