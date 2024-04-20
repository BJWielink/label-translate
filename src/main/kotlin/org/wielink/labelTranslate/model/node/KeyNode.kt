package org.wielink.labelTranslate.model.node

import org.wielink.labelTranslate.enum.NodeType

class KeyNode(override val label: String) : AbstractNode(NodeType.KEY) {
    override fun clone(): AbstractNode {
        return KeyNode(label)
    }
}