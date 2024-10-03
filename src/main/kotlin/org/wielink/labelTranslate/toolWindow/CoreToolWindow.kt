package org.wielink.labelTranslate.toolWindow

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.TableSpeedSearch
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.components.JBTreeTable
import com.intellij.ui.tree.StructureTreeModel
import com.intellij.util.ui.components.BorderLayoutPanel
import org.wielink.labelTranslate.Constants
import org.wielink.labelTranslate.engine.TranslationFileSaver
import org.wielink.labelTranslate.enum.NodeType
import org.wielink.labelTranslate.model.CoreNodeDescriptor
import org.wielink.labelTranslate.model.node.*
import org.wielink.labelTranslate.util.RecursionUtility
import org.wielink.labelTranslate.util.TreeUtility
import java.awt.Color
import java.io.File
import javax.swing.event.CellEditorListener
import javax.swing.event.ChangeEvent
import javax.swing.tree.DefaultMutableTreeNode

class CoreToolWindow(
    project: Project,
    private var fileNode: FileNode,
    val id: String
) : BorderLayoutPanel() {
    val statefulTreeCellEditor = StatefulTreeCellEditor()
    val model = TreeUtility.toRepresentationModel(project, fileNode, statefulTreeCellEditor)
    val treeTable: JBTreeTable

    private fun getCategoryPath(keyNode: KeyNode): List<String> {
        var nodeIterator: AbstractNode? = keyNode.parent
        val labelPath = mutableListOf<String>()
        while (nodeIterator != null && nodeIterator.type != NodeType.ROOT) {
            labelPath.add(nodeIterator.label)
            nodeIterator = nodeIterator.parent
        }
        labelPath.reverse()
        return labelPath
    }

    init {
        treeTable = JBTreeTable(model)
        treeTable.columnProportion = 1.0f / model.columnCount

        // Table editor
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

                // Find the key node that belongs to the edited cell
                val genericKeyNode = treeTable.tree.getPathForRow(row).lastPathComponent
                val keyNode = ((genericKeyNode as DefaultMutableTreeNode).userObject as CoreNodeDescriptor).element as KeyNode
                val categoryPath = getCategoryPath(keyNode)

                // Get the updated value
                val updatedValue = editor.cellEditorValue as String

                // If it does not exist, find the language node so that it can be inserted
                val firstFile = File((keyNode.children().toList().first() as TranslationNode).languageNode!!.filePath)
                val categoryDir = firstFile.parentFile.parentFile
                val languageFolderName = model.languageColumns[column + 1].header
                val languageDir = File(categoryDir, languageFolderName)
                val filePath = File(languageDir, firstFile.name).absolutePath

                val fileSaver = TranslationFileSaver(project, updatedValue, categoryPath, filePath, fileNode, keyNode.label)
                fileSaver.save()
            }

            override fun editingCanceled(e: ChangeEvent?) {
            }
        })
        treeTable.table.setDefaultEditor(StructureTreeModel::class.java, cellEditor)

        // Tree editor
        statefulTreeCellEditor.addCellEditorListener(object: CellEditorListener {
            override fun editingStopped(e: ChangeEvent?) {
                if (e == null) {
                    return
                }

                val editor = e.source as StatefulTreeCellEditor
                val path = editor.path
                val newKey = editor.value as String
            }

            override fun editingCanceled(e: ChangeEvent?) {}
        })
        treeTable.tree.cellEditor = statefulTreeCellEditor
        treeTable.tree.isEditable = true

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

        // Toolbar
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(Constants.TOOL_WINDOW_ID)
        val isHorizontalView = toolWindow != null && toolWindow.anchor.isHorizontal
        val actionToolbar = ActionManager.getInstance().createActionToolbar("CoreToolWindow", createToolbarActions(), !isHorizontalView)
        actionToolbar.targetComponent = treeTable
        val toolbarComponent = actionToolbar.component
        if (isHorizontalView) {
            addToLeft(toolbarComponent)
        } else {
            addToTop(toolbarComponent)
        }

        // Search
        val treeSpeedSearch = TreeSpeedSearch.installOn(treeTable.tree, false) { it.lastPathComponent.toString() }
        treeSpeedSearch.setCanExpand(true)
        treeSpeedSearch.setClearSearchOnNavigateNoMatch(true)
        val tableSpeedSearch = TableSpeedSearch.installOn(treeTable.table)
        tableSpeedSearch.setClearSearchOnNavigateNoMatch(true)
    }

    private fun createToolbarActions(): ActionGroup {
        val actionGroup = DefaultActionGroup()

        // Button to add a new translation key
        actionGroup.addAction(object: AnAction("Add New Translation", "Add a new translation key", AllIcons.General.Add) {
            override fun actionPerformed(e: AnActionEvent) {
                model.addKeyNode("")
            }
        })

        return actionGroup
    }

    fun processUpdate(fileNode: FileNode) {
        val keyNode = RecursionUtility.mergeIntoKeyTree(fileNode)
        model.setRootNode(keyNode)
        this.fileNode = fileNode
    }
}