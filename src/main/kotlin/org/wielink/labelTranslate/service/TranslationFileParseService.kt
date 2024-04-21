package org.wielink.labelTranslate.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.wielink.labelTranslate.applicationListener.TranslationFileChangeListener
import org.wielink.labelTranslate.engine.TranslationFileParser
import org.wielink.labelTranslate.model.node.RootNode
import java.io.File

@Service(Service.Level.PROJECT)
class TranslationFileParseService(
    private val project: Project
) {
    private var listenerIsInitialized = false

    private val baseFolder: String? by lazy {
        determineTranslationPath()
    }

    fun initListener() {
        if (listenerIsInitialized) {
            return
        }

        sendInitialEvent()

        listenerIsInitialized = true
    }

    fun onFileChanged(virtualFiles: List<VirtualFile>) {
        if (!listenerIsInitialized) {
            return
        }

        val baseFolder = this.baseFolder ?: return
        val translationFiles = virtualFiles.filter {
            val file = File(it.path)
            file.absolutePath.startsWith(baseFolder) && TranslationFileParser.isTranslationFile(file)
        }

        if (translationFiles.isEmpty()) {
            return
        }

        // Ideally we would want to only parse this language tree instead of the whole root tree
        sendInitialEvent()
    }

    private fun sendInitialEvent(): RootNode {
        val baseFolder = this.baseFolder ?: return null!!
        val translationFileParser = TranslationFileParser(project)
        val rootNode = translationFileParser.buildTree(baseFolder)
        TranslationFileChangeListener.publisher().onParse(project, rootNode)
        return rootNode
    }

    private fun determineTranslationPath(): String? {
        for (candidate in CANDIDATES) {
            val file = File(project.basePath, candidate)

            if (file.exists() && folderContainsTranslationFiles(file)) {
                return file.absolutePath
            }
        }

        return null
    }

    /**
     * We determine that a folder contains a translation file by checking
     * if the immediate child folder has a file with the .php extension.
     */
    private fun folderContainsTranslationFiles(file: File): Boolean {
        if (!file.isDirectory) {
            return false
        }

        // First iterate over the folders in the root
        val childFiles = file.listFiles() ?: return false
        for (childFile in childFiles) {
            if (!childFile.isDirectory) {
                continue
            }

            // Then check if the immediate folder is a translation directory based on a dumb algorithm
            if (TranslationFileParser.folderIsTranslationDirectory(childFile)) {
                return true
            }
        }

        return false
    }

    companion object {
        private val CANDIDATES = listOf("resources/lang", "lang")
    }
}