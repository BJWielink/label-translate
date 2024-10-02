package org.wielink.labelTranslate.engine

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.readText
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiRecursiveElementVisitor
import com.jetbrains.php.lang.PhpLanguage
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.*
import org.wielink.labelTranslate.enum.NodeType
import org.wielink.labelTranslate.model.node.*
import java.io.File

class TranslationFileParser(private val project: Project) {
    fun buildTree(rootPath: String): RootNode {
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

                val languageNode = LanguageNode(translationDirectory.name, languageFile.absolutePath)
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
        val psiFile = PsiFileFactory.getInstance(project).createFileFromText(PhpLanguage.INSTANCE, virtualFile.readText())
        psiFile.accept(object: PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (isInitialCategoryNode(element)) {
                    for (child in element.children) {
                        visitCategory(languageNode, child)
                    }
                    return
                }

                super.visitElement(element)
            }

            private fun visitCategory(node: AbstractNode, element: PsiElement) {
                var currentNode = node

                // Advanced depth
                if (element is ArrayCreationExpression) {
                    var key: String? = null

                    // Find key that belongs to this category
                    val binaryWrapper = element.parent?.parent
                    if (binaryWrapper is ArrayHashElement) {
                        val stringElement = binaryWrapper.firstPsiChild?.firstPsiChild
                        if (stringElement is StringLiteralExpression) {
                            key = stringElement.contents
                        }
                    }

                    // Attach node and advance
                    if (key != null) {
                        currentNode = CategoryNode(key)

                        if (node.type == NodeType.LANGUAGE) {
                            (node as LanguageNode).addCategoryNode(currentNode)
                        } else if (node.type == NodeType.CATEGORY) {
                            (node as CategoryNode).addCategoryNode(currentNode)
                        }
                    }
                } else if (element is ArrayHashElement) {
                    // Add the actual translations to the tree
                    val stringOperands = element.children
                        .filter { psiElement -> psiElement is PhpPsiElement && psiElement.firstPsiChild is StringLiteralExpression }
                        .map { (it as PhpPsiElement).firstPsiChild as StringLiteralExpression }

                    if (stringOperands.size == 2) {
                        // Unescape the escape characters. When we save, we add them again
                        val key = stringOperands[0].contents.replace("\\'", "'")
                        val value = stringOperands[1].contents.replace("\\'", "'")
                        val translationNode = TranslationNode(key, value)
                        if (currentNode.type == NodeType.LANGUAGE) {
                            (currentNode as LanguageNode).addTranslationNode(translationNode)
                        } else if (currentNode.type == NodeType.CATEGORY) {
                            (currentNode as CategoryNode).addTranslationNode(translationNode)
                        }
                    }
                }

                for (child in element.children) {
                    visitCategory(currentNode, child)
                }
            }
        })
    }

    companion object {
        fun isInitialCategoryNode(element: PsiElement): Boolean {
            return element is ArrayCreationExpression
                    && element.parent is PhpReturn
                    && element.parent?.parent?.parent is PhpFile
        }

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

        fun isTranslationFile(file: File): Boolean {
            return file.exists() && file.isFile && file.extension == "php"
        }
    }
}