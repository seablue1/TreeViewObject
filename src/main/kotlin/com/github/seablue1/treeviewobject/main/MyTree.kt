package com.github.seablue1.treeviewobject.main

import com.intellij.openapi.util.TextRange
import com.intellij.ui.SpeedSearchComparator
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.treeStructure.Tree
import org.apache.commons.lang3.StringUtils
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath

class MyTree internal constructor(var baseTreeModel: TreeModel, myNode: MyNode?) : Tree(baseTreeModel) {
    var filterTreeModel: TreeModel? = null
    var current: TreeModel
    var myNode: MyNode?
    var speedSearchComparator: SpeedSearchComparator = SpeedSearchComparator(false, true)


    init {
        current = treeModel
        this.myNode = myNode
    }

    fun filterNode2(pattern: String?) {
        if (pattern == null || pattern.length == 0) {
            if (current !== baseTreeModel) {
                current = baseTreeModel
                model = current
            }
            try {
                val field = speedSearchComparator.javaClass.getDeclaredField("myRecentSearchText")
                field.isAccessible = true
                field[speedSearchComparator] = ""
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        } else {
            // try {
            //     Field field = speedSearchComparator.getClass().getDeclaredField("myRecentSearchText");
            //     field.setAccessible(true);
            //     field.set(speedSearchComparator, pattern);
            // } catch (Exception e) {
            //     throw new RuntimeException(e);
            // }


            val defaultMutableTreeNode = buildChildAndReturnResult2(myNode, speedSearchComparator, pattern)
            current = DefaultTreeModel(defaultMutableTreeNode)

            // render(this.getCellRenderer());
            model = current
            expandAllNodes(this, TreePath(defaultMutableTreeNode))
        }
    }

    // private void render(TreeCellRenderer renderer) {
    //     SpeedSearchUtil.applySpeedSearchHighlighting(renderer,);
    // }
    private fun hasContent2(speedSearch: SpeedSearchComparator, text: String?, pattern: String): Boolean {
        if (StringUtils.isBlank(text)) {
            return false
        }
        val ranges = speedSearch.matchingFragments(pattern, text!!)
        val rangesIterator: Iterator<TextRange>? = ranges?.iterator()
        return rangesIterator != null && rangesIterator.hasNext()
    }

    private fun buildChildAndReturnResult2(node: MyNode?, speedSearchComparator: SpeedSearchComparator, pattern: String): DefaultMutableTreeNode? {
        if (node == null) {
            return DefaultMutableTreeNode()
        }

        val childResults: MutableList<DefaultMutableTreeNode> = ArrayList()
        for (child in node.children!!) {
            val childResult = buildChildAndReturnResult2(child, speedSearchComparator, pattern)
            if (childResult != null) {
                childResults.add(childResult)
            }
        }

        if (childResults.size > 0 || hasContent2(speedSearchComparator, node.name, pattern) || hasContent2(speedSearchComparator, node.note, pattern)) {
            val defaultMutableTreeNode = DefaultMutableTreeNode()
            defaultMutableTreeNode.userObject = node
            for (childResult in childResults) {
                defaultMutableTreeNode.add(childResult)
            }
            return defaultMutableTreeNode
        }

        return null
    }


    fun filterNode(text: String?, speedSearch: TreeSpeedSearch) {
        if (text == null || text.length == 0) {
            if (current !== baseTreeModel) {
                current = baseTreeModel
                model = current
            }
        } else {
            val defaultMutableTreeNode = buildChildAndReturnResult(myNode, speedSearch)
            current = DefaultTreeModel(defaultMutableTreeNode)
            model = current
            expandAllNodes(this, TreePath(defaultMutableTreeNode))
        }
    }

    private fun buildChildAndReturnResult(node: MyNode?, speedSearch: TreeSpeedSearch): DefaultMutableTreeNode? {
        if (node == null) {
            return null
        }

        val childResults: MutableList<DefaultMutableTreeNode> = ArrayList()
        for (child in node.children!!) {
            val childResult = buildChildAndReturnResult(child, speedSearch)
            if (childResult != null) {
                childResults.add(childResult)
            }
        }

        if (childResults.size > 0 || hasContent(speedSearch, node.name)) {
            val defaultMutableTreeNode = DefaultMutableTreeNode()
            defaultMutableTreeNode.userObject = node
            for (childResult in childResults) {
                defaultMutableTreeNode.add(childResult)
            }
            return defaultMutableTreeNode
        }

        return null
    }

    private fun hasContent(speedSearch: TreeSpeedSearch, name: String?): Boolean {
        val ranges = speedSearch.matchingFragments(name!!)
        val rangesIterator: Iterator<TextRange>? = ranges?.iterator()
        return rangesIterator != null && rangesIterator.hasNext()
    }

    companion object {
        private fun expandAllNodes(tree: Tree, parent: TreePath) {
            // Expand the current node
            tree.expandPath(parent)

            // Get the node represented by the parent path
            val node = parent.lastPathComponent as DefaultMutableTreeNode

            // Iterate over each child
            for (i in 0 until node.childCount) {
                val childNode = node.getChildAt(i) as DefaultMutableTreeNode
                val childPath = parent.pathByAddingChild(childNode)
                expandAllNodes(tree, childPath) // Recursive call
            }
        }
    }
}
