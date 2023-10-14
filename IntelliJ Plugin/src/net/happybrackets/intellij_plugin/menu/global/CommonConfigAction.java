package net.happybrackets.intellij_plugin.menu.global;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import net.happybrackets.intellij_plugin.controller.gui.device.DefaultConfig;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static net.happybrackets.device.config.ConfigFiles.COMMON_CONFIG;
import static net.happybrackets.device.config.ConfigFiles.CONFIG_PATH;
import static net.happybrackets.intellij_plugin.NotificationMessage.displayNotification;
import static net.happybrackets.intellij_plugin.templates.project.HappyBracketsProject.HAPPY_BRACKETS_DEVICE_FOLDER;

/**
 * Menu action to reboot all devices
 */
public class CommonConfigAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        try {
            Project current_project = anActionEvent.getProject();
            String root_path = current_project.getBasePath();
            String common_config = root_path + HAPPY_BRACKETS_DEVICE_FOLDER + CONFIG_PATH + COMMON_CONFIG;
            Files.write(Paths.get(common_config), DefaultConfig.getDefaultConfigString().getBytes());
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(common_config));
            displayNotification("Wrote " + common_config, NotificationType.INFORMATION);
        } catch (Exception ex) {
        }

    }

    @Override
    public void update(AnActionEvent anActionEvent) {

        try {
            if (anActionEvent != null) {
                boolean enable = false;

                Project current_project = anActionEvent.getProject();

                String root_path = current_project.getBasePath();

                String common_config = root_path + HAPPY_BRACKETS_DEVICE_FOLDER + CONFIG_PATH + COMMON_CONFIG;

                if (Files.exists(Paths.get(root_path + HAPPY_BRACKETS_DEVICE_FOLDER + CONFIG_PATH))) {
                    enable = !Files.exists(Paths.get(common_config));
                }

                anActionEvent.getPresentation().setEnabled(enable);
            }
        } catch (Exception ex) {
            if (anActionEvent != null) {
                try {
                    anActionEvent.getPresentation().setEnabled(false);
                } catch (Exception ex2) {
                }
            }
        }

    }
}
