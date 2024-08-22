package com.github.seablue1.treeviewobject.main

import java.awt.Cursor
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class NavigatePanel : JPanel() {
    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        border = EmptyBorder(5, 0, 5, 0)
        this.add(JLabel(" "))
    }

    fun setLabelList(jLabelList: List<JLabel>?) {
        this.removeAll()

        if (jLabelList != null && !jLabelList.isEmpty()) {
            for (jLabel in jLabelList) {
                jLabel.cursor = Cursor(Cursor.HAND_CURSOR)
                this.add(jLabel)
            }
        }

        this.revalidate() // 重新验证此容器
        this.repaint() // 重新绘制此容器
    }

    fun clear() {
        this.removeAll()
        layout = null
        border = null
    }
}
