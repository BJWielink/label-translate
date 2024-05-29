package org.wielink.labelTranslate.engine

import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import org.wielink.labelTranslate.enum.NodeType
import org.wielink.labelTranslate.model.node.*
import java.io.File

class TranslationFileSaver(
    private val project: Project,
    private val updatedValue: String,
    private val categoryPath: List<String>,
    private val filePath: String,
    private val fileNode: FileNode,
    private val translationLabel: String
) {
    private fun getUpdatedContent(updatedArray: String, currentFile: String): String {
        val firstIndex = "return\\s*\\[".toRegex().find(currentFile)?.range?.last
            ?: return getUpdatedContent(updatedArray, "$currentFile\nreturn [];")

        val lastIndex = currentFile.lastIndexOf("]") + 1

        if (lastIndex == 0) {
            // Add it to the current file ourselves and call the method again
            return getUpdatedContent(updatedArray, "$currentFile\nreturn [];")
        }

        return currentFile.replaceRange(firstIndex, lastIndex, updatedArray)
    }

    fun save() {
        runBackgroundableTask("Saving translations", project, false) {
            val languageFile = File(filePath)

            if (!languageFile.exists() || !languageFile.isFile) {
                return@runBackgroundableTask
            }

            // Find the language node, ensure that the path exists and set or update the translation
            val languageNode = fileNode.children().toList().first { (it as LanguageNode).filePath == filePath }
            val translationNode = ensureNodeExists(languageNode as LanguageNode, categoryPath)
            translationNode.translation = updatedValue

            // Create the return expression implementation
            val stringBuilder = StringBuilder()
            deserializeCategory(stringBuilder, languageNode)

            // Generate the update file content
            val updatedContent = getUpdatedContent(stringBuilder.toString(), languageFile.readText())
            languageFile.writeText(updatedContent)

            VfsUtil.markDirtyAndRefresh(true, false, false, languageFile)
        }
    }

    private fun ensureNodeExists(languageNode: LanguageNode, labelPath: List<String>): TranslationNode {
        var nodeIterator: AbstractNode = languageNode
        for (labelItem in labelPath) {
            var match = nodeIterator.children().toList().firstOrNull { it.type == NodeType.CATEGORY && it.label == labelItem }
            if (match == null) {
                match = CategoryNode(labelItem)
                if (nodeIterator.type == NodeType.LANGUAGE) {
                    (nodeIterator as LanguageNode).addCategoryNode(match)
                } else {
                    (nodeIterator as CategoryNode).addCategoryNode(match)
                }
            }
            nodeIterator = match
        }

        val match = nodeIterator.children().toList().firstOrNull { it.type == NodeType.TRANSLATION && it.label == translationLabel } as TranslationNode?
        if (match != null) {
            return match
        }

        val translationNode = TranslationNode(translationLabel, "") // Dummy
        if (nodeIterator.type == NodeType.LANGUAGE) {
            (nodeIterator as LanguageNode).addTranslationNode(translationNode)
        } else {
            (nodeIterator as CategoryNode).addTranslationNode(translationNode)
        }
        return translationNode
    }

    private fun deserializeCategory(stringBuilder: StringBuilder, nodeToDeserialize: AbstractNode, depth: Int = 0) {
        // Initialize the array
        stringBuilder.append("[\n")

        val children = nodeToDeserialize.children().toList().sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
        for (childNodeToDeserialize in children) {
            if (childNodeToDeserialize.type == NodeType.KEY) {
                continue
            }

            val label = childNodeToDeserialize.label.replace("'", "\\'")
            // Prepare key value pair by, firstly, adding the key
            stringBuilder.append("${" ".repeat((depth + 1 ) * 4)}'$label' => ")
            if (childNodeToDeserialize is TranslationNode) {
                val translation = childNodeToDeserialize.translation.replace("'", "\\'")
                stringBuilder.append("'$translation'")
            } else {
                deserializeCategory(stringBuilder, childNodeToDeserialize, depth + 1)
            }
            stringBuilder.append(",\n")
        }

        stringBuilder.append("${" ".repeat(depth * 4)}]")
    }
}