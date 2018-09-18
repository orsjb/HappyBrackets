package net.happybrackets.intellij_plugin.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.happybrackets.controller.ControllerEngine;
import net.happybrackets.controller.network.DeviceConnection;

public class RefreshDevicesMenu extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            // first clean out broadcaster
            
            //ControllerEngine.getInstance().getBroadcastManager().dispose();
            DeviceConnection connection = ControllerEngine.getInstance().getDeviceConnection();
            connection.rescanDevices();
            connection.setDisableAdvertise(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void update(AnActionEvent event) {
        try {
            event.getPresentation().setEnabledAndVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
