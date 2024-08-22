package com.github.seablue1.treeviewobject.main

import com.github.seablue1.treeviewobject.main.MyStoreService.MyStore
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import java.util.*

@State(name = "TreeViewObjectSetting", storages = [Storage("TreeViewObjectSetting.xml")])
class MyStoreService : PersistentStateComponent<MyStore?> {
    class MyStore {
        var showNote: Boolean = true
        var deep: Int = 10
        var split: String = "."

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false
            val myStore = o as MyStore
            return showNote == myStore.showNote && deep == myStore.deep && split == myStore.split
        }

        override fun hashCode(): Int {
            return Objects.hash(showNote, deep, split)
        }
    }

    var myStore: MyStore = MyStore()

    override fun getState(): MyStore? {
        return myStore
    }

    override fun loadState(myStore: MyStore) {
        this.myStore = myStore
    }

    companion object {
        fun getMyStore(): MyStore? {
            val service = ApplicationManager.getApplication().getService(MyStoreService::class.java)
            return service.state
        }

        fun setMyStore(store: MyStore?) {
            val service = ApplicationManager.getApplication().getService(MyStoreService::class.java)
            service.loadState(store!!)
        }
    }
}
