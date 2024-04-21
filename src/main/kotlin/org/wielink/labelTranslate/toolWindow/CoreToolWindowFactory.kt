package org.wielink.labelTranslate.toolWindow

import com.intellij.openapi.components.serviceOrNull
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

        // We can also add an equality comparison on the tree so that we can only rerender what is needed
        toolWindow.contentManager.removeAllContents(true)

        for (fileNode in rootNode.children()) {
            val content = ContentFactory.getInstance().createContent(CoreToolWindow(fileNode as FileNode), fileNode.label, false)
            toolWindow.contentManager.addContent(content)
        }
    }
}