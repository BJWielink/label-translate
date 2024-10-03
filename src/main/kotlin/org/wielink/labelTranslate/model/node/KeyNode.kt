package org.wielink.labelTranslate.model.node

import org.wielink.labelTranslate.enum.NodeType

class KeyNode(override var label: String) : AbstractNode(NodeType.KEY) {
    fun addTranslationNode(translationNode: TranslationNode) {
        addNode(translationNode)
    }

    override fun clone(): KeyNode {
        return KeyNode(label)
    }
}