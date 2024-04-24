package org.wielink.labelTranslate.model

import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.util.ui.ColumnInfo
import org.wielink.labelTranslate.model.node.AbstractNode
import org.wielink.labelTranslate.model.node.CategoryNode
import org.wielink.labelTranslate.model.node.KeyNode
import org.wielink.labelTranslate.model.node.TranslationNode

data class CoreColumn(val index: Int, val header: String): ColumnInfo<NodeDescriptor<*>, String>(header) {
    private val myComparator: Comparator<NodeDescriptor<*>> = Comparator { o1, o2 ->
        compareValuesBy(o1, o2, String.CASE_INSENSITIVE_ORDER) { valueOf(it) }
    }

    override fun valueOf(item: NodeDescriptor<*>?): String {
        return if (item == null) {
            ""
        } else if (index == 0 || item.element is CategoryNode) {
            /*
             * Index 0 is the key index. But if it is a language column, but it is empty because
             * it is a category, return the category as fallback.
             */
            return (item.element as AbstractNode).label
        } else {
            val element = item.element
            // This is a language column, so we have to find the translation that belongs to this column
            val node = (element as KeyNode).children().toList().firstOrNull { (it as TranslationNode).languageNode?.label == this.header }
            when (node) {
                is TranslationNode -> node.translation
                else -> "" // No translation exists
            }
        }
    }

    override fun getComparator(): Comparator<NodeDescriptor<*>> {
        return myComparator
    }
}