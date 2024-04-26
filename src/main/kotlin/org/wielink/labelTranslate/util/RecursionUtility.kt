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
            rootChild = node
            // Set reference to language so that we can match it to the right column in the table UI
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
                // Either category or root
                if (rootNode.type == NodeType.ROOT) {
                    (rootNode as RootNode).addKeyNode(existingKeyNode)
                } else if (rootNode.type == NodeType.CATEGORY) {
                    (rootNode as CategoryNode).addKeyNode(existingKeyNode)
                } else {
                    throw Exception("Illegal tree structure. A key should belong to either a root or a category.")
                }
            }

            existingKeyNode.addTranslationNode(rootChild)
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
                // Either category or root
                if (rootNode.type == NodeType.CATEGORY) {
                    (rootNode as CategoryNode).addCategoryNode(rootChild as CategoryNode)
                } else if (rootNode.type == NodeType.ROOT) {
                    (rootNode as RootNode).addCategoryNode(rootChild as CategoryNode)
                } else {
                    throw Exception("Illegal tree structure. A category should belong to either another category or a root.")
                }
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