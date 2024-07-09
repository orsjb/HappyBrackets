package net.happybrackets.intellij_plugin.menu;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.happybrackets.intellij_plugin.NotificationMessage;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;

public class ProbeDevicesMenu extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            ControllerEngine.getInstance().doProbe();
            NotificationMessage.displayNotification("Probed Network for devices", NotificationType.INFORMATION);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
