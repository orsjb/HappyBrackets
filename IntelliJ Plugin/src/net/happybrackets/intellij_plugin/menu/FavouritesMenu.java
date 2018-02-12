package net.happybrackets.intellij_plugin.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.happybrackets.controller.ControllerEngine;
import net.happybrackets.controller.network.DeviceConnection;

public class FavouritesMenu extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {

        DeviceConnection connection = ControllerEngine.getInstance().getDeviceConnection();
        boolean showing_favourites = connection.isShowOnlyFavourites();
        connection.setShowOnlyFavourites(!showing_favourites);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void update(AnActionEvent event) {
        try {

            DeviceConnection connection = ControllerEngine.getInstance().getDeviceConnection();
            boolean disabled = connection.getDisabledAdvertise();

            boolean showing_favourites = connection.isShowOnlyFavourites();
            String menu_text = "Show Only Favourites";
            if (showing_favourites) {
                menu_text = "Show All Devices";

            }
            event.getPresentation().setText(menu_text);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
