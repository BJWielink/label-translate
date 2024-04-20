package org.wielink.labelTranslate.engine

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.ArrayHashElement
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.jetbrains.php.lang.psi.elements.impl.ArrayCreationExpressionImpl
import com.jetbrains.rd.generator.nova.Root
import org.wielink.labelTranslate.enum.NodeType
import org.wielink.labelTranslate.model.node.*
import java.io.File

class TranslationFileParser(private val project: Project) {
    fun buildTree(rootPath: String): AbstractNode {
        val rootNode = RootNode()

        val rootFile = File(rootPath)
        val translationDirectoryCandidates = rootFile.listFiles() ?: return rootNode

        // Get all translation directories
        val translationDirectories = translationDirectoryCandidates.filter { it != null && folderIsTranslationDirectory(it) }

        // Get all categories based on the file structure
        val translationFileNames = translationDirectories
            .flatMap { it.listFiles()?.filterNotNull() ?: emptyList() }
            .map { it.nameWithoutExtension }
            .toHashSet()

        for (translationFileName in translationFileNames) {
            val fileNode = FileNode(translationFileName)
            rootNode.addFileNode(fileNode)

            for (translationDirectory in translationDirectories) {
                val languageFile = File(translationDirectory, "$translationFileName.php")

                if (!languageFile.exists()) {
                    continue
                }

                // TODO: Debug code
                if (translationFileName != "validation") {
                    continue
                }

                val languageNode = LanguageNode(translationFileName, languageFile.absolutePath)
                completeLanguageTree(languageNode)
                fileNode.addLanguageNode(languageNode)
            }
        }

        return rootNode
    }

    private fun completeLanguageTree(languageNode: LanguageNode) {
        val languageFile = File(languageNode.filePath)

        if (!languageFile.exists() || !languageFile.isFile) {
            return
        }

        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(languageFile) ?: return
        val psiFile = virtualFile.findPsiFile(project) ?: return

        var root: AbstractNode? = null
        psiFile.accept(object: PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ArrayCreationExpression) {
                    // Initial opening bracket has been reached, start tracking the current root
                    if (root == null) {
                        root = languageNode
                    }
                } else if (element is ArrayHashElement) {
                    // If the root is null, we have not reached the opening bracket yet, so just discard
                    if (root != null) {
                        val (key, value) = getKeyValueString(element)
                        println("$key, $value")
                    }
                }
                super.visitElement(element)
            }
        })
    }

    fun getKeyValueString(element: ArrayHashElement): Pair<String?, String?> {
        val operands = element.children.filterIsInstance<PhpPsiElement>()
        val leftOperand = operands.getOrNull(0)?.firstChild ?: return Pair(null, null)
        val rightOperand = operands.getOrNull(1)?.firstChild ?: return Pair(null, null)

        if (leftOperand !is StringLiteralExpression || rightOperand !is StringLiteralExpression) {
            return Pair(null, null)
        }

        return Pair(leftOperand.contents, rightOperand.contents)
    }

//    private fun completeLanguageTree(languageNode: AbstractNode) {
//        if (languageNode.type != NodeType.LANGUAGE) {
//            return
//        }
//
//        val languagePayload = languageNode.languagePayload ?: return
//        val file = File(languagePayload.filePath)
//
//        if (!file.exists() || !file.isDirectory) {
//            return
//        }
//
//        val categoryFiles = file.listFiles() ?: return
//        for (categoryFile in categoryFiles) {
//            if (!isTranslationFile(categoryFile)) {
//                continue
//            }
//
//            val categoryPayload = CategoryPayload(categoryFile.absolutePath)
//            val categoryNode = AbstractNode(NodeType.FILE, categoryPayload = categoryPayload)
//            completeCategoryTree(categoryNode)
//        }
//    }

//    private fun completeCategoryTree(categoryNode: AbstractNode) {
//        // Retrieve the PSI file
//        val categoryPayload = categoryNode.categoryPayload ?: return
//
//        if (!categoryPayload.filePath.contains("validation.php")) {
//            return
//        }
//
//        val categoryFile = File(categoryPayload.filePath)
//        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(categoryFile) ?: return
//        val psiFile = virtualFile.findPsiFile(project) ?: return
//
//        val arrayCreation = PsiTreeUtil.collectElementsOfType(psiFile, ArrayCreationExpressionImpl::class.java)
//
//        // Walk the file tree to create child wrapper and translation nodes
//        psiFile.accept(object: PsiRecursiveElementVisitor() {
//            override fun visitElement(element: PsiElement) {
//                if (element is ArrayCreationExpression) {
//                    println("Test")
//                }
//                super.visitElement(element)
//            }
//        })
//    }

    companion object {
        fun folderIsTranslationDirectory(file: File): Boolean {
            if (!file.exists() || !file.isDirectory) {
                return false
            }

            val childFiles = file.listFiles() ?: return false
            for (childFile in childFiles) {
                if (isTranslationFile(childFile)) {
                    return true
                }
            }

            return false
        }

        private fun isTranslationFile(file: File): Boolean {
            return file.exists() && file.isFile && file.extension == "php"
        }
    }
}