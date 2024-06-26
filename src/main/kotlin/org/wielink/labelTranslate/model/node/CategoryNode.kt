package org.wielink.labelTranslate.model.node

import org.wielink.labelTranslate.enum.NodeType

class CategoryNode(override val label: String) : AbstractNode(NodeType.CATEGORY) {
    fun addTranslationNode(translationNode: TranslationNode) {
        addNode(translationNode)
    }

    fun addCategoryNode(categoryNode: CategoryNode) {
        addNode(categoryNode)
    }

    fun addKeyNode(keyNode: KeyNode) {
        addNode(keyNode)
    }

    override fun clone(): CategoryNode {
        return CategoryNode(label)
    }
}