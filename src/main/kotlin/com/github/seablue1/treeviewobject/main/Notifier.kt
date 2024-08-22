package com.github.seablue1.treeviewobject.main

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object Notifier {
    val aaa: String
        get() = "aaa"

    fun notifyError(content: String?, project: Project?) {
        notify(content, NotificationType.ERROR, project)
    }

    fun notifyWarn(content: String?, project: Project?) {
        notify(content, NotificationType.WARNING, project)
    }

    fun notifyInfo(content: String?, project: Project?) {
        notify(content, NotificationType.INFORMATION, project)
    }

    fun notify(content: String?, type: NotificationType?, project: Project?) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("ink.organics.pojo2json.NotificationGroup")
            .createNotification(content!!, type!!)
            .notify(project)
    }
}
