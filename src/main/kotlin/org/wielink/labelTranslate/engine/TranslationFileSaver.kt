package org.wielink.labelTranslate.engine

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.ArrayHashElement
import org.wielink.labelTranslate.enum.NodeType
import org.wielink.labelTranslate.model.node.*
import org.wielink.labelTranslate.service.TranslationFileParseService
import java.io.File

class TranslationFileSaver(
    private val project: Project,
    private val updatedValue: String,
    private val categoryPath: List<String>,
    private val filePath: String,
    private val fileNode: FileNode,
    private val translationLabel: String
) {
    fun save() {
        val languageFile = File(filePath)

        if (!languageFile.exists() || !languageFile.isFile) {
            return
        }

        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(languageFile) ?: return
        val psiFile = virtualFile.findPsiFile(project) ?: return

        val languageNode = fileNode.children().toList().first { (it as LanguageNode).filePath == filePath }
        val translationNode = ensureNodeExists(languageNode as LanguageNode, categoryPath)
        translationNode.translation = updatedValue

        // Add the translations
        ApplicationManager.getApplication().invokeLater {
            WriteCommandAction.runWriteCommandAction(project) {
                deserializeNodeIntoPsi(psiFile, languageNode)

                // Add indenting to the newly created file
                val codeStyleManager = CodeStyleManager.getInstance(project)
                codeStyleManager.reformat(psiFile)
                project.service<TranslationFileParseService>().onFileChanged(listOf(virtualFile))
            }
        }
    }

    private fun ensureNodeExists(languageNode: LanguageNode, labelPath: List<String>): TranslationNode {
        var nodeIterator: AbstractNode = languageNode
        for (labelItem in labelPath) {
            var match = nodeIterator.children().toList().firstOrNull { it.type == NodeType.CATEGORY && it.label == labelItem }
            if (match == null) {
                match = CategoryNode(labelItem)
                if (nodeIterator.type == NodeType.LANGUAGE) {
                    (nodeIterator as LanguageNode).addCategoryNode(match)
                } else {
                    (nodeIterator as CategoryNode).addCategoryNode(match)
                }
            }
            nodeIterator = match
        }

        val match = nodeIterator.children().toList().firstOrNull { it.type == NodeType.TRANSLATION && it.label == translationLabel } as TranslationNode?
        if (match != null) {
            return match
        }

        val translationNode = TranslationNode(translationLabel, "") // Dummy
        if (nodeIterator.type == NodeType.LANGUAGE) {
            (nodeIterator as LanguageNode).addTranslationNode(translationNode)
        } else {
            (nodeIterator as CategoryNode).addTranslationNode(translationNode)
        }
        return translationNode
    }

    private fun deserializeCategory(arrayElementToFill: PsiElement, nodeToDeserialize: AbstractNode) {
        // Initialize the array
        val array = PhpPsiElementFactory.createPhpPsiFromText(project, ArrayCreationExpression::class.java, "[")

        // Sort alphabetically to mitigate merge conflicts
        val children = nodeToDeserialize.children().toList().sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
        for (childNodeToDeserialize in children) {
            if (childNodeToDeserialize.type == NodeType.KEY) {
                continue
            }

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