package org.wielink.labelTranslate.model.node

import org.wielink.labelTranslate.enum.NodeType

class TranslationNode(override val label: String, val translation: String) : AbstractNode(NodeType.TRANSLATION) {
    var languageNode: LanguageNode? = null

    override fun clone(): AbstractNode {
        return TranslationNode(label, translation)
    }
}