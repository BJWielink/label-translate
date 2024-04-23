package org.wielink.labelTranslate.model

import com.intellij.ide.util.treeView.AbstractTreeStructure
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.project.Project
import org.wielink.labelTranslate.model.node.AbstractNode
import org.wielink.labelTranslate.model.node.RootNode

class CoreTreeStructure(private val project: Project, private var rootNode: RootNode) : AbstractTreeStructure() {
    override fun getRootElement(): Any {
        return rootNode
    }

    override fun getChildElements(element: Any): Array<out AbstractNode> {
        return (element as AbstractNode).viewChildren()
    }

    override fun getParentElement(element: Any): Any? {
        return (element as AbstractNode).parent
    }

    override fun createDescriptor(element: Any, parentDescriptor: NodeDescriptor<*>?): NodeDescriptor<*> {
        return CoreNodeDescriptor(project, parentDescriptor, element)
    }

    override fun commit() {
    }

    override fun hasSomethingToCommit(): Boolean {
        return false
    }

    fun setRootElement(rootNode: RootNode) {
        this.rootNode = rootNode
    }
}