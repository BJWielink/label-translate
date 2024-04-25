package org.wielink.labelTranslate.toolWindow

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.TableSpeedSearch
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.components.JBTreeTable
import com.intellij.ui.tree.StructureTreeModel
import com.intellij.util.ui.components.BorderLayoutPanel
import org.wielink.labelTranslate.engine.TranslationFileSaver
import org.wielink.labelTranslate.model.node.FileNode
import org.wielink.labelTranslate.model.node.KeyNode
import org.wielink.labelTranslate.model.node.TranslationNode
import org.wielink.labelTranslate.util.RecursionUtility
import org.wielink.labelTranslate.util.TreeUtility
import java.awt.Color
import java.awt.Component
import javax.swing.JTable
import javax.swing.JTree
import javax.swing.event.CellEditorListener
import javax.swing.event.ChangeEvent
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.tree.TreePath

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

        val cellEditor = StatefulCellEditor()
        cellEditor.addCellEditorListener(object: CellEditorListener {
            override fun editingStopped(e: ChangeEvent?) {
                if (e == null) {
                    return
                }

                val editor = e.source as StatefulCellEditor
                val row = editor.row
                val column = editor.column

                if (row == -1 || row == null || column == -1 || column == null) {
                    return
                }

                val keyNode = treeTable.tree.getPathForRow(row).lastPathComponent
                val updatedValue = editor.cellEditorValue as String
                val translationNode = model.getNodeAt(keyNode, column + 1) ?: return

                val fileSaver = TranslationFileSaver(project, translationNode, updatedValue)
                ApplicationManager.getApplication().runWriteAction {
                    fileSaver.save()
                }
            }

            override fun editingCanceled(e: ChangeEvent?) {
            }
        })
        treeTable.table.setDefaultEditor(StructureTreeModel::class.java, cellEditor)

        treeTable.tree.setCellRenderer(object: NodeRenderer() {
            // Remove the over the top selection display on the tree item
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

        val sorter = CoreRowSorter(treeTable, model)
        treeTable.setRowSorter(sorter)

        // By default, sort by key ascending
        sorter.toggleSortOrder(0)

        addToCenter(treeTable)

        // Search
        val treeSpeedSearch = TreeSpeedSearch.installOn(treeTable.tree, false) { it.lastPathComponent.toString() }
        treeSpeedSearch.setCanExpand(true)
        treeSpeedSearch.setClearSearchOnNavigateNoMatch(true)
        val tableSpeedSearch = TableSpeedSearch.installOn(treeTable.table)
        tableSpeedSearch.setClearSearchOnNavigateNoMatch(true)
    }

    fun processUpdate(fileNode: FileNode) {
        val keyNode = RecursionUtility.mergeIntoKeyTree(fileNode)
        model.setRootNode(keyNode)
    }
}