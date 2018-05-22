package net.happybrackets.intellij_plugin.menu.device;

import javafx.scene.control.MenuItem;
import net.happybrackets.controller.network.LocalDeviceRepresentation;

public abstract class DeviceMenu {

    protected LocalDeviceRepresentation localDeviceRepresentation;

    /**
     * Constructor that assigns the LocalDeviceRepresentation
     * @param item the localDevice we want to send message to
     */
    public DeviceMenu(LocalDeviceRepresentation item){
        localDeviceRepresentation = item;
    }

    /**
     * Get all the MenuItems we want displayed for this Item contex
     * @return
     */
    public abstract MenuItem[] getMenuItems();
}
