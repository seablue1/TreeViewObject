package com.github.seablue1.treeviewobject.main

import com.github.seablue1.treeviewobject.main.MyStoreService.MyStore
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JTextField

class MySettingGui(store: MyStore?) {
    val root: JPanel? = null
    private val checkBox: JCheckBox? = null
    private val deep: JTextField? = null
    private val split: JTextField? = null

    init {
        checkBox!!.isSelected = store!!.showNote
        deep!!.text = store.deep.toString()
        split!!.text = store.split
    }

    val setting: MyStore
        get() {
            val setting = MyStore()
            setting.showNote = checkBox!!.isSelected
            setting.deep = deep!!.text.toInt()
            setting.split = split!!.text
            return setting
        }
}
