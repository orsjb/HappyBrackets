package net.happybrackets.intellij_plugin.menu.global;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;

/**
 * Reset all devices to their initial state (same as Reset Sounding + Clear Sound).
 */
public class ResetAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        try {
            ControllerEngine.getInstance().getDeviceConnection().deviceReset();
        } catch (Exception ex) {
        }
    }
}
