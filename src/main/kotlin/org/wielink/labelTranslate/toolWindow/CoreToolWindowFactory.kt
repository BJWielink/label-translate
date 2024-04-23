package org.wielink.labelTranslate.toolWindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.serviceOrNull
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.impl.CoreProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory
import org.wielink.labelTranslate.Constants
import org.wielink.labelTranslate.applicationListener.TranslationFileChangeListener
import org.wielink.labelTranslate.model.node.FileNode
import org.wielink.labelTranslate.model.node.RootNode
import org.wielink.labelTranslate.service.TranslationFileParseService

/**
 * The core [ToolWindowFactory] for the Label Translate plugin.
 * This factory will create a [ToolWindow] that displays all
 * available translations, and it enables the user to modify
 * those translations.
 */
class CoreToolWindowFactory : ToolWindowFactory, TranslationFileChangeListener {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val service = toolWindow.project.serviceOrNull<TranslationFileParseService>() ?: return
        service.initListener()
    }

    override fun onParse(project: Project, rootNode: RootNode) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(Constants.TOOL_WINDOW_ID) ?: return
        if (toolWindow.contentManager.contents.isEmpty()) {
            setInitialContent(rootNode, toolWindow)
        } else {
            updateContent(rootNode, toolWindow)
        }
    }

    private fun setInitialContent(rootNode: RootNode, toolWindow: ToolWindow) {
        for (fileNode in rootNode.children()) {
            addTranslationTab(fileNode as FileNode, toolWindow)
        }
    }

    private fun addTranslationTab(fileNode: FileNode, toolWindow: ToolWindow) {
        val component = CoreToolWindow(toolWindow.project, fileNode, fileNode.label)
        val content = ContentFactory.getInstance().createContent(component, fileNode.label, false)
        toolWindow.contentManager.addContent(content)
    }

    private fun updateContent(rootNode: RootNode, toolWindow: ToolWindow) {
        val existingLabels = toolWindow.contentManager.contents.map { (it.component as CoreToolWindow).id }.toSet()
        val currentLabels = rootNode.children().toList().map { it.label }.toSet()

        val labelsToDelete = existingLabels subtract currentLabels
        val labelsToUpdate = existingLabels intersect currentLabels
        val labelsToAdd = currentLabels subtract existingLabels

        val labelToContentMap = toolWindow.contentManager.contents.associateBy {
            (it.component as CoreToolWindow).id
        }
        val labelToNodeMap = rootNode.children().toList().associateBy {
            it.label
        }

        // Delete
        for (labelToDelete in labelsToDelete) {
            val content = labelToContentMap[labelToDelete] ?: continue
            toolWindow.contentManager.removeContent(content, true)
        }

        // Update
        for (labelToUpdate in labelsToUpdate) {
            val content = labelToContentMap[labelToUpdate] ?: continue
            val fileNode = labelToNodeMap[labelToUpdate] ?: continue
            val component = (content.component as CoreToolWindow)
            component.processUpdate(fileNode as FileNode)
        }

        // Add
        for (labelToAdd in labelsToAdd) {
            val fileNode = labelToNodeMap[labelToAdd] ?: continue
            addTranslationTab(fileNode as FileNode, toolWindow)
        }
    }
}