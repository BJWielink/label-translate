package org.wielink.labelTranslate.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import org.wielink.labelTranslate.applicationListener.TranslationFileChangeListener
import org.wielink.labelTranslate.service.TranslationFileParseService
import javax.swing.JPanel

/**
 * The core [ToolWindowFactory] for the Label Translate plugin.
 * This factory will create a [ToolWindow] that displays all
 * available translations, and it enables the user to modify
 * those translations.
 */
class CoreToolWindowFactory : ToolWindowFactory, TranslationFileChangeListener {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val content = ContentFactory.getInstance().createContent(JPanel(), "", false)
        toolWindow.contentManager.addContent(content)
        val service = toolWindow.project.service<TranslationFileParseService>()
        service.initListener()
    }

    override fun onParse() {
        println("Parsed...")
    }
}