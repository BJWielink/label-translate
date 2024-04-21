package org.wielink.labelTranslate.applicationListener

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import org.wielink.labelTranslate.model.node.RootNode

interface TranslationFileChangeListener {
    fun onParse(project: Project, rootNode: RootNode)

    companion object {
        fun publisher(): TranslationFileChangeListener {
            return ApplicationManager.getApplication().messageBus.syncPublisher(TOPIC)
        }

        @Topic.AppLevel
        val TOPIC = Topic.create("TranslationFileChangeListener", TranslationFileChangeListener::class.java)
    }
}