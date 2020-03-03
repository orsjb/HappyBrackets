package net.happybrackets.intellij_plugin.menu.global;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;

import javax.swing.*;

/**
 * Menu item to shutdown all devices
 */
public class ShutdownAction extends AnAction
{
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        try {
            int dialog_button = JOptionPane.YES_NO_OPTION;
            int dialog_result = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to shutdown all the devices?", "Shutdown All Devices", dialog_button);

            if (dialog_result == JOptionPane.YES_OPTION) {
                ControllerEngine.getInstance().getDeviceConnection().deviceShutdown();
            }
        }catch (Exception ex){}
    }
}
