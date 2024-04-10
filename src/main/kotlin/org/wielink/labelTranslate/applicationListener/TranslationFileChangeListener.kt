package org.wielink.labelTranslate.applicationListener

import com.intellij.util.messages.Topic

interface TranslationFileChangeListener {
    fun onParse()

    companion object {
        val CHANGE_ACTION_TOPIC = Topic.create("translation_file_change", TranslationFileChangeListener::class.java)
    }
}