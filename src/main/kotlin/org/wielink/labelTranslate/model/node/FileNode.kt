package org.wielink.labelTranslate.model.node

import org.wielink.labelTranslate.enum.NodeType

class FileNode(override val label: String, val filePath: String) : AbstractNode(NodeType.FILE) {
    fun addLanguageNode(languageNode: LanguageNode) {
        addNode(languageNode)
    }

    override fun clone(): AbstractNode {
        return FileNode(label, filePath)
    }
}