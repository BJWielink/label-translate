package org.wielink.labelTranslate.toolWindow

import java.awt.Component
import javax.swing.DefaultCellEditor
import javax.swing.JTable
import javax.swing.JTextField

class StatefulCellEditor : DefaultCellEditor(JTextField()) {
    var row: Int? = null
    var column: Int? = null

    override fun getTableCellEditorComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        row: Int,
        column: Int
    ): Component {
        this.row = row
        this.column = column
        return super.getTableCellEditorComponent(table, value, isSelected, row, column)
    }

    override fun fireEditingStopped() {
        super.fireEditingStopped()
        row = -1
        column = -1
    }
}