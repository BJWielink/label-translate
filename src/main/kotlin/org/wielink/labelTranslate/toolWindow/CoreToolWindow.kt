package org.wielink.labelTranslate.toolWindow

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBTreeTable
import com.intellij.util.ui.components.BorderLayoutPanel
import com.intellij.util.ui.tree.TreeUtil
import org.wielink.labelTranslate.model.node.FileNode
import org.wielink.labelTranslate.util.RecursionUtility
import org.wielink.labelTranslate.util.TreeUtility
import java.awt.Color

class CoreToolWindow(
    fileNode: FileNode,
    val id: String
) : BorderLayoutPanel() {
    val model = TreeUtility.toRepresentationModel(fileNode)

    init {
        val treeTable = JBTreeTable(model)
        TreeUtil.expand(treeTable.tree, 2)

        // Remove the over the top selection display on the tree item
        treeTable.tree.setCellRenderer(object: NodeRenderer() {
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
        model.setRoot(keyNode)
    }
}