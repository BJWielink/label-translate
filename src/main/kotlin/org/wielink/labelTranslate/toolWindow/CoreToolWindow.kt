package org.wielink.labelTranslate.toolWindow

import com.intellij.ui.components.JBTreeTable
import org.wielink.labelTranslate.model.node.FileNode
import org.wielink.labelTranslate.util.TreeUtility
import java.awt.Dimension
import javax.swing.JPanel

class CoreToolWindow(fileNode: FileNode) : JPanel() {
    init {
        val model = TreeUtility.toRepresentationModel(fileNode)
        val treeTable = JBTreeTable(model)
        treeTable.preferredSize = Dimension(500, 500)
        add(treeTable)
    }
}