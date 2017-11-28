package net.happybrackets.intellij_plugin.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.happybrackets.controller.ControllerEngine;
import net.happybrackets.controller.network.DeviceConnection;

public class RefreshDevicesMenu extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        DeviceConnection connection = ControllerEngine.getInstance().getDeviceConnection();
        connection.rescanDevices();
        connection.setDisableAdvertise(false);
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabledAndVisible(true);
    }

}
