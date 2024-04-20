package org.wielink.labelTranslate.util

import org.wielink.labelTranslate.model.CoreColumn
import org.wielink.labelTranslate.model.CoreTreeTableModel
import org.wielink.labelTranslate.model.node.FileNode
import org.wielink.labelTranslate.model.node.LanguageNode

object TreeUtility {
    fun toRepresentationModel(fileNode: FileNode): CoreTreeTableModel {
        /*
         * The UI tree expects the keys and categories to be displayed horizontally.
         * We merge together the file tree to become a horizontal key / category tree.
         */
        val keyTree = RecursionUtility.mergeIntoKeyTree(fileNode)

        // Acquire column information based on the available languages
        val languageColumns = fileNode.children()
            .toList()
            .filterIsInstance<LanguageNode>().map { CoreColumn(it.label) }

        // Calculate columns
        val model = CoreTreeTableModel(keyTree, languageColumns)
        return model
    }
}