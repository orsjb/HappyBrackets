package net.happybrackets.intellij_plugin.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.happybrackets.controller.ControllerEngine;
import net.happybrackets.controller.network.DeviceConnection;

public class FavouritesMenu extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        DeviceConnection connection = ControllerEngine.getInstance().getDeviceConnection();
        boolean showing_favourites = connection.isShowOnlyFavourites();
        connection.setShowOnlyFavourites(!showing_favourites);
    }

    @Override
    public void update(AnActionEvent event) {
        DeviceConnection connection = ControllerEngine.getInstance().getDeviceConnection();
        boolean disabled = connection.getDisabledAdvertise();

        boolean showing_favourites = connection.isShowOnlyFavourites();
        String menu_text = "Show Only Favourites";
        if (showing_favourites)
        {
            menu_text = "Show All Devices";

        }
        event.getPresentation().setText(menu_text);
    }
}
