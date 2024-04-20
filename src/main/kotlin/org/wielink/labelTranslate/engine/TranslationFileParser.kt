package org.wielink.labelTranslate.engine

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.impl.ArrayCreationExpressionImpl
import org.wielink.labelTranslate.enum.NodeType
import org.wielink.labelTranslate.model.node.AbstractNode
import org.wielink.labelTranslate.model.node.RootNode
import java.io.File

class TranslationFileParser(private val project: Project) {
    fun buildTree(rootPath: String): AbstractNode {
//        val rootNode = AbstractNode(NodeType.ROOT)
//
//        val rootFile = File(rootPath)
//        val translationDirectoryCandidates = rootFile.listFiles() ?: return rootNode
//
//        for (translationDirectoryCandidate in translationDirectoryCandidates) {
//            if (!folderIsTranslationDirectory(translationDirectoryCandidate)) {
//                continue
//            }
//
//            val languagePayload = LanguagePayload(translationDirectoryCandidate.absolutePath)
//            val childNode = AbstractNode(NodeType.LANGUAGE, languagePayload = languagePayload)
//            completeLanguageTree(childNode)
//            rootNode.addChild(childNode)
//        }
//
//        return rootNode
        return RootNode()
    }

    private fun completeLanguageTree(languageNode: AbstractNode) {
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
    }

    private fun completeCategoryTree(categoryNode: AbstractNode) {
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
    }

    companion object {
        fun folderIsTranslationDirectory(file: File): Boolean {
            if (!file.isDirectory) {
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
            return file.isFile && file.extension == "php";
        }
    }
}