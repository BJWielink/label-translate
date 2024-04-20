package org.wielink.labelTranslate.toolWindow

import com.intellij.ui.components.JBTreeTable
import org.wielink.labelTranslate.model.CoreTreeTableModel
import org.wielink.labelTranslate.model.node.CategoryNode
import org.wielink.labelTranslate.model.node.FileNode
import org.wielink.labelTranslate.model.node.LanguageNode
import org.wielink.labelTranslate.model.node.TranslationNode
import org.wielink.labelTranslate.util.RecursionUtility
import org.wielink.labelTranslate.util.TreeUtility
import java.awt.Dimension
import javax.swing.JPanel

class CoreToolWindow : JPanel() {
    init {
        val fileNode = FileNode("labels")

        val enNode = LanguageNode("en", "")
        val enHelloNode = TranslationNode("hello", "hello")
        enNode.addTranslationNode(enHelloNode)
        val enErrorCategory = CategoryNode("error")
        val enWarningNode = TranslationNode("warning", "warning")
        enErrorCategory.addTranslationNode(enWarningNode)
        enNode.addCategoryNode(enErrorCategory)
        fileNode.addLanguageNode(enNode)

        val nlNode = LanguageNode("nl", "")
        val nlHelloNode = TranslationNode("hello", "hallo")
        nlNode.addTranslationNode(nlHelloNode)
        val nlErrorCategory = CategoryNode("error")
        val nlWarningNode = TranslationNode("warning", "waarschuwing")
        nlErrorCategory.addTranslationNode(nlWarningNode)
        nlNode.addCategoryNode(nlErrorCategory)
        fileNode.addLanguageNode(nlNode)

        val dlNode = LanguageNode("dl", "")
        val dlHelloNode = TranslationNode("hello", "gütentag")
        dlNode.addTranslationNode(dlHelloNode)
        val dlTestCategory = CategoryNode("test")
        val dlWarningNode = TranslationNode("warning", "wärschuwing")
        dlTestCategory.addTranslationNode(dlWarningNode)
        dlNode.addCategoryNode(dlTestCategory)
        fileNode.addLanguageNode(dlNode)

        val model = TreeUtility.toRepresentationModel(fileNode)

        /*
         * - Create a: merge(fileNode) method
         *  - The method merges all keys into: LinkedTranslationNode, LinkedCategoryNode
         *  - Keeps track of the links so that the headers can match the language label
         */

        val treeTable = JBTreeTable(model)
        treeTable.preferredSize = Dimension(500, 500)
        add(treeTable)
    }
}