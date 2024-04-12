package org.wielink.labelTranslate.model

import org.wielink.labelTranslate.enum.TranslationNodeType

data class TranslationNode(
    val type: TranslationNodeType,
    var children: MutableList<TranslationNode>? = null,
    val languagePayload: LanguagePayload? = null
) {
    fun addChild(translationNode: TranslationNode) {
        if (children == null) {
            children = mutableListOf()
        }

        children?.add(translationNode)
    }
}
