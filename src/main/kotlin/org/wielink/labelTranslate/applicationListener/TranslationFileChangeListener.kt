package org.wielink.labelTranslate.applicationListener

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic

interface TranslationFileChangeListener {
    fun onParse()

    companion object {
        fun publisher(): TranslationFileChangeListener {
            return ApplicationManager.getApplication().messageBus.syncPublisher(TOPIC)
        }

        @Topic.AppLevel
        val TOPIC = Topic.create("TranslationFileChangeListener", TranslationFileChangeListener::class.java)
    }
}