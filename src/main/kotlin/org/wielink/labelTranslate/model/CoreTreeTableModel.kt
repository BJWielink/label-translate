package org.wielink.labelTranslate.model

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.tree.StructureTreeModel
import com.intellij.ui.tree.TreeVisitor
import com.intellij.ui.treeStructure.treetable.TreeTableModel
import com.intellij.util.ui.tree.AbstractTreeModel
import org.jetbrains.concurrency.Promise
import org.wielink.labelTranslate.enum.NodeType
import org.wielink.labelTranslate.model.node.AbstractNode
import org.wielink.labelTranslate.model.node.KeyNode
import org.wielink.labelTranslate.model.node.RootNode
import org.wielink.labelTranslate.model.node.TranslationNode
import javax.swing.JTree
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath

class CoreTreeTableModel(project: Project, treeNode: TreeNode, private val languageColumns: List<CoreColumn>) : AbstractTreeModel(), TreeTableModel, TreeModelListener, TreeVisitor.Acceptor, Disposable {
    private val structure = CoreTreeStructure(project, treeNode as RootNode)
    private val structureModel = StructureTreeModel(structure, this)
    private val asyncModel = AsyncTreeModel(structureModel, true, this)

    init {
        asyncModel.addTreeModelListener(this)
    }

    fun setRootNode(rootNode: RootNode) {
        structure.setRootElement(rootNode)
        structureModel.invalidateAsync()
    }

    override fun getRoot(): Any? {
        return asyncModel.root
    }

    override fun getChild(parent: Any?, index: Int): Any {
        return asyncModel.getChild(parent, index)
    }

    override fun getChildCount(parent: Any?): Int {
        return asyncModel.getChildCount(parent)
    }

    override fun isLeaf(node: Any?): Boolean {
        return asyncModel.isLeaf(node)
    }

    override fun getIndexOfChild(parent: Any?, child: Any?): Int {
        return asyncModel.getIndexOfChild(parent, child)
    }

    override fun accept(visitor: TreeVisitor): Promise<TreePath> {
        return asyncModel.accept(visitor)
    }

    override fun getColumnCount(): Int {
        return languageColumns.size
    }

    override fun getColumnName(column: Int): String {
        return languageColumns[column].header
    }

    override fun getColumnClass(column: Int): Class<*> {
        return when (column) {
            0 -> return TreeTableModel::class.java
            else -> StructureTreeModel::class.java
        }
    }

    override fun getValueAt(node: Any?, column: Int): Any {
        if (node == null) {
            throw Exception("Null value for one of the nodes was unexpected.")
        }

        val defaultNode = (node as DefaultMutableTreeNode).userObject
        val element = (defaultNode as CoreNodeDescriptor).element

        if ((element as AbstractNode).type === NodeType.CATEGORY) {
            return ""
        }

        if ((element as AbstractNode).type === NodeType.KEY) {
            val languageLabel = languageColumns[column].header
            val translationNode = (element as KeyNode).children()
                .toList()
                .firstOrNull { (it as TranslationNode).languageNode?.label == languageLabel } ?: return ""
            return (translationNode as TranslationNode).translation
        }

        throw Exception("Unexpected node value: ${element::class.java.simpleName}")
    }

    override fun isCellEditable(node: Any?, column: Int): Boolean {
        return false
    }

    override fun setValueAt(aValue: Any?, node: Any?, column: Int) {
    }

    override fun setTree(tree: JTree?) {
    }

    override fun treeNodesChanged(e: TreeModelEvent?) {
        treeNodesChanged(e?.treePath, e?.childIndices, e?.children)
    }

    override fun treeNodesInserted(e: TreeModelEvent?) {
        treeNodesInserted(e?.treePath, e?.childIndices, e?.children)
    }

    override fun treeNodesRemoved(e: TreeModelEvent?) {
        treeNodesRemoved(e?.treePath, e?.childIndices, e?.children)
    }

    override fun treeStructureChanged(e: TreeModelEvent?) {
        treeStructureChanged(e?.treePath, e?.childIndices, e?.children)
    }
}