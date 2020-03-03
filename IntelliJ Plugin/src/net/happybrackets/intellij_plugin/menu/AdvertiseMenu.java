package net.happybrackets.intellij_plugin.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;
import net.happybrackets.intellij_plugin.controller.network.DeviceConnection;

public class AdvertiseMenu extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            DeviceConnection connection = ControllerEngine.getInstance().getDeviceConnection();

            boolean disabled = connection.getDisabledAdvertise();
            connection.setDisableAdvertise(!disabled);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void update(AnActionEvent event) {
        try {

            int advertise_port = ControllerEngine.getInstance().getDeviceConnection().getReplyPort();

            DeviceConnection connection = ControllerEngine.getInstance().getDeviceConnection();
            boolean disabled = connection.getDisabledAdvertise();

            String menu_text = "Disable Advertise: Port " + advertise_port;
            String menu_descript = "Stops Communication between Intellij and HB device";

            if (disabled) {
                menu_text = "Enable Advertise: Port " + advertise_port;
                menu_descript = "Resumes communcation between Intellij and HB device";

            }
            event.getPresentation().setText(menu_text);
            event.getPresentation().setDescription(menu_descript);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
