package com.github.seablue1.treeviewobject.main

import com.github.seablue1.treeviewobject.main.MyStoreService.MyStore
import com.google.gson.GsonBuilder
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.ui.content.Content
import com.intellij.util.concurrency.NonUrgentExecutor
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UastLanguagePlugin
import org.jetbrains.uast.UastLanguagePlugin.Companion.getInstances
import org.jetbrains.uast.findContaining
import java.util.*

class MainAction : AnAction() {
    private val gsonBuilder: GsonBuilder = GsonBuilder().setPrettyPrinting()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        val psiElement = e.getData(CommonDataKeys.PSI_ELEMENT)

        val psiFile = e.getData(LangDataKeys.PSI_FILE)

        // if (!uastSupported(psiFile)) {
        //     Notifier.notifyWarn("This file can't convert to json.", project);
        //     return;
        // }
        if (project == null) {
            return;
        }
        var uClass: UClass? = null
        if (editor != null) {
            val elementAt = psiFile!!.findElementAt(editor.caretModel.offset)
            uClass = elementAt.findContaining(UClass::class.java)
        }

        if (uClass == null) {
            val fileText = psiFile!!.text
            val offset = if (fileText.contains("class")) fileText.indexOf("class") else fileText.indexOf("record")
            if (offset < 0) {
                Notifier.notifyWarn("Can't find class scope.", project)
                return
            }
            val elementAt = psiFile.findElementAt(offset)
            uClass = elementAt.findContaining(UClass::class.java)
        }

        val javaPsi = uClass!!.javaPsi


        val toolWindow: ToolWindow = ToolWindowManager.getInstance(project).getToolWindow("TreeViewObject") ?: return

        val contentManager = toolWindow.contentManager
        val mainPanel = MainPanel()
        var findOld = false
        if (CollectionUtils.isNotEmpty(Arrays.asList<Content?>(*contentManager.contents))) {
            for (content in contentManager.contents) {
                if (StringUtils.equals(javaPsi.name, content.displayName)) {
                    content.setComponent(mainPanel)
                    contentManager.setSelectedContent(content)
                    findOld = true
                    break
                }
            }
        }
        if (!findOld) {
            val content = contentManager.factory.createContent(mainPanel, javaPsi.name, true)
            content.isCloseable = true
            contentManager.addContent(content)
            contentManager.setSelectedContent(content)
        }


        toolWindow.activate {
            val progressIndicator =
                BackgroundableProcessIndicator(project, "TreeViewObject", "cancel", "stop", true)
            ReadAction
                .nonBlocking<MyNode> {
                    try {
                        val myNode = getFromRoot(javaPsi, javaPsi.name!!, psiFile!!.containingFile.virtualFile.path, project)
                        mainPanel.initData(myNode, project)
                        return@nonBlocking myNode
                    } finally {
                        progressIndicator.processFinish()
                    }
                }
                .wrapProgress(progressIndicator)
                .inSmartMode(project!!)
                .submit(NonUrgentExecutor.getInstance())
        }
    }

    private fun getFromRoot(psiClass: PsiClass, name: String, fileName: String, project: Project?): MyNode {
        val store: MyStore? = MyStoreService.Companion.getMyStore()
        val node = MyNode(name, psiClass.qualifiedName, fileName, ArrayList(), null)

        val allFields = psiClass.allFields
        for (field in allFields) {
            if (!field.hasModifierProperty(PsiModifier.STATIC)) {
                val parentList: MutableList<String?> = ArrayList()
                parentList.add(psiClass.qualifiedName)
                val comment = StringBuilder()
                if (store!!.showNote) {
                    for (element in field.children) {
                        if (element is PsiComment) {
                            comment.append(element.getText())
                        }
                    }
                }

                val child = getMyNode(field.type, field.name, comment.toString(), parentList, 1, project, store)
                if (child != null) {
                    node.children!!.add(child)
                }
            }
        }
        return node
    }

    private fun getMyNode(javaPsi: PsiType?, name: String, note: String, parentList: List<String?>, level: Int, project: Project?, store: MyStore?): MyNode? {
        if (level > store!!.deep) {
            Notifier.notifyWarn("level too deep, maybe a bug, maybe to large", project)
            return null
        }

        if (javaPsi is PsiPrimitiveType) {
            return MyNode(name, javaPsi.getPresentableText(), "", ArrayList(), note)
        }
        if (javaPsi is PsiArrayType) {
            val componentType = javaPsi.componentType
            return getMyNode(componentType, name, note, parentList, level, project, store)
        }
        var psiClass: PsiClass?
        if (javaPsi is PsiClassType) {
            psiClass = javaPsi.resolve()
        } else if (javaPsi is PsiWildcardType) {
            val extendsBound = javaPsi.extendsBound
            return getMyNode(extendsBound, name, note, parentList, level, project, store)
        } else {
            Notifier.notifyError("unknown type:" + javaPsi!!.javaClass.name, project)
            return null
        }


        if (psiClass!!.containingFile.virtualFile == null) {
            val fileScope = GlobalSearchScope.allScope(psiClass.project)
            psiClass = JavaPsiFacade.getInstance(psiClass.project).findClass(psiClass.qualifiedName!!, fileScope)
        }

        if (InheritanceUtil.isInheritor(psiClass, CommonClassNames.JAVA_LANG_ITERABLE)) {
            val typeToDeepType = PsiUtil.extractIterableTypeParameter(javaPsi, false)
            return getMyNode(typeToDeepType, name, note, parentList, level, project, store)
        }


        val jarName = psiClass!!.containingFile.virtualFile.path
        val node = MyNode(name, psiClass.qualifiedName, jarName, ArrayList(), note)

        if (psiClass.qualifiedName != null && psiClass.qualifiedName!!.startsWith("java.")) {
            return node
        }
        if (parentList.contains(psiClass.qualifiedName)) {
            node.isRecursive=true
            return node
        }

        val allFields = psiClass.allFields
        for (field in allFields) {
            if (!field.hasModifierProperty(PsiModifier.STATIC)) {
                val copyParentList: MutableList<String?> = ArrayList(parentList)
                copyParentList.add(psiClass.qualifiedName)

                val comment = StringBuilder()
                if (store.showNote) {
                    for (element in field.children) {
                        if (element is PsiComment) {
                            comment.append(element.getText())
                        }
                    }
                }

                val child = getMyNode(field.type, field.name, comment.toString(), copyParentList, level + 1, project, store)
                if (child != null) {
                    node.children!!.add(child)
                }
            }
        }
        return node
    }


    fun uastSupported(psiFile: PsiFile): Boolean {
        return getInstances()
            .stream()
            .anyMatch { l: UastLanguagePlugin -> l.isFileSupported(psiFile.name) }
    }
}
