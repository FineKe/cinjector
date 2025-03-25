package com.github.fineke.core;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;

public class MyNotification {
    private static final NotificationGroup NOTIFICATION_GROUP = NotificationGroupManager.getInstance().getNotificationGroup("CInjector Notifications");
    public static void showNotification(Project project, String title,String content,boolean ok) {
        Notification notification = NOTIFICATION_GROUP.createNotification(
                title,  // 标题
                content,                   // 内容
                ok? NotificationType.INFORMATION:NotificationType.ERROR  // 通知类型 (INFORMATION, WARNING, ERROR)
        );
        notification.notify(project);  // 显示通知
    }

    public static void showNotificationOk(Project project, String title,String content){
        showNotification(project,title,content,true);
    }

    public static void showNotificationErr(Project project, String title,String content){
        showNotification(project,title,content,false);
    }
}