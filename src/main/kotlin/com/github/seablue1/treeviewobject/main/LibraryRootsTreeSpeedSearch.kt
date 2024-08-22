package com.github.seablue1.treeviewobject.main

import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.treeStructure.Tree
import java.io.File
import java.util.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

internal class LibraryRootsTreeSpeedSearch(tree: Tree?) : TreeSpeedSearch(tree) {
    public override fun isMatchingElement(element: Any, pattern: String): Boolean {
        val userObject = ((element as TreePath).lastPathComponent as DefaultMutableTreeNode).userObject

        var str: String? = getElementText(element) ?: return false
        if (!hasCapitals(pattern)) { // be case-sensitive only if user types capitals
            str = StringUtil.toLowerCase(str)
        }
        if (pattern.contains(File.separator)) {
            return compare(str!!, pattern)
        }
        val tokenizer = StringTokenizer(str, File.separator)
        while (tokenizer.hasMoreTokens()) {
            val token = tokenizer.nextToken()
            if (compare(token, pattern)) {
                return true
            }
        }
        return false

        // if (userObject instanceof ItemElement) {
        //
        // }
        // else {
        //     return super.isMatchingElement(element, pattern);
        // }
    }

    companion object {
        private fun hasCapitals(str: String): Boolean {
            for (idx in 0 until str.length) {
                if (Character.isUpperCase(str[idx])) {
                    return true
                }
            }
            return false
        }
    }
}

