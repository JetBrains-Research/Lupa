package org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.buildGradle.plugins

import com.intellij.psi.PsiElement
import java.util.*

/**
 * Find the first child with type [T], that satisfies [condition]
 * For traversing is used BFS
 *
 * @param toIncludeRoot to include the current node in the traversal
 * @param condition
 */
internal inline fun <reified T : PsiElement> PsiElement.findFirstChild(
    toIncludeRoot: Boolean = true,
    condition: (PsiElement) -> Boolean = { true }
): PsiElement? {
    val children: Queue<PsiElement> = LinkedList(if (toIncludeRoot) listOf(this) else this.children.toList())
    while (children.isNotEmpty()) {
        val currentItem = children.poll()
        if (currentItem is T && condition(currentItem)) {
            return currentItem
        }
        children.addAll(currentItem.children)
    }
    return null
}

/**
 * Get plugin version from the PSI node
 *
 * @param childrenCount indicates the number of children for this PSI node
 */
internal fun PsiElement.pluginVersion(childrenCount: Int): String? {
    return if (GradleConstants.VERSION.key in this.text) {
        require(this.children.size == childrenCount) { "Incorrect PSI for ${this.text} with specified version" }
        this.children.last()?.text?.trim('\'')?.trim('"')
    } else null
}

/**
 * Find the plugins { ... } section in build.gradle file
 */
internal inline fun <reified S : PsiElement, reified B : PsiElement> PsiElement.findPluginsBody() =
    findSectionBody<S, B>(GradleConstants.PLUGINS.key)

/**
 * Find the allprojects { ... } section in build.gradle file
 */
internal inline fun <reified S : PsiElement, reified B : PsiElement> PsiElement.findAllProjectsBody() =
    findSectionBody<S, B>(GradleConstants.ALL_PROJECTS.key)

/**
 * Find the [sectionName] { ... } section in build.gradle file
 *
 * @param sectionName
 */
private inline fun <reified S : PsiElement, reified B : PsiElement> PsiElement.findSectionBody(
    sectionName: String
): PsiElement? {
    val allProjectsSection = this.findFirstChild<S> { sectionName in it.text } ?: return null
    return allProjectsSection.findFirstChild<B>()
}
