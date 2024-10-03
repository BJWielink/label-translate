package org.wielink.labelTranslate.util

import com.intellij.openapi.project.Project
import org.wielink.labelTranslate.model.CoreColumn
import org.wielink.labelTranslate.model.CoreTreeTableModel
import org.wielink.labelTranslate.model.node.FileNode
import org.wielink.labelTranslate.model.node.LanguageNode
import org.wielink.labelTranslate.toolWindow.StatefulTreeCellEditor

object TreeUtility {
    fun toRepresentationModel(project: Project, fileNode: FileNode, statefulTreeCellEditor: StatefulTreeCellEditor): CoreTreeTableModel {
        /*
         * The UI tree expects the keys and categories to be displayed horizontally.
         * We merge together the file tree to become a horizontal key / category tree.
         */
        val keyTree = RecursionUtility.mergeIntoKeyTree(fileNode)

        // Acquire column information based on the available languages
        val languageColumns = fileNode.children()
            .toList()
            .filterIsInstance<LanguageNode>().mapIndexed { index, it -> CoreColumn(index + 1, it.label) }.toMutableList()

        // Add key for the tree
        languageColumns.add(0, CoreColumn(0, "key"))

        // Calculate columns
        return CoreTreeTableModel(project, keyTree, languageColumns, statefulTreeCellEditor)
    }
}