package net.happybrackets.intellij_plugin.menu.global;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;

import javax.swing.*;

/**
 * Menu action to reboot all devices
 */
public class RebootAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        try {
            int dialog_button = JOptionPane.YES_NO_OPTION;
            int dialog_result = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to reboot all the devices?", "Rebooting All Devices", dialog_button);

            if (dialog_result == JOptionPane.YES_OPTION) {
                ControllerEngine.getInstance().getDeviceConnection().deviceReboot();
            }
        } catch (Exception ex) {
        }

    }
}
