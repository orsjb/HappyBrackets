package net.happybrackets.intellij_plugin.controller.gui.device;

import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import net.happybrackets.intellij_plugin.controller.network.LocalDeviceRepresentation;

/**
 * Creates popup menus for Network Menus
 */
public class DeviceNameMenu extends DeviceMenu {

    // define the username to use for SSH Command
    final String DEF_USERNAME = "pi";
    private String username = DEF_USERNAME;

    //in case the user is not using pi as the default username for ssh
    public void setUsername(String val)
    {
        this.username = val;
    }
    private String buildSSHCommand(String device_name)
    {
        return "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no " + username + "@" + device_name;
    }

    /**
     * Constructor
     * @param item the localDevice we want to send message to
     */
    public DeviceNameMenu(LocalDeviceRepresentation item){
        super(item);
    }



    /**
     * Return an array of MenuItems to Display the Sound menus.
     * These can be used to populate a menu later
     * @return an Array Of MenuItems with actions already attached
     */
    @Override
    public MenuItem[] getMenuItems(){

        // Get IP Address
        MenuItem copy_ip_address_menu = new MenuItem("Copy " + localDeviceRepresentation.getAddress() + " to clipboard");
        copy_ip_address_menu.setOnAction(event -> {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(localDeviceRepresentation.getAddress());
            clipboard.setContent(content);
        });

        // Get the SSH Command
        MenuItem copy_ssh_command_menu = new MenuItem("Copy SSH " + localDeviceRepresentation.getAddress() + " to clipboard");
        copy_ssh_command_menu.setOnAction(event -> {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(buildSSHCommand(localDeviceRepresentation.getAddress()));
            clipboard.setContent(content);
        });

        MenuItem copy_host_command_menu = new MenuItem("Copy " + localDeviceRepresentation.deviceName + " to clipboard");
        copy_host_command_menu.setOnAction(event -> {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(localDeviceRepresentation.deviceName);
            clipboard.setContent(content);
        });


        return new MenuItem[]{copy_ip_address_menu, copy_ssh_command_menu,
                copy_host_command_menu};
    }
}
