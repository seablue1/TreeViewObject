package com.github.seablue1.treeviewobject.main

import com.github.seablue1.treeviewobject.main.MyStoreService.MyStore
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JTextField

class MySettingUI (store: MyStore){
    var root: JPanel? = null
    var checkBox: JCheckBox? = null
    var deep: JTextField? = null
    var split: JTextField? = null

    init {
        checkBox!!.isSelected = store.showNote
        deep!!.text = store.deep.toString()
        split!!.text = store.split
    }

    val setting: MyStore
        get() {
            var setting = MyStore()
            setting.showNote = checkBox!!.isSelected
            setting.deep = deep!!.text.toInt()
            setting.split = split!!.text
            return setting
        }
}
