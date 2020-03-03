package net.happybrackets.intellij_plugin.controller.gui.device;

import javafx.scene.control.MenuItem;
import net.happybrackets.intellij_plugin.controller.network.LocalDeviceRepresentation;

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
     * @return Array of menu Items
     */
    public abstract MenuItem[] getMenuItems();
}
