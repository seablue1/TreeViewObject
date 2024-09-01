package com.github.seablue1.treeviewobject.main

import com.github.seablue1.treeviewobject.main.MyStoreService.MyStore
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class MyConfigurable : Configurable {
    override fun getDisplayName(): String {
        return "TreeViewObject"
    }


    override fun disposeUIResources() {
        settingGui = null
    }

    var settingGui: MySettingUI? = null

    override fun createComponent(): JComponent? {
        val store: MyStore = MyStoreService.Companion.getMyStore()
        settingGui = MySettingUI(store)
        return settingGui!!.root
    }

    override fun isModified(): Boolean {
        val store: MyStore? = MyStoreService.Companion.getMyStore()
        if (store == null || settingGui == null) {
            return false
        }
        return store != settingGui!!.setting
    }

    override fun apply() {
        MyStoreService.Companion.setMyStore(settingGui?.setting)
    }
}
