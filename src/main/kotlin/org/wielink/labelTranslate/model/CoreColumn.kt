package org.wielink.labelTranslate.model

import com.intellij.util.ui.ColumnInfo
import org.wielink.labelTranslate.model.node.AbstractNode

data class CoreColumn(val header: String): ColumnInfo<AbstractNode, String>(header) {
    override fun valueOf(item: AbstractNode?): String? {
        return item?.label
    }
}