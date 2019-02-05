package net.happybrackets.intellij_plugin.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.happybrackets.controller.ControllerEngine;
import net.happybrackets.controller.network.ControllerAdvertiser;
import net.happybrackets.controller.network.DeviceConnection;

public class BroadcastEnableMenu extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        try {

            ControllerAdvertiser advertiser = ControllerEngine.getInstance().getControllerAdvertiser();

            if (advertiser != null) {
                boolean multicast_only = advertiser.isOnlyMulticastMessages();

                advertiser.setOnlyMulticastMessages(!multicast_only);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void update(AnActionEvent event) {
        try {
            ControllerAdvertiser advertiser = ControllerEngine.getInstance().getControllerAdvertiser();

            if (advertiser != null) {
                boolean multicast_only = advertiser.isOnlyMulticastMessages();

                String menu_text = "Multicast only";
                if (multicast_only) {
                    menu_text = "Multicast and Broadcast";

                }
                event.getPresentation().setText(menu_text);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
