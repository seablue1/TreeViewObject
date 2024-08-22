package com.github.seablue1.treeviewobject.main

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "PersistData", storages = [Storage("PersistData.xml")])
class PersistData : PersistentStateComponent<PersistData?> {
    override fun getState(): PersistData? {
        return this
    }

    override fun loadState(state: PersistData) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: PersistData
            get() = ApplicationManager.getApplication().getService(PersistData::class.java)
    }
}
