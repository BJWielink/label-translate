package org.wielink.labelTranslate.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.wielink.labelTranslate.applicationListener.TranslationFileChangeListener
import java.io.File

@Service(Service.Level.PROJECT)
class TranslationFileParseService(private val project: Project) {
    private var listenerIsInitialized = false

    private val baseFolder: String? by lazy {
        determineTranslationPath()
    }

    fun initListener() {
        if (listenerIsInitialized) {
            return
        }

        // Send initial event
        sendInitialEvent()
        // Forward CoreVfsListener events now that we are initialized

        listenerIsInitialized = true
    }

    private fun sendInitialEvent() {
        val publisher = project.messageBus.syncPublisher(TranslationFileChangeListener.CHANGE_ACTION_TOPIC)
        publisher.onParse()
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
            if (folderIsTranslationDirectory(childFile)) {
                return true
            }
        }

        return false
    }

    private fun folderIsTranslationDirectory(file: File): Boolean {
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

    companion object {
        private val CANDIDATES = listOf("resources/lang", "lang")
    }
}