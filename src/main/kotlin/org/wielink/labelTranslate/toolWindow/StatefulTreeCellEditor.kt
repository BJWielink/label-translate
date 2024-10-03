package org.wielink.labelTranslate.toolWindow

import javax.swing.DefaultCellEditor
import javax.swing.JTextField
import javax.swing.tree.TreePath

class StatefulTreeCellEditor : DefaultCellEditor(JTextField()) {
    var path: TreePath? = null
    var value: Any? = null

    fun valueForPathChanged(path: TreePath?, value: Any?) {
        this.path = path
        this.value = value
    }
}