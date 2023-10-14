package net.happybrackets.intellij_plugin.controller.gui.device;

import javafx.scene.control.MenuItem;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.intellij_plugin.controller.network.LocalDeviceRepresentation;

/**
 * Creates popup menus for Reset, ResetSounding, ClearSound
 */
public class SoundMenu extends DeviceMenu {

    /**
     * Constructor
     *
     * @param item the localDevice we want to send message to
     */
    public SoundMenu(LocalDeviceRepresentation item) {
        super(item);

    }


    /**
     * Return an array of MenuItems to Display the Sound menus.
     * These can be used to populate a menu later
     *
     * @return an Array Of MenuItems with actions already attached
     */
    @Override
    public MenuItem[] getMenuItems() {

        // Create our reset menu localDeviceRepresentation
        MenuItem reset_menu_item = new MenuItem("Reset");
        reset_menu_item.setOnAction(event -> localDeviceRepresentation.resetDevice());

        // Create our reset sounding menu localDeviceRepresentation
        MenuItem reset_sounding_menu_item = new MenuItem("Reset Sounding");
        reset_sounding_menu_item.setOnAction(event -> localDeviceRepresentation.send(OSCVocabulary.Device.RESET_SOUNDING));

        // Create our clear sound menu localDeviceRepresentation
        MenuItem clear_sound_menu_item = new MenuItem("Clear Sound");
        clear_sound_menu_item.setOnAction(event -> localDeviceRepresentation.send(OSCVocabulary.Device.CLEAR_SOUND));


        return new MenuItem[]{reset_menu_item, reset_sounding_menu_item, clear_sound_menu_item};
    }
}
