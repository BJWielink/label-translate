package org.wielink.labelTranslate.engine

import org.wielink.labelTranslate.enum.TranslationNodeType
import org.wielink.labelTranslate.model.LanguagePayload
import org.wielink.labelTranslate.model.TranslationNode
import java.io.File

object TranslationFileParser {
    private fun completeLanguageTree(translationNode: TranslationNode) {
        if (translationNode.type != TranslationNodeType.LANGUAGE) {
            return
        }

        val languagePayload = translationNode.languagePayload ?: return
        val file = File(languagePayload.filePath)

        if (!file.exists()) {
            return
        }

        val content = file.readText()

        // First match all non grouped items

        // Then match all grouped items so that we can recursively match them again
    }

    fun buildTree(rootPath: String): TranslationNode {
        val rootNode = TranslationNode(TranslationNodeType.ROOT)

        val rootFile = File(rootPath)
        val translationDirectoryCandidates = rootFile.listFiles() ?: return rootNode

        for (translationDirectoryCandidate in translationDirectoryCandidates) {
            if (!folderIsTranslationDirectory(translationDirectoryCandidate)) {
                continue
            }

            val languagePayload = LanguagePayload(translationDirectoryCandidate.absolutePath)
            val childNode = TranslationNode(TranslationNodeType.LANGUAGE, languagePayload = languagePayload)
            completeLanguageTree(childNode)
            rootNode.addChild(childNode)
        }

        return rootNode
    }

    fun folderIsTranslationDirectory(file: File): Boolean {
        if (!file.isDirectory) {
            return false
        }

        val childFiles = file.listFiles() ?: return false
        for (childFile in childFiles) {
            if (!childFile.isFile) {
                continue
            }

            // If the subdirectory contains a php file we deem the folder as a valid translation folder
            if (childFile.extension == "php") {
                return true
            }
        }

        return false
    }
}