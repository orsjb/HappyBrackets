package net.happybrackets.intellij_plugin.controller.gui.device;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.jgoodies.common.base.Strings;
import javafx.application.Platform;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import net.happybrackets.intellij_plugin.controller.network.LocalDeviceRepresentation;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static net.happybrackets.intellij_plugin.NotificationMessage.displayNotification;
import static net.happybrackets.intellij_plugin.templates.project.HappyBracketsProject.DEVICE_CONFIG_PATH;

/**
 * Creates popup menus for Network Menus
 */
public class PingMenu extends DeviceMenu {

    // define the username to use for SSH Command
    final String DEF_USERNAME = "pi";
    private String username = DEF_USERNAME;
    private String projectDir; // the project directory

    /**
     * Constructor
     *
     * @param item        the localDevice we want to send message to
     * @param project_dir the project directory
     */
    public PingMenu(LocalDeviceRepresentation item, String project_dir) {
        super(item);
        projectDir = project_dir;
    }

    //in case the user is not using pi as the default username for ssh
    public void setUsername(String val) {
        this.username = val;
    }

    private String buildSSHCommand(String device_name) {
        return "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no " + username + "@" + device_name;
    }

    /**
     * Return an array of MenuItems to Display the Sound menus.
     * These can be used to populate a menu later
     *
     * @return an Array Of MenuItems with actions already attached
     */
    @Override
    public MenuItem[] getMenuItems() {

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

        MenuItem request_status_menu = new MenuItem("Request status");
        request_status_menu.setDisable(localDeviceRepresentation.isIgnoringDevice());
        request_status_menu.setOnAction(event -> localDeviceRepresentation.sendStatusRequest());

        MenuItem cancel_send_menu = new MenuItem("Cancel Send");
        cancel_send_menu.setDisable(!localDeviceRepresentation.getFileIsSending());
        cancel_send_menu.setOnAction(event -> localDeviceRepresentation.cancelSendFile());


        MenuItem request_version_menu = new MenuItem("Request Version");
        request_version_menu.setDisable(localDeviceRepresentation.isIgnoringDevice());
        request_version_menu.setOnAction(event -> localDeviceRepresentation.sendVersionRequest());


        MenuItem remove_item_menu = new MenuItem("Remove " + localDeviceRepresentation.deviceName);
        remove_item_menu.setOnAction(event -> localDeviceRepresentation.removeDevice());


        CheckMenuItem ignore_controls_item_menu = new CheckMenuItem("Ignore this Device");
        ignore_controls_item_menu.setSelected(localDeviceRepresentation.isIgnoringDevice());
        ignore_controls_item_menu.setOnAction(event -> {
            localDeviceRepresentation.setIgnoreDevice(!localDeviceRepresentation.isIgnoringDevice());
            Platform.runLater(() -> {

                ignore_controls_item_menu.setSelected(localDeviceRepresentation.isIgnoringDevice());
            });

        });

        CheckMenuItem favourite_item_menu = new CheckMenuItem("Favourite");
        favourite_item_menu.setSelected(localDeviceRepresentation.isFavouriteDevice());
        favourite_item_menu.setOnAction(event -> {

            localDeviceRepresentation.setFavouriteDevice(!localDeviceRepresentation.isFavouriteDevice());
            Platform.runLater(() -> {
                favourite_item_menu.setSelected(localDeviceRepresentation.isFavouriteDevice());
            });
        });

        CheckMenuItem encrypt_item_menu = new CheckMenuItem("Encrypt Classes");
        encrypt_item_menu.setSelected(localDeviceRepresentation.isEncryptionEnabled());
        encrypt_item_menu.setOnAction(event -> {

            localDeviceRepresentation.setEncryptionEnabled(!localDeviceRepresentation.isEncryptionEnabled());
            Platform.runLater(() -> {
                encrypt_item_menu.setSelected(localDeviceRepresentation.isFavouriteDevice());
            });
        });

        MenuItem reboot_menu = new MenuItem("Reboot Device");
        reboot_menu.setDisable(localDeviceRepresentation.isIgnoringDevice());
        reboot_menu.setOnAction(event -> new Thread(() -> {
            try {

                int dialog_button = JOptionPane.YES_NO_OPTION;
                int dialog_result = JOptionPane.showConfirmDialog(null,
                        "Are you sure you want to reboot " + localDeviceRepresentation.getFriendlyName() + "?", "Rebooting " + localDeviceRepresentation.getFriendlyName(), dialog_button);

                if (dialog_result == JOptionPane.YES_OPTION) {
                    localDeviceRepresentation.rebootDevice();
                }
            } catch (Exception ex) {
            }
        }).start());

        // we are going to set Device to our Time
        MenuItem synchronise_item = new MenuItem("Synchronise Device");
        synchronise_item.setDisable(localDeviceRepresentation.isIgnoringDevice());
        synchronise_item.setOnAction(event -> new Thread(() -> {
            try {
                localDeviceRepresentation.synchroniseDevice();
            } catch (Exception ex) {
            }
        }).start());

        // we are going to create a config file
        String config_filename = localDeviceRepresentation.deviceName + ".config";

        MenuItem write_config_item = new MenuItem("Create " + config_filename);

        if (Strings.isEmpty(projectDir)) {
            write_config_item.setDisable(true);
        } else {
            String filename = projectDir + File.separatorChar + DEVICE_CONFIG_PATH + config_filename;
            File f = new File(filename);

            write_config_item.setDisable(f.exists());

        }

        write_config_item.setOnAction(event -> new Thread(() -> {
            try {
                String filename = projectDir + File.separatorChar + DEVICE_CONFIG_PATH + config_filename;

                Files.write(Paths.get(filename), DefaultConfig.getDefaultConfigString().getBytes());
                LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(filename));
                displayNotification("Wrote " + filename, NotificationType.INFORMATION);
            } catch (Exception ex) {
            }
        }).start());

