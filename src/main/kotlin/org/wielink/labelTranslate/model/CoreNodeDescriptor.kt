package org.wielink.labelTranslate.model

import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.project.Project
import org.wielink.labelTranslate.model.node.AbstractNode

class CoreNodeDescriptor(project: Project, parentDescriptor: NodeDescriptor<*>?, private val element: Any) : NodeDescriptor<Any>(project, parentDescriptor) {
    init {
        myName = if (element is AbstractNode) element.label else element.toString()
    }

    override fun update(): Boolean {
        return false
    }

    override fun getElement(): Any {
        return element
    }
}