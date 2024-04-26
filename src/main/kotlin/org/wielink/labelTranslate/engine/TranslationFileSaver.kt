package org.wielink.labelTranslate.engine

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.ArrayHashElement
import org.wielink.labelTranslate.enum.NodeType
import org.wielink.labelTranslate.model.node.AbstractNode
import org.wielink.labelTranslate.model.node.LanguageNode
import org.wielink.labelTranslate.model.node.TranslationNode
import java.io.File

class TranslationFileSaver(
    private val project: Project,
    private val translationNode: TranslationNode,
    private val updatedValue: String
) {
    fun save() {
        val languageNode = translationNode.languageNode ?: return
        val languageFile = File(languageNode.filePath)

        if (!languageFile.exists() || !languageFile.isFile) {
            return
        }

        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(languageFile) ?: return
        val psiFile = virtualFile.findPsiFile(project) ?: return

        // TODO: When deserialization works, just replace the existing psi file
        val psiOutputFile = psiFile.copy() as PsiFile
        psiOutputFile.name = "updated.php"

        // Add modified translation to language node
        translationNode.translation = updatedValue

        // Sort nodes alphabetically
        sortAlphabetically(languageNode)

        // Add the translations
        deserializeNodeIntoPsi(psiOutputFile, languageNode)

        // Add indenting to the newly created file
        val codeStyleManager = CodeStyleManager.getInstance(project)
        codeStyleManager.reformat(psiOutputFile)

        // Write the psi file to the disk
        psiFile.containingDirectory.add(psiOutputFile)
    }

    private fun sortAlphabetically(node: AbstractNode) {
        node.sortAlphabetically()

        for (child in node.children()) {
            if (child.type == NodeType.CATEGORY) {
                sortAlphabetically(child)
            }
        }
    }

    private fun deserializeCategory(arrayElementToFill: PsiElement, nodeToDeserialize: AbstractNode) {
        // Initialize the array
        val array = PhpPsiElementFactory.createPhpPsiFromText(project, ArrayCreationExpression::class.java, "[")

        for (childNodeToDeserialize in nodeToDeserialize.children()) {
            val label = childNodeToDeserialize.label.replace("'", "\\'")
            // Prepare key value pair by, firstly, adding the key
            val keyValuePair = PhpPsiElementFactory.createPhpPsiFromText(project, ArrayHashElement::class.java, "['$label' =>]")
            if (childNodeToDeserialize is TranslationNode) {
                // Add a simple translation to the current array
                val translation = childNodeToDeserialize.translation.replace("'", "\\'")
                keyValuePair.add(PhpPsiElementFactory.createStringLiteralExpression(project, translation, true))
            } else {
                // Create dummy that has to be filled with data
                val categoryElement = PhpPsiElementFactory.createPhpPsiFromText(project, ArrayCreationExpression::class.java, "[]")
                val addedElement = keyValuePair.add(categoryElement)
                // Recursively add data to the dummy
                deserializeCategory(addedElement, childNodeToDeserialize)
            }
            array.add(PhpPsiElementFactory.createNewLine(project))
            array.add(keyValuePair)
            array.add(PhpPsiElementFactory.createComma(project))
            array.add(PhpPsiElementFactory.createNewLine(project))
        }

        // End the array
        val arrayClosureElement = PhpPsiElementFactory.createFromText(project, LeafPsiElement::class.java, "]")!!
        array.add(arrayClosureElement)

        // Finally we replace ourselves (we are an empty array) with translation and other category data
        arrayElementToFill.replace(array)
    }

    private fun deserializeNodeIntoPsi(element: PsiElement, languageNode: LanguageNode) {
        // Find the array initializer
        if (TranslationFileParser.isInitialCategoryNode(element)) {
            deserializeCategory(element, languageNode)
            return
        }

        for (child in element.children) {
            deserializeNodeIntoPsi(child, languageNode)
        }
    }
}