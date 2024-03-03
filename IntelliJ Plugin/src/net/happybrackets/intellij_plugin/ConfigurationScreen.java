package net.happybrackets.intellij_plugin;

import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.SwingUtilities;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class ConfigurationScreen {

    final static Logger logger = LoggerFactory.getLogger(ConfigurationScreen.class);
    private static final int DEFAULT_ELEMENT_SPACING = 10;
    private static final int MIN_TEXT_AREA_HEIGHT = 200;
    private Button[] configApplyButton = new Button[2]; // 0 = overall config, 1 = known devices.
    private Stage displayStage = new Stage();
    private String locationHash;

    public ConfigurationScreen(Project project) {
        locationHash = project.getLocationHash();
        displayStage.setTitle("HappyBrackets Settings");
        TitledPane config_pane = new TitledPane("Configuration", makeConfigurationPane(0));
        TitledPane known_devices_pane = new TitledPane("Known Devices", makeConfigurationPane(1));
        VBox main_container = new VBox(5);
        main_container.setFillWidth(true);
        main_container.getChildren().addAll(config_pane, known_devices_pane);
        ScrollPane main_scroll = new ScrollPane();
        main_scroll.setFitToWidth(true);
        main_scroll.setFitToHeight(true);
        main_scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        main_scroll.setStyle("-fx-font-family: sample; -fx-font-size: 12;");
        main_scroll.setMinHeight(100);
        main_scroll.setContent(main_container);

        displayStage.setScene(new Scene(main_scroll));
    }

    /**
     * Get the current project based by comparing location hash
     *
     * @return The current ptoject, otherwise, null
     */
    Project getThisProject() {
        Project ret = null;

        for (IdeFrame frame : WindowManager.getInstance().getAllProjectFrames()) {
            if (frame.getProject() != null) {
                Project p = frame.getProject();
                if (p.getLocationHash().equalsIgnoreCase(locationHash)) {
                    ret = p;
                    break;
                }
            }

        }
        return ret;
    }

    public void show() {
        displayStage.show();
    }

    /**
     * Make Configuration/Known devices pane.
     *
     * @param file_type 0 == configuration, 1 == known devices.
     */
    private Pane makeConfigurationPane(final int file_type) {

        ControllerEngine control_engine = ControllerEngine.getInstance();
        final TextArea config_field = new TextArea();
        final String label = file_type == 0 ? "Configuration" : "Known Devices";
        final String setting = file_type == 0 ? "controllerConfigPath" : "knownDevicesPath";

        config_field.setMinHeight(MIN_TEXT_AREA_HEIGHT);
        // Load initial config into text field.
        if (file_type == 0) {
            config_field.setText(control_engine.getCurrentConfigString());
        } else {
            StringBuilder map = new StringBuilder();
            control_engine.getDeviceConnection().getKnownDevices().forEach((hostname, id) -> map.append(id.getSaveLine() + "\n"));

            config_field.setText(map.toString());
        }
        config_field.textProperty().addListener((observable, oldValue, newValue) -> {
            configApplyButton[file_type].setDisable(false);
        });

        Button load_button = new Button("Load");
        load_button.setTooltip(new Tooltip("Load a new " + label.toLowerCase() + " file."));
        load_button.setOnMouseClicked(event -> {
            //select a file
            final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor().withShowHiddenFiles(true);
            descriptor.setTitle("Select " + label.toLowerCase() + " file");

            String currentFile = control_engine.getSettings().getString(setting);
            VirtualFile vfile = currentFile == null ? null : LocalFileSystem.getInstance().findFileByPath(currentFile.replace(File.separatorChar, '/'));

            displayStage.hide();

            //needs to run in Swing event dispatch thread, and then back again to JFX thread!!
            SwingUtilities.invokeLater(() -> {

                VirtualFile[] virtual_file = FileChooser.chooseFiles(descriptor, null, vfile);
                if (virtual_file != null && virtual_file.length > 0 && virtual_file[0] != null) {
                    Platform.runLater(() -> {
                        loadConfigFile(virtual_file[0].getCanonicalPath(), label, config_field, setting, load_button, event);
                        displayStage.show();
                    });
                } else {
                    Platform.runLater(() -> {
                        displayStage.show();
                    });
                }
            });
        });

        Button save_button = new Button("Save");
        save_button.setTooltip(new Tooltip("Save these " + label.toLowerCase() + " settings to a file."));
        save_button.setOnMouseClicked(event -> {

            displayStage.hide();
            //needs to run in Swing event dispatch thread, and then back again to JFX thread!!
            SwingUtilities.invokeLater(() -> {

                //select a file
                FileSaverDescriptor fsd = new FileSaverDescriptor("Select " + label.toLowerCase() + " file to save to.", "Select " + label.toLowerCase() + " file to save to.");
                fsd.withShowHiddenFiles(true);
                final FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(fsd, getThisProject());

                String current_file_path = control_engine.getSettings().getString(setting);
                File currentFile = current_file_path != null ? new File(control_engine.getSettings().getString(setting)) : null;
                VirtualFile base_dir = null;
                String current_name = null;
                if (currentFile != null && currentFile.exists()) {
                    base_dir = LocalFileSystem.getInstance().findFileByPath(currentFile.getParentFile().getAbsolutePath().replace(File.separatorChar, '/'));
                    current_name = currentFile.getName();
                } else {
                    base_dir = LocalFileSystem.getInstance().findFileByPath(HappyBracketsToolWindow.getPluginLocation());
                    current_name = file_type == 0 ? "controller-config.json" : "known_devices";
                }
                final VirtualFile base_dir_final = base_dir;
                final String current_name_final = current_name;

                final VirtualFileWrapper wrapper = dialog.save(base_dir_final, current_name_final);

                if (wrapper != null) {
                    Platform.runLater(() -> {
                        File config_file = wrapper.getFile();

                        try (PrintWriter out = new PrintWriter(config_file.getAbsolutePath())) {
                            out.print(config_field.getText());

                            control_engine.getSettings().set(setting, config_file.getAbsolutePath());
                        } catch (Exception ex) {
                            showPopup("Error saving " + label.toLowerCase() + ": " + ex.getMessage(), save_button, 5, event);
                        }

                        displayStage.show();
                    });
                } else {
                    Platform.runLater(() -> {
                        displayStage.show();
                    });
                }
            });
        });

        configApplyButton[file_type] = new Button("Apply");
        configApplyButton[file_type].setTooltip(new Tooltip("Apply these " + label.toLowerCase() + " settings."));
        configApplyButton[file_type].setDisable(true);
        configApplyButton[file_type].setOnMouseClicked(event -> {
            configApplyButton[file_type].setDisable(true);

            if (file_type == 0) {
                applyConfig(config_field.getText());
            } else {
                applyKnownDevices(config_field.getText());
            }
        });

        FlowPane buttons = new FlowPane(DEFAULT_ELEMENT_SPACING, DEFAULT_ELEMENT_SPACING);
        buttons.setAlignment(Pos.TOP_LEFT);
        buttons.getChildren().addAll(load_button, save_button, configApplyButton[file_type]);


        VBox config_pane = new VBox(DEFAULT_ELEMENT_SPACING);
        config_pane.setAlignment(Pos.TOP_LEFT);
        config_pane.getChildren().addAll(makeTitle(label), config_field, buttons);

        return config_pane;
    }

    private void loadConfigFile(String path, String label, TextArea config_field, String setting, Node triggering_element, MouseEvent event) {
        File config_file = new File(path);
        try {
            String config_JSON = (new Scanner(config_file)).useDelimiter("\\Z").next();
            config_field.setText(config_JSON);
            ControllerEngine.getInstance().getSettings().set(setting, config_file.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            showPopup("Error loading " + label.toLowerCase() + ": " + ex.getMessage(), triggering_element, 5, event);
        }
    }

    private void showPopup(String message, Node element, int timeout, MouseEvent event) {
        showPopup(message, element, timeout, event.getScreenX(), event.getScreenY());
    }

    private void showPopup(String message, Node element, int timeout, double x, double y) {
        Text t = new Text(message);

        VBox pane = new VBox();
        pane.setPadding(new Insets(10));
        pane.getChildren().add(t);

        Popup p = new Popup();
        p.getScene().setFill(Color.ORANGE);
        p.getContent().add(pane);
        p.show(element, x, y);
        p.setAutoHide(true);

        if (timeout >= 0) {
            PauseTransition pause = new PauseTransition(Duration.seconds(timeout));
            pause.setOnFinished(e -> p.hide());
            pause.play();
        }
    }

    private void applyKnownDevices(String kd) {
        ControllerEngine.getInstance().getDeviceConnection().setKnownDevices(kd.split("\\r?\\n"));
    }

    private void applyConfig(String config) {
        HappyBracketsToolWindow.setConfig(config, null);
    }

    private Text makeTitle(String title) {
        Text text = new Text(title);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setTextOrigin(VPos.CENTER);
        text.setStyle("-fx-font-weight: bold;");
        return text;
    }
}
