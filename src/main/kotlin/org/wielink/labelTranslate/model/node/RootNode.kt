package org.wielink.labelTranslate.model.node

import org.wielink.labelTranslate.enum.NodeType

class RootNode : AbstractNode(NodeType.ROOT) {
    override val label: String = "ROOT"

    fun addKeyNode(keyNode: KeyNode) {
        addNode(keyNode)
    }

    fun addCategoryNode(categoryNode: CategoryNode) {
        addNode(categoryNode)
    }

    fun addFileNode(fileNode: FileNode) {
        addNode(fileNode)
    }

    override fun clone(): AbstractNode {
        return RootNode()
    }
}