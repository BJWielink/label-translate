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
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
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

        // Create a copy
        val copy = psiFile.copy() as PsiFile
        copy.name = "updated.php"

        // Add the translations
        traverseNodes(copy, languageNode)

        // Format the code
        val codeStyleManager = CodeStyleManager.getInstance(project)
        codeStyleManager.reformat(copy)

        psiFile.containingDirectory.add(copy)

        translationNode.translation = updatedValue
    }

    private fun addCategory(element: PsiElement, node: AbstractNode) {
        val type = PhpPsiElementFactory.createPhpPsiFromText(project, ArrayCreationExpression::class.java, "[")

        for (child in node.children()) {
            val label = child.label.replace("'", "\\'")
            // Prepare key value pair by, firstly, adding the key
            val keyValuePair = PhpPsiElementFactory.createPhpPsiFromText(project, ArrayHashElement::class.java, "['$label'     =>]")
            if (child is TranslationNode) {
                // Add a simple translation to the current array
                val translation = child.translation.replace("'", "\\'")
                keyValuePair.add(PhpPsiElementFactory.createStringLiteralExpression(project, translation, true))
            } else {
                // Create dummy that has to be filled with data
                val categoryElement = PhpPsiElementFactory.createPhpPsiFromText(project, ArrayCreationExpression::class.java, "[]")
                val addedElement = keyValuePair.add(categoryElement)
                // Recursively add data to the dummy
                addCategory(addedElement, child)
            }
            type.add(PhpPsiElementFactory.createNewLine(project))
            type.add(keyValuePair)
            type.add(PhpPsiElementFactory.createComma(project))
            type.add(PhpPsiElementFactory.createNewLine(project))
        }

        val end = PhpPsiElementFactory.createFromText(project, LeafPsiElement::class.java, "]")!!
        type.add(end)

        element.replace(type)
    }

    private fun traverseNodes(element: PsiElement, languageNode: LanguageNode) {
        // Find the array initializer
        if (TranslationFileParser.isInitialCategoryNode(element)) {
            addCategory(element, languageNode)
            return
        }

        for (child in element.children) {
            traverseNodes(child, languageNode)
        }
    }

    private fun traverseTranslationArray(element: PsiElement) {
        if (element is ArrayHashElement) {
            mutateKeyValue(element)
        }

        for (child in element.children) {
            traverseTranslationArray(child)
        }
    }

    private fun mutateKeyValue(element: PsiElement) {
        val stringOperands = element.children
            .filterIsInstance<PhpPsiElement>()
            .map { it.firstPsiChild }
            .filterIsInstance<StringLiteralExpression>()

        if (stringOperands.size != 2) {
            return
        }

        stringOperands[1].updateText("test")
        println(element)
    }
}