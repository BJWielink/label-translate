package org.wielink.labelTranslate.toolWindow

import com.intellij.ui.components.JBTreeTable
import org.wielink.labelTranslate.model.CoreTreeTableModel
import org.wielink.labelTranslate.model.node.FileNode
import org.wielink.labelTranslate.util.RecursionUtility
import org.wielink.labelTranslate.util.TreeUtility
import java.awt.Dimension
import javax.swing.JPanel

class CoreToolWindow(fileNode: FileNode, val id: String) : JPanel() {
    val model = TreeUtility.toRepresentationModel(fileNode)

    init {
        val treeTable = JBTreeTable(model)
        treeTable.preferredSize = Dimension(500, 500)
        add(treeTable)
    }

    fun processUpdate(fileNode: FileNode) {
        val keyNode = RecursionUtility.mergeIntoKeyTree(fileNode)
        model.setRoot(keyNode)
    }
}