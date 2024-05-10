package org.wielink.labelTranslate.model.node

import org.wielink.labelTranslate.enum.NodeType
import java.util.*
import javax.swing.tree.TreeNode

abstract class AbstractNode(val type: NodeType): TreeNode {
    private var parent: AbstractNode? = null

    private var children: MutableList<AbstractNode>? = null

    abstract val label: String

    protected fun addNode(node: AbstractNode) {
        var children = this.children

        if (children == null) {
            children = mutableListOf()
            this.children = children
        }

        node.parent = this
        children.add(node)
    }

    override fun getChildAt(childIndex: Int): TreeNode {
        val children = this.children ?: throw Exception("Unable to find child at index: $childIndex")
        return children[childIndex]
    }

    override fun getChildCount(): Int {
        return children?.size ?: 0
    }

    override fun getParent(): AbstractNode? {
        return parent
    }

    override fun getIndex(node: TreeNode?): Int {
        return children?.indexOf(node) ?: -1
    }

    override fun getAllowsChildren(): Boolean {
        return type != NodeType.TRANSLATION && type != NodeType.KEY
    }

    override fun isLeaf(): Boolean {
        return type == NodeType.KEY || (children?.isEmpty() ?: true)
    }

    override fun children(): Enumeration<out AbstractNode> {
        return Collections.enumeration(children ?: emptyList())
    }

    abstract fun clone(): AbstractNode

    fun viewChildren(): Array<out AbstractNode> {
        val viewChildren = children?.filter { it !is TranslationNode }?.toTypedArray() ?: emptyArray()
        return viewChildren
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (other !is AbstractNode) return false
        return other.label == label
    }

    override fun hashCode(): Int {
        return label.hashCode()
    }
}
