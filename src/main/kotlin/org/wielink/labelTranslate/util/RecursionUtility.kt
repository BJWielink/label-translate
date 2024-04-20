package org.wielink.labelTranslate.util

import org.wielink.labelTranslate.enum.NodeType
import org.wielink.labelTranslate.model.node.*

object RecursionUtility {
    fun mergeIntoKeyTree(fileNode: FileNode): RootNode {
        val rootNode = RootNode()

        for (languageNode in fileNode.children()) {
            recursivelyMergeKeysIntoRootTree(rootNode, languageNode, languageNode as LanguageNode)
        }

        return rootNode
    }

    private fun recursivelyMergeKeysIntoRootTree(rootNode: AbstractNode, node: AbstractNode, languageNode: LanguageNode) {
        var rootChild = rootNode

        if (node.type == NodeType.TRANSLATION) {
            // Merge together translation nodes into a parent key node so that we can horizontally use them in the table
            rootChild = node.clone()
            (rootChild as TranslationNode).languageNode = languageNode

            // Use existing key node if present
            var existingKeyNode: KeyNode? = null
            for (childNode in rootNode.children()) {
                if (childNode.type == NodeType.KEY && (childNode as KeyNode).label == node.label) {
                    existingKeyNode = childNode
                }
            }

            // Create new one if not present
            if (existingKeyNode == null) {
                existingKeyNode = KeyNode(node.label)
                rootNode.addNode(existingKeyNode)
            }

            existingKeyNode.addNode(rootChild)
        } else if (node.type == NodeType.CATEGORY) {
            // Merge together categories. Duplicate categories will be skipped because this is language invariant anyhow
            var categoryNode: CategoryNode? = null
            for (childNode in rootNode.children()) {
                if (childNode.type == NodeType.CATEGORY && (childNode as CategoryNode).label == node.label) {
                    categoryNode = childNode
                }
            }

            if (categoryNode == null) {
                // If the category is unique to this root, add it
                rootChild = node.clone()
                rootNode.addNode(rootChild)
            } else {
                // Use existing category if already has been added to the root
                rootChild = categoryNode
            }
        }

        for (child in node.children()) {
            recursivelyMergeKeysIntoRootTree(rootChild, child, languageNode)
        }
    }
}