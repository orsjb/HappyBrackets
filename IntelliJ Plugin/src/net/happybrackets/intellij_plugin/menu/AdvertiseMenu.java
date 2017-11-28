package net.happybrackets.intellij_plugin.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.happybrackets.controller.ControllerEngine;
import net.happybrackets.controller.network.DeviceConnection;

public class AdvertiseMenu extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        DeviceConnection connection = ControllerEngine.getInstance().getDeviceConnection();
        boolean disabled = connection.getDisabledAdvertise();
        connection.setDisableAdvertise(!disabled);
    }

    @Override
    public void update(AnActionEvent event) {
        DeviceConnection connection = ControllerEngine.getInstance().getDeviceConnection();
        boolean disabled = connection.getDisabledAdvertise();

        String menu_text = "Disable Advertise";
        String menu_descript = "Stops Communication between Intellij and HB device";

        if (disabled)
        {
            menu_text = "Enable Advertise";
            menu_descript = "Resumes communcation between Intellij and HB device";

        }
        event.getPresentation().setText(menu_text);
        event.getPresentation().setDescription(menu_descript);
    }
}