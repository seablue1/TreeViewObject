package com.github.seablue1.treeviewobject.main

class MyNode {
    var name: String? = null
    var className: String? = null
    var filePath: String? = null
    var children: MutableList<MyNode?>? = null
    var isRecursive: Boolean = false
    var note: String? = null

    constructor()

    override fun toString(): String {
        return name!!
    }

    constructor(name: String?, className: String?, filePath: String?, children: MutableList<MyNode?>?, note: String?) {
        this.name = name
        this.className = className
        this.filePath = filePath
        this.children = children
        this.note = note
    }


    companion object {
        var spaces: String = "    "
    }
}