        // Create Menu for displaying config on device
        MenuItem get_config_item = new MenuItem("Show Device Config ");
        get_config_item.setOnAction(event -> new Thread(() -> {
            try {
                if (localDeviceRepresentation.sendPrintConfig()) {
                    displayNotification("Request config  from " + localDeviceRepresentation.deviceName, NotificationType.INFORMATION);
                } else {
                    displayNotification("Unable to connect to " + localDeviceRepresentation.deviceName, NotificationType.ERROR);
                }

            } catch (Exception ex) {
            }
        }).start());

        MenuItem clear_config_menu = new MenuItem("Remove Config from Device");
        clear_config_menu.setDisable(localDeviceRepresentation.isIgnoringDevice());
        clear_config_menu.setOnAction(event -> new Thread(() -> {
            try {

                int dialog_button = JOptionPane.YES_NO_OPTION;
                int dialog_result = JOptionPane.showConfirmDialog(null,
                        "This will remove all files from data/classes data/jars and config folders on " + localDeviceRepresentation.getFriendlyName() + ". Do you really want to do this?", "Reset Device Configuration" + localDeviceRepresentation.getFriendlyName(), dialog_button);

                if (dialog_result == JOptionPane.YES_OPTION) {
                    localDeviceRepresentation.sendResetConfiguration();
                }
            } catch (Exception ex) {
            }
        }).start());

        MenuItem shutdown_menu = new MenuItem("Shutdown Device");
        shutdown_menu.setDisable(localDeviceRepresentation.isIgnoringDevice());
        shutdown_menu.setOnAction(event -> new Thread(() -> {
            try {

                int dialog_button = JOptionPane.YES_NO_OPTION;
                int dialog_result = JOptionPane.showConfirmDialog(null,
                        "Are you sure you want to shutdown " + localDeviceRepresentation.getFriendlyName() + "?", "Shutting Down " + localDeviceRepresentation.getFriendlyName(), dialog_button);

                if (dialog_result == JOptionPane.YES_OPTION) {
                    localDeviceRepresentation.shutdownDevice();
                }
            } catch (Exception ex) {
            }
        }).start());


        return new MenuItem[]{copy_ip_address_menu, copy_ssh_command_menu,
                copy_host_command_menu, new SeparatorMenuItem(),
                request_status_menu, cancel_send_menu, request_version_menu,
                new SeparatorMenuItem(),
                get_config_item,
                write_config_item,
                clear_config_menu,
                new SeparatorMenuItem(),
                remove_item_menu, ignore_controls_item_menu, favourite_item_menu,
                new SeparatorMenuItem(), encrypt_item_menu,
                synchronise_item, reboot_menu, shutdown_menu};
    }
}
