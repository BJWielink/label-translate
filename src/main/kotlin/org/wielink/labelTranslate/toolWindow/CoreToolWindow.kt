package org.wielink.labelTranslate.toolWindow

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBTreeTable
import com.intellij.util.ui.components.BorderLayoutPanel
import com.intellij.util.ui.tree.TreeUtil
import org.wielink.labelTranslate.model.CoreNodeDescriptor
import org.wielink.labelTranslate.model.node.AbstractNode
import org.wielink.labelTranslate.model.node.FileNode
import org.wielink.labelTranslate.util.RecursionUtility
import org.wielink.labelTranslate.util.TreeUtility
import java.awt.Color
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class CoreToolWindow(
    project: Project,
    fileNode: FileNode,
    val id: String
) : BorderLayoutPanel() {
    val model = TreeUtility.toRepresentationModel(project, fileNode)
    val treeTable: JBTreeTable

    init {
        treeTable = JBTreeTable(model)
        treeTable.columnProportion = 1.0f / model.columnCount

        // Remove the over the top selection display on the tree item
        treeTable.tree.setCellRenderer(object: NodeRenderer() {
            override fun customizeCellRenderer(
                tree: JTree,
                value: Any?,
                selected: Boolean,
                expanded: Boolean,
                leaf: Boolean,
                row: Int,
                hasFocus: Boolean
            ) {
                val defaultNode = (value as DefaultMutableTreeNode).userObject
                val element = (defaultNode as CoreNodeDescriptor).element
                val text = (element as AbstractNode).label
                super.customizeCellRenderer(tree, text, selected, expanded, leaf, row, hasFocus)
            }

            override fun getSimpleTextAttributes(
                presentation: PresentationData,
                color: Color?,
                node: Any
            ): SimpleTextAttributes {
                var myColor = color
                if (mySelected) {
                    myColor = null
                }
                return super.getSimpleTextAttributes(presentation, myColor, node)
            }
        })

        addToCenter(treeTable)
    }

    fun processUpdate(fileNode: FileNode) {
        val keyNode = RecursionUtility.mergeIntoKeyTree(fileNode)
        model.setRootNode(keyNode)
    }
}