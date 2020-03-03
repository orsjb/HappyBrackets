package net.happybrackets.intellij_plugin.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;
import net.happybrackets.intellij_plugin.controller.network.DeviceConnection;

public class RefreshDevicesMenu extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            // first clean out broadcaster
            
            //ControllerEngine.getInstance().getBroadcastManager().dispose();
            DeviceConnection connection = ControllerEngine.getInstance().getDeviceConnection();
            ControllerEngine.getInstance().startDeviceCommunication();
            // we will make sure we do not have advertising disabled
            connection.setDisableAdvertise(false);

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
