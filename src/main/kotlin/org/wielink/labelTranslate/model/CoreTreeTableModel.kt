package org.wielink.labelTranslate.model

import com.intellij.ui.treeStructure.treetable.TreeTableModel
import org.wielink.labelTranslate.enum.NodeType
import org.wielink.labelTranslate.model.node.AbstractNode
import org.wielink.labelTranslate.model.node.KeyNode
import org.wielink.labelTranslate.model.node.TranslationNode
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

class CoreTreeTableModel(treeNode: TreeNode, private val languageColumns: List<CoreColumn>) : DefaultTreeModel(treeNode), TreeTableModel {
    override fun getColumnCount(): Int {
        return languageColumns.size
    }

    override fun getColumnName(column: Int): String {
        return languageColumns[column].header
    }

    override fun getColumnClass(column: Int): Class<*> {
        return AbstractNode::class.java
    }

    override fun getValueAt(node: Any?, column: Int): Any {
        if (node == null) {
            throw Exception("Null value for one of the nodes was unexpected.")
        }

        if ((node as AbstractNode).type === NodeType.CATEGORY) {
            return ""
        }

        if ((node as AbstractNode).type === NodeType.KEY) {
            val languageLabel = languageColumns[column].header
            val translationNode = (node as KeyNode).children()
                .toList()
                .firstOrNull { (it as TranslationNode).languageNode?.label == languageLabel } ?: return ""
            return (translationNode as TranslationNode).translation
        }

        throw Exception("Unexpected node value: ${node::class.java.simpleName}")
    }

    override fun isCellEditable(node: Any?, column: Int): Boolean {
        return false
    }

    override fun setValueAt(aValue: Any?, node: Any?, column: Int) {
    }

    override fun setTree(tree: JTree?) {
    }
}