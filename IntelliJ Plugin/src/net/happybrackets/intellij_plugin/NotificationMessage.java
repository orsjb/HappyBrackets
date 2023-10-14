package net.happybrackets.intellij_plugin;

import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

public class NotificationMessage {

    public static final NotificationGroup GROUP_DISPLAY_ID_INFO =
            new NotificationGroup("My notification group",
                    NotificationDisplayType.BALLOON, true);

    public static void displayNotification(String message, NotificationType notificationType) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                Notification notification = GROUP_DISPLAY_ID_INFO.createNotification(message, notificationType);
                Project[] projects = ProjectManager.getInstance().getOpenProjects();
                Notifications.Bus.notify(notification, projects[0]);
            }
        });
    }
}
