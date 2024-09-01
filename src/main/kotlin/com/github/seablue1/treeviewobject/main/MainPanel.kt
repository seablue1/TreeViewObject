package com.github.seablue1.treeviewobject.main

import com.google.common.collect.Lists
import com.intellij.icons.AllIcons
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.KeyStrokeAdapter
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.speedSearch.SpeedSearchUtil
import com.intellij.util.concurrency.NonUrgentExecutor
import org.apache.commons.lang3.StringUtils
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.JTree
import javax.swing.event.TreeSelectionEvent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class MainPanel : JPanel(), Disposable {
    private fun create(myNode: MyNode?): DefaultMutableTreeNode {
        val node = DefaultMutableTreeNode(myNode)

        if (myNode != null) {
            if (myNode.children != null) {
                for (child in myNode.children!!) {
                    node.add(create(child))
                }
            }
        }

        return node
    }

    fun initData(myNode: MyNode?, project: Project?) {
        val root = create(myNode)


        val treeModel = DefaultTreeModel(root)


        // FilteringTreeModel filteringTreeModel = FilteringTreeModel.createModel(new MyTreeStructure(project, root), MyTreeStructure.createFilter(), this);
        val tree = MyTree(treeModel, myNode)

        // 创建一个 DefaultTreeExpander 实例，并将 tree 作为参数
        // DefaultTreeExpander treeExpander = new DefaultTreeExpander(tree);
        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    e.consume()
                    val node = tree.lastSelectedPathComponent as DefaultMutableTreeNode
                    navigate(node, project)
                }
            }
        })

        tree.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                // System.out.println(e.isControlDown());
                // System.out.println(e.getKeyCode() == KeyEvent.VK_C);
                if (e.isControlDown && e.keyCode == KeyEvent.VK_C) {
                    var list: MutableList<String?> = ArrayList()
                    var node = tree.lastSelectedPathComponent as DefaultMutableTreeNode?
                    do {
                        list.add(node!!.userObject.toString())
                        node = node!!.parent as DefaultMutableTreeNode?
                    } while (node != null)

                    list = Lists.reverse(list)
                    val result = java.lang.String.join(MyStoreService.Companion.getMyStore()!!.split, list)

                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    clipboard.setContents(StringSelection(result), null)
                    e.consume()
                }
            }
        })


        //
        // tree.addFocusListener(new FocusAdapter() {
        //     @Override
        //     public void focusLost(FocusEvent e) {
        //         // Debug log to check focus lost event
        //         System.out.println("Tree lost focus");
        //         treeSpeedSearch.showPopup();
        //     }
        // });
        //
        // tree.addTreeSelectionListener(new TreeSelectionListener() {
        //     @Override
        //     public void valueChanged(TreeSelectionEvent e) {
        //         // Debug log to check selection changes
        //         System.out.println("Selection changed: " + tree.getSelectionPath());
        //     }
        // });

        // TreeSpeedSearch treeSpeedSearch = new TreeSpeedSearch(tree);
        // treeSpeedSearch.setCanExpand(true);
        // treeSpeedSearch.addChangeListener(e -> {
        //     treeSpeedSearch.getSearchField()
        // });


        // ColoredTreeCellRenderer renderer = new ColoredTreeCellRenderer() {
        //     @Override
        //     public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        //         MyNode myNode = ((MyNode) ((DefaultMutableTreeNode) value).getUserObject());
        //         append(myNode.getName());
        //         SpeedSearchComparator speedSearchComparator = ((MyTree) tree).getSpeedSearchComparator();
        //         if (speedSearchComparator != null && StringUtils.isNotBlank(speedSearchComparator.getRecentSearchText())) {
        //             Iterable<TextRange> ranges = speedSearchComparator.matchingFragments(speedSearchComparator.getRecentSearchText(), value.toString());
        //             SpeedSearchUtil.applySpeedSearchHighlighting(this, ranges, hasFocus);
        //         }
        //     }
        // };
        val nodeRenderer: NodeRenderer = object : NodeRenderer() {
            override fun customizeCellRenderer(tree: JTree, value: @NlsSafe Any?, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
                super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus)

                var string = value.toString()
                if (value is DefaultMutableTreeNode) {
                    val myNode = (value.userObject as MyNode)
                    if (myNode.isRecursive) {
                        setIcon(AllIcons.Gutter.RecursiveMethod)
                    }
                    if (StringUtils.isNotBlank(myNode.note)) {
                        val s: String = MyNode.Companion.spaces + myNode.note
                        append(s, SimpleTextAttributes.GRAYED_ATTRIBUTES, true)
                        string = string + s
                    }
                }

                val speedSearchComparator = (tree as MyTree).speedSearchComparator
                if (speedSearchComparator != null && StringUtils.isNotBlank(speedSearchComparator.recentSearchText)) {
                    val ranges = speedSearchComparator.matchingFragments(speedSearchComparator.recentSearchText, string)
                    SpeedSearchUtil.applySpeedSearchHighlighting(this, ranges, hasFocus)
                }
            }
        }
        tree.cellRenderer = nodeRenderer


        val textField = JTextField()
        run {
            textField.addKeyListener(object : KeyStrokeAdapter() {
                override fun keyReleased(event: KeyEvent) {
                    tree.filterNode2(textField.text)
                    tree.repaint()
                }
            })
        }


        // JPanel labelsPanel = new JPanel();
        // {
        //     JLabel label1 = new JLabel("12312>");
        //     label1.setCursor(new Cursor(Cursor.HAND_CURSOR));
        //
        //     JLabel label2 = new JLabel("12312");
        //     label2.setCursor(new Cursor(Cursor.HAND_CURSOR));
        //
        //     // labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.X_AXIS));
        //     // labelsPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
        //     labelsPanel.add(label1);
        //     labelsPanel.add(label2);
        // }
        val topPanel = JPanel()
        val comp = NavigatePanel()
        run {
            topPanel.layout = BorderLayout()
            topPanel.add(textField, BorderLayout.NORTH)
            topPanel.add(comp, BorderLayout.CENTER)
        }


        tree.addTreeSelectionListener { e: TreeSelectionEvent? ->
            var node = tree.lastSelectedPathComponent as DefaultMutableTreeNode?
            val leaf = node
            var jLabelList: MutableList<JLabel> = ArrayList()
            do {
                val myNode1 = node!!.userObject as MyNode
                var text = myNode1.name
                if (node !== leaf) {
                    text = "$text  >  "
                }
                val jLabel = JLabel(text)
                jLabel.cursor = Cursor(Cursor.HAND_CURSOR)

                val finalNode = node
                jLabel.addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        navigate(finalNode, project)
                    }
                })

                jLabelList.add(jLabel)

                node = node!!.parent as DefaultMutableTreeNode?
            } while (node != null)

            jLabelList = Lists.reverse(jLabelList)
            comp.setLabelList(jLabelList)
        }

        this.layout = BorderLayout()
        this.add(topPanel, BorderLayout.NORTH)
        this.add(JBScrollPane(tree), BorderLayout.CENTER)
    }

    fun navigate(node: DefaultMutableTreeNode?, project: Project?) {
        if (node == null) return

        val parent = node.parent as DefaultMutableTreeNode
        if (parent == null) {
            val fileScope = GlobalSearchScope.allScope(project!!)
            val nodeInfo = (node.userObject as MyNode)
            val classes = nodeInfo.className?.let { JavaPsiFacade.getInstance(project).findClasses(it, fileScope) }
            classes?.get(0)?.navigate(true)
            return
        }

        val parentNode = (parent.userObject as MyNode)
        val nodeInfo = (node.userObject as MyNode)

        // TreePath treePath = new TreePath(node.getPath());
        // tree.setSelectionPath(treePath);
        ReadAction
            .nonBlocking<Runnable?> {
                val fileScope = GlobalSearchScope.allScope(project!!)
                val classes = parentNode.className?.let { JavaPsiFacade.getInstance(project).findClasses(it, fileScope) }
                if (classes != null) {
                    for (psiClass in classes) {
                        for (field in psiClass.allFields) {
                            if (field.name == nodeInfo.name) {
                                return@nonBlocking Runnable { field.navigate(true) }
                            }
                        }
                    }
                }
                null
            }
            .inSmartMode(project!!)
            .finishOnUiThread(ModalityState.defaultModalityState()) { task: Runnable? ->
                task?.run()
            }
            .submit(NonUrgentExecutor.getInstance())
    }

    // public NewNavBarPanel getNewNavBarPanel(Project project) {
    //     CoroutineContext context = Dispatchers.getIO();
    //     CoroutineScope coroutineScope = CoroutineScope(context);
    //
    //     NavBarVm navBarVm = createNavBarVmInstance(coroutineScope);
    //
    //     NewNavBarPanel newNavBarPanel = new NewNavBarPanel(coroutineScope, navBarVm, project, false  , false);
    //     return newNavBarPanel;
    //
    // }
    //
    // private NavBarVm createNavBarVmInstance(CoroutineScope coroutineScope) {
    //
    //
    //     DefaultNavBarItem<String> defaultNavBarItem = new DefaultNavBarItem("1231231");
    //
    //
    //     NavBarVmItem navBarVmItem = new NavBarVmItem() {
    //
    //         @NotNull
    //         @Override
    //         public Pointer<? extends NavBarItem> getPointer() {
    //             return defaultNavBarItem.createPointer();
    //         }
    //
    //         @NotNull
    //         @Override
    //         public NavBarItemPresentation getPresentation() {
    //             return new NavBarItemPresentation(null, "123", null,null,null,false);
    //         }
    //
    //         @Nullable
    //         @Override
    //         public Object children(@NotNull Continuation<? super List<? extends NavBarVmItem>> continuation) {
    //             return null;
    //         }
    //     };
    //
    //     List<NavBarVmItem> list = new ArrayList<>();
    //     list.add(navBarVmItem);
    //     NavBarVm navBarVm = new NavBarVmImpl(coroutineScope, list, emptyFlow());
    //     return navBarVm;
    // }
    override fun dispose() {
    }
}
