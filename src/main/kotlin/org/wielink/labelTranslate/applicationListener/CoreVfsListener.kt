package org.wielink.labelTranslate.applicationListener

import com.google.common.collect.MultimapBuilder
import com.intellij.openapi.components.serviceOrNull
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
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
    // TODO: Find a way to determine files deletions for the right project
    override fun after(events: MutableList<out VFileEvent>) {
        // Group together the events to prevent unnecessary refreshes
        val projectToFileEventsMap = MultimapBuilder.hashKeys().arrayListValues().build<Project, VFileEvent>()

        for (event in events) {
            // We require the file to determine the project. If the file is null or deleted, we can not get the project
            if (event.file == null || event is VFileDeleteEvent) {
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
                projectToFileEventsMap.put(project, event)
            }
        }

        for ((project, entries) in projectToFileEventsMap.asMap()) {
            val translationFileParserService = project.serviceOrNull<TranslationFileParseService>() ?: continue
            val virtualFiles = entries.mapNotNull { it.file }
            translationFileParserService.onFileChanged(virtualFiles)
        }
    }
}