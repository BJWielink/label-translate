package org.wielink.labelTranslate.model

import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.tree.StructureTreeModel
import com.intellij.ui.tree.TreeVisitor
import com.intellij.ui.treeStructure.treetable.TreeTableModel
import com.intellij.util.ui.tree.AbstractTreeModel
import com.intellij.util.ui.tree.TreeUtil
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

class CoreTreeTableModel(project: Project, treeNode: TreeNode, val languageColumns: List<CoreColumn>) : AbstractTreeModel(), TreeTableModel, TreeModelListener, TreeVisitor.Acceptor, Disposable {
    private val structure = CoreTreeStructure(project, treeNode as RootNode)
    private val structureModel = StructureTreeModel(structure, this)
    private val asyncModel = AsyncTreeModel(structureModel, true, this)
    private lateinit var tree: JTree

    init {
        asyncModel.addTreeModelListener(this)
    }

    fun setRootNode(rootNode: RootNode) {
        structure.setRootElement(rootNode)
        structureModel.invalidateAsync()
    }

    fun setComparator(comparator: Comparator<in NodeDescriptor<*>>) {
        runActionWithPathsRestore {
            structureModel.setComparator(comparator)
        }
    }

    private fun runActionWithPathsRestore(action: Runnable) {
        ApplicationManager.getApplication().invokeLater {
            val selectedPaths = TreeUtil.collectSelectedPaths(tree)
            val expandedPaths = TreeUtil.collectExpandedPaths(tree)
            action.run()
            for (path in selectedPaths) {
                TreeUtil.promiseSelect(tree, path)
            }
            for (path in expandedPaths) {
                TreeUtil.promiseExpand(tree, path)
            }
        }
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
        return getNodeAt(node, column)?.translation ?: ""
    }

    fun getNodeAt(node: Any?, column: Int): TranslationNode? {
        if (node == null) {
            throw Exception("Null value for one of the nodes was unexpected.")
        }

        val defaultNode = (node as DefaultMutableTreeNode).userObject
        val element = (defaultNode as CoreNodeDescriptor).element

        if ((element as AbstractNode).type === NodeType.CATEGORY) {
            return null
        }

        if ((element as AbstractNode).type === NodeType.KEY) {
            val languageLabel = languageColumns[column].header
            val translationNode = (element as KeyNode).children()
                .toList()
                .firstOrNull { (it as TranslationNode).languageNode?.label == languageLabel } ?: return null
            return (translationNode as TranslationNode)
        }

        throw Exception("Unexpected node value: ${element::class.java.simpleName}")
    }

    override fun isCellEditable(node: Any?, column: Int): Boolean {
        val abstractNode = ((node as DefaultMutableTreeNode).userObject as CoreNodeDescriptor).element as AbstractNode
        return abstractNode.type == NodeType.KEY
    }

    override fun setValueAt(aValue: Any?, node: Any?, column: Int) {
    }

    override fun setTree(tree: JTree) {
        this.tree = tree
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