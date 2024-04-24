package org.wielink.labelTranslate.toolWindow

import com.intellij.ui.components.JBTreeTable
import com.intellij.util.containers.ContainerUtil
import org.wielink.labelTranslate.model.CoreTreeTableModel
import java.util.*
import javax.swing.RowSorter
import javax.swing.SortOrder
import javax.swing.table.TableModel

class CoreRowSorter(private val treeTable: JBTreeTable, private val model: CoreTreeTableModel) : RowSorter<TableModel>() {
    private var sortKey: SortKey? = null

    override fun getModel(): TableModel {
        return treeTable.table.model
    }

    override fun toggleSortOrder(column: Int) {
        val sortOrder = if (sortKey != null && sortKey?.column == column && sortKey?.sortOrder == SortOrder.ASCENDING) {
            SortOrder.DESCENDING
        } else {
            SortOrder.ASCENDING
        }
        sortKeys = Collections.singletonList(SortKey(column, sortOrder))
    }

    override fun convertRowIndexToModel(index: Int): Int {
        return index
    }

    override fun convertRowIndexToView(index: Int): Int {
        return index
    }

    override fun setSortKeys(keys: MutableList<out SortKey>?) {
        if (keys == null || keys.isEmpty()) return
        val key = keys[0]
        if (key.sortOrder == SortOrder.UNSORTED) return
        sortKey = key
        val columnInfo = model.languageColumns[key.column]
        val comparator = columnInfo.comparator
        fireSortOrderChanged()
        model.setComparator(reverseComparator(comparator, key.sortOrder))
    }

    override fun getSortKeys(): MutableList<out SortKey> {
        return ContainerUtil.createMaybeSingletonList(sortKey)
    }

    override fun getViewRowCount(): Int {
        return treeTable.tree.rowCount
    }

    override fun getModelRowCount(): Int {
        return treeTable.tree.rowCount
    }

    override fun modelStructureChanged() {
    }

    override fun allRowsChanged() {
    }

    override fun rowsInserted(firstRow: Int, endRow: Int) {
    }

    override fun rowsDeleted(firstRow: Int, endRow: Int) {
    }

    override fun rowsUpdated(firstRow: Int, endRow: Int) {
    }

    override fun rowsUpdated(firstRow: Int, endRow: Int, column: Int) {
    }

    private fun <T> reverseComparator(comparator: Comparator<T>, order: SortOrder): Comparator<T> {
        if (order != SortOrder.DESCENDING) return comparator
        return comparator.reversed()
    }
}