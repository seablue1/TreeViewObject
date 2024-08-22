package com.github.seablue1.treeviewobject.main

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "MyPluginSettings", storages = [Storage("myPluginSettings.xml")])
class MyPluginSettings : PersistentStateComponent<PersistData?> {
    override fun getState(): PersistData? {
        return null
    }

    override fun loadState(state: PersistData) {
    }
}
