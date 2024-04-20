package org.wielink.labelTranslate.model.node

import org.wielink.labelTranslate.enum.NodeType

class LanguageNode(override val label: String, val dirPath: String) : AbstractNode(NodeType.LANGUAGE) {
    fun addTranslationNode(translationNode: TranslationNode) {
        this.addNode(translationNode)
    }

    fun addCategoryNode(categoryNode: CategoryNode) {
        this.addNode(categoryNode)
    }

    override fun clone(): AbstractNode {
        return LanguageNode(label, dirPath)
    }
}