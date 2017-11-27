package net.happybrackets.intellij_plugin.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.happybrackets.controller.ControllerEngine;

public class RefreshDevicesMenu extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        ControllerEngine.getInstance().getDeviceConnection().rescanDevices();
    }
}
