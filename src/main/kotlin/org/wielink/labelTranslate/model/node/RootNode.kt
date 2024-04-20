package org.wielink.labelTranslate.model.node

import org.wielink.labelTranslate.enum.NodeType

class RootNode : AbstractNode(NodeType.ROOT) {
    override val label: String = "ROOT"

    override fun clone(): AbstractNode {
        return RootNode()
    }
}