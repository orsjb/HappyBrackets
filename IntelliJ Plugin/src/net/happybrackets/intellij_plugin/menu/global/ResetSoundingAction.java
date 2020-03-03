package net.happybrackets.intellij_plugin.menu.global;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;

/**
 * "Reset all devices to their initial state except for audio that is currently playing.
 */
public class ResetSoundingAction extends AnAction
{
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        try {
            ControllerEngine.getInstance().getDeviceConnection().deviceResetSounding();
        }catch (Exception ex){}
    }
}
