package net.happybrackets.intellij_plugin;

import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import javafx.application.Platform;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class ConfigurationScreenModel {
    /// The configuration editor sections that are available.
    enum ConfigurationEditorType {
        /// The "Configuration" editor section
        config,

        // The "Known devices" configuration editor section.
        devices
    }

    private ControllerEngine controllerEngine;
    private String locationHash;

    public ConfigurationScreenModel(ControllerEngine controllerEngine, Project project) {
        this.controllerEngine = controllerEngine;
        locationHash = project.getLocationHash();
    }

    public String getInitialConfig(ConfigurationEditorType configurationEditorType) {
        switch(configurationEditorType) {
            case config:
                return controllerEngine.getCurrentConfigString();
            case devices:
                StringBuilder map = new StringBuilder();
                controllerEngine.getDeviceConnection().getKnownDevices().forEach((hostname, id) -> map.append(id.getSaveLine() + "\n"));
                return map.toString();

        }
        return "";
    }

    /// Saves a configuration to disk, prompting the user where to save it.
    public void save(String textToSave, PopupPresenter popupPresenter, ConfigurationEditorType configurationEditorType) {
        switch(configurationEditorType) {
            case config:
                saveFile("Configuration",
                        "controllerConfigPath",
                        "controller-config.json",
                        textToSave,
                        popupPresenter);
                return;
            case devices:
                saveFile("Known Devices",
                        "knownDevicesPath",
                        "known_devices",
                        textToSave,
                        popupPresenter);
                return;
        }
    }

    /// Apply the configuration to the running HappyBrackets system.
    public void apply(String textToSave, ConfigurationEditorType configurationEditorType) {
        switch(configurationEditorType) {
            case config:
                HappyBracketsToolWindow.setConfig(textToSave, null);
                return;
            case devices:
                ControllerEngine.getInstance().getDeviceConnection().setKnownDevices(textToSave.split("\\r?\\n"));
                return;
        }
    }

    /// Loads the configuration from a files, prompting the user to choose which file to load from.
    public String load(ConfigurationEditorType configurationEditorType, PopupPresenter popupPresenter) {
        switch(configurationEditorType) {
            case config:
                return load(popupPresenter, "Configuration", "controllerConfigPath");
            case devices:
                return load(popupPresenter, "Known Devices", "knownDevicesPath");
        }
        return null;
    }

    private String load(PopupPresenter popupPresenter, String label, String setting) {
        ControllerEngine controlEngine = ControllerEngine.getInstance();

        // Select a file
        final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor().withShowHiddenFiles(true);
        descriptor.setTitle("Select " + label.toLowerCase() + " file");

        String currentFile = controlEngine.getSettings().getString(setting);
        VirtualFile vfile = currentFile == null ? null : LocalFileSystem.getInstance().findFileByPath(currentFile.replace(File.separatorChar, '/'));

        VirtualFile[] virtualFile = FileChooser.chooseFiles(descriptor, null, vfile);
        if (virtualFile == null || virtualFile.length == 0 || virtualFile[0] == null) {
            return null;
        }

        String path = virtualFile[0].getCanonicalPath();

        File configFile = new File(path);
        try {
            String configJson = (new Scanner(configFile)).useDelimiter("\\Z").next();
            controlEngine.getSettings().set(setting, configFile.getAbsolutePath());
            popupPresenter.showPopup("Test notification");
            return configJson;
        } catch (FileNotFoundException ex) {
            popupPresenter.showPopup("Error loading " + label.toLowerCase() + ": " + ex.getMessage());
        }
        return null;
    }

    private void saveFile(String label, String settingName, String defaultFileName, String textToSave, PopupPresenter popupPresenter) {
        SwingUtilities.invokeLater(() -> {
            String currentFilePath = controllerEngine.getSettings().getString(settingName);

            // Select a file
            FileSaverDescriptor fsd = new FileSaverDescriptor("Select " + label.toLowerCase() + " file to save to.", "Select " + label.toLowerCase() + " file to save to.");
            fsd.withShowHiddenFiles(true);
            final FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(fsd, getThisProject());

            File currentFile = currentFilePath != null ? new File(currentFilePath) : null;
            VirtualFile base_dir = null;
            String current_name = null;
            if (currentFile != null && currentFile.exists()) {
                base_dir = LocalFileSystem.getInstance().findFileByPath(currentFile.getParentFile().getAbsolutePath().replace(File.separatorChar, '/'));
                current_name = currentFile.getName();
            }
            else {
                base_dir = LocalFileSystem.getInstance().findFileByPath(HappyBracketsToolWindow.getPluginLocation());
                current_name = defaultFileName;
            }
            final VirtualFile base_dir_final = base_dir;

            final VirtualFileWrapper wrapper = dialog.save(base_dir_final, current_name);

            if (wrapper != null) {
                Platform.runLater(() -> {
                    File config_file = wrapper.getFile();

                    try (PrintWriter out = new PrintWriter(config_file.getAbsolutePath())) {
                        out.print(textToSave);

                        controllerEngine.getSettings().set(settingName, config_file.getAbsolutePath());
                    } catch (Exception ex) {
                        popupPresenter.showPopup("Error saving " + label.toLowerCase() + ": " + ex.getMessage());
                    }
                });
            }
        });
    }

    /**
     * Get the current project based by comparing location hash
     * @return The current ptoject, otherwise, null
     */
    Project getThisProject(){
        Project ret = null;

        for (IdeFrame frame : WindowManager.getInstance().getAllProjectFrames()) {
            if (frame.getProject() != null) {
                Project p = frame.getProject();
                if (p.getLocationHash().equalsIgnoreCase(locationHash)){
                    ret = p;
                    break;
                }
            }

        }
        return ret;
    }

    public interface PopupPresenter {
        void showPopup(String text);
    }
}
