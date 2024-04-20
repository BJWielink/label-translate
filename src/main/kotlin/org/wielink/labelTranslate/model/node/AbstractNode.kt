package org.wielink.labelTranslate.model.node

import org.wielink.labelTranslate.enum.NodeType
import java.util.*
import javax.swing.tree.TreeNode

abstract class AbstractNode(val type: NodeType): TreeNode {
    private var parent: AbstractNode? = null

    private var children: MutableList<AbstractNode>? = null

    abstract val label: String

    fun addNode(node: AbstractNode) {
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

    override fun getParent(): TreeNode? {
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
}
