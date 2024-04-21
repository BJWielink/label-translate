package org.wielink.labelTranslate.applicationListener

import com.intellij.openapi.components.serviceOrNull
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import org.wielink.labelTranslate.service.TranslationFileParseService
import org.wielink.labelTranslate.toolWindow.CoreToolWindow

/**
 * Listens to virtual file system changes. This is done to keep
 * the [CoreToolWindow] in sync with the translation files.
 *
 * Do note that this event is blocking. Heavy tasks should
 * thus be off-loaded.
 */
class CoreVfsListener : BulkFileListener {
    override fun after(events: MutableList<out VFileEvent>) {
        for (event in events) {
            if (event.file == null) {
                continue
            }

            // Get the projects that this file belongs to
            val projects = ProjectManager.getInstance().openProjects.filter {
                var isInContent = false
                val virtualFile = event.file
                if (virtualFile != null) {
                    isInContent = ProjectRootManager.getInstance(it).fileIndex.isInContent(virtualFile)
                }
                isInContent
            }

            for (project in projects) {
                val translationFileParserService = project.serviceOrNull<TranslationFileParseService>() ?: continue
                event.file?.let { translationFileParserService.onFileChanged(it) }
            }
        }
    }
}