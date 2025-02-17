package com.github.fineke.core;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

public class MyNotification {
    private static final NotificationGroup NOTIFICATION_GROUP = NotificationGroup.balloonGroup("CInjector Notifications");

    public static void showNotification(Project project, String title,String content) {
        Notification notification = NOTIFICATION_GROUP.createNotification(
                title,  // 标题
                content,                   // 内容
                NotificationType.INFORMATION,  // 通知类型 (INFORMATION, WARNING, ERROR)
                null                      // 可选的操作
        );
        notification.notify(project);  // 显示通知
    }
}