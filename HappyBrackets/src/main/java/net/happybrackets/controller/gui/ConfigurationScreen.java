package net.happybrackets.controller.gui;


import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationScreen {

    private static final int DEFAULT_ELEMENT_SPACING = 10;
    private static final int MIN_TEXT_AREA_HEIGHT = 200;

    private Stage DisplayStage = new Stage();

    final static Logger logger = LoggerFactory.getLogger(ConfigurationScreen.class);

    public ConfigurationScreen()
    {
        DisplayStage.setTitle("HappyBrackets Settings");
        TitledPane config_pane = new TitledPane("Configuration", makeConfigurationPane(0));
    }

    /**
     * Make Configuration/Known devices pane.
     * @param file_type 0 == configuration, 1 == known devices.
     */
    private Pane makeConfigurationPane(final int file_type) {
        /*
        final TextArea config_field = new TextArea();
        final String label = file_type == 0 ? "Configuration" : "Known Devices";
        final String setting = file_type == 0 ? "controllerConfigPath" : "knownDevicesPath";

        //configField.setPrefSize(400, 250);
        config_field.setMinHeight(MIN_TEXT_AREA_HEIGHT);
        // Load initial config into text field.
        if (file_type == 0) {
            config_field.setText(HappyBracketsToolWindow.getCurrentConfigString());
        }
        else {
            StringBuilder map = new StringBuilder();
            deviceConnection.getKnownDevices().forEach((hostname, id) -> map.append(hostname + " " + id + "\n"));
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

            String currentFile = HappyBracketsToolWindow.getSettings().getString(setting);
            VirtualFile vfile = currentFile == null ? null : LocalFileSystem.getInstance().findFileByPath(currentFile.replace(File.separatorChar, '/'));

            //needs to run in Swing event dispatch thread, and then back again to JFX thread!!
            SwingUtilities.invokeLater(() -> {
                VirtualFile[] virtual_file = FileChooser.chooseFiles(descriptor, null, vfile);
                if (virtual_file != null && virtual_file.length > 0 && virtual_file[0] != null) {
                    Platform.runLater(() -> {
                        loadConfigFile(virtual_file[0].getCanonicalPath(), label, config_field, setting, load_button, event);
                    });
                }
            });
        });

        Button save_button = new Button("Save");
        save_button.setTooltip(new Tooltip("Save these " + label.toLowerCase() + " settings to a file."));
        save_button.setOnMouseClicked(event -> {
            //select a file
            FileSaverDescriptor fsd = new FileSaverDescriptor("Select " + label.toLowerCase() + " file to save to.", "Select " + label.toLowerCase() + " file to save to.");
            fsd.withShowHiddenFiles(true);
            final FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(fsd, project);

            String current_file_path = HappyBracketsToolWindow.getSettings().getString(setting);
            File currentFile = current_file_path != null ? new File(HappyBracketsToolWindow.getSettings().getString(setting)) : null;
            VirtualFile base_dir = null;
            String current_name = null;
            if (currentFile != null && currentFile.exists()) {
                base_dir = LocalFileSystem.getInstance().findFileByPath(currentFile.getParentFile().getAbsolutePath().replace(File.separatorChar, '/'));
                current_name = currentFile.getName();
            }
            else {
                base_dir = LocalFileSystem.getInstance().findFileByPath(HappyBracketsToolWindow.getPluginLocation());
                current_name = file_type == 0 ? "controller-config.json" : "known_devices";
            }
            final VirtualFile base_dir_final = base_dir;
            final String current_name_final = current_name;

            //needs to run in Swing event dispatch thread, and then back again to JFX thread!!
            SwingUtilities.invokeLater(() -> {
                final VirtualFileWrapper wrapper = dialog.save(base_dir_final, current_name_final);

                if (wrapper != null) {
                    Platform.runLater(() -> {
                        File config_file = wrapper.getFile();

                        // Check for overwrite of default config files (this doesn't apply to deployed plugin so disabling for now.)
                        //if ((new File(HappyBracketsToolWindow.getDefaultControllerConfigPath())).getAbsolutePath().equals(configFile.getAbsolutePath()) ||
                        //		(new File(HappyBracketsToolWindow.getDefaultKnownDevicesPath())).getAbsolutePath().equals(configFile.getAbsolutePath())) {
                        //	showPopup("Error saving " + label.toLowerCase() + ": cannot overwrite default configuration files.", saveButton, 5, event);
                        //}

                        try (PrintWriter out = new PrintWriter(config_file.getAbsolutePath())) {
                            out.print(config_field.getText());

                            HappyBracketsToolWindow.getSettings().set(setting, config_file.getAbsolutePath());
                        } catch (Exception ex) {
                            showPopup("Error saving " + label.toLowerCase() + ": " + ex.getMessage(), save_button, 5, event);
                        }
                    });
                }
            });
        });

        Button reset_button = new Button("Reset");
        reset_button.setTooltip(new Tooltip("Reset these " + label.toLowerCase() + " settings to their defaults."));
        reset_button.setOnMouseClicked(event -> {
            HappyBracketsToolWindow.getSettings().clear(setting);

            if (file_type == 0) {
                loadConfigFile(HappyBracketsToolWindow.getDefaultControllerConfigPath(), label, config_field, setting, reset_button, event);
                applyConfig(config_field.getText());
            }
            else {
                loadConfigFile(HappyBracketsToolWindow.getDefaultKnownDevicesPath(), label, config_field, setting, reset_button, event);
                applyKnownDevices(config_field.getText());
            }
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
        buttons.getChildren().addAll(load_button, save_button, reset_button, configApplyButton[file_type]);


        // If this is the main configuration pane, include buttons to set preferred IP version.
        FlowPane ipv_buttons = null;
        if (file_type == 0) {
            // Set IP version buttons.
            ipv_buttons = new FlowPane(DEFAULT_ELEMENT_SPACING, DEFAULT_ELEMENT_SPACING);
            ipv_buttons.setAlignment(Pos.TOP_LEFT);

            for (int ipv = 4; ipv <= 6; ipv += 2) {
                final int ipv_final = ipv;

                Button set_IPv = new Button("Set IntelliJ to prefer IPv" + ipv);
                String current_setting = System.getProperty("java.net.preferIPv" + ipv + "Addresses");

                if (current_setting != null && current_setting.toLowerCase().equals("true")) {
                    set_IPv.setDisable(true);
                }

                set_IPv.setTooltip(new Tooltip("Set the JVM used by IntelliJ to prefer IPv" + ipv + " addresses by default.\nThis can help resolve IPv4/Ipv6 incompatibility issues in some cases."));
                set_IPv.setOnMouseClicked(event -> {
                    // for the 32 and 64 bit versions of the options files.
                    for (String postfix : new String[]{"", "64"}) {
                        String postfix2 = "";
                        String filename = "/idea" + postfix + postfix2 + ".vmoptions";
                        // If this (Linux (and Mac?)) version of the file doesn't exist, try the Windows version.
                        if (!Paths.get(PathManager.getBinPath() + filename).toFile().exists()) {
                            postfix2 = ".exe";
                            filename = "/idea" + postfix + postfix2 + ".vmoptions";

                            if (!Paths.get(PathManager.getBinPath() + filename).toFile().exists()) {
                                showPopup("An error occurred: could not find default configuration file.", set_IPv, 5, event);
                                return;
                            }
                        }

                        // Create custom options files if they don't already exist.
                        File cust_opts_file = new File(PathManager.getCustomOptionsDirectory() + "/idea" + postfix + postfix2 + ".vmoptions");
                        if (!cust_opts_file.exists()) {
                            // Create copy of default.
                            try {
                                Files.copy(Paths.get(PathManager.getBinPath() + filename), cust_opts_file.toPath());
                            } catch (IOException e) {
                                logger.error("Error creating custom options file.", e);
                                showPopup("Error creating custom options file: " + e.getMessage(), set_IPv, 5, event);
                                return;
                            }
                        }

                        if (cust_opts_file.exists()) {
                            StringBuilder new_opts = new StringBuilder();
                            try (Stream<String> stream = Files.lines(cust_opts_file.toPath())) {
                                stream.forEach((line) -> {
                                    // Remove any existing preferences.
                                    if (!line.contains("java.net.preferIPv")) {
                                        new_opts.append(line + "\n");
                                    }
                                });
                                // Add new preference to end.
                                new_opts.append("-Djava.net.preferIPv" + ipv_final + "Addresses=true");
                            } catch (IOException e) {
                                logger.error("Error creating custom options file.", e);
                                showPopup("Error creating custom options file: " + e.getMessage(), set_IPv, 5, event);
                                return;
                            }

                            // Write new options to file.
                            try (PrintWriter out = new PrintWriter(cust_opts_file.getAbsolutePath())) {
                                out.println(new_opts);
                            } catch (FileNotFoundException e) {
                                // This totally shouldn't happen.
                            }
                        }
                    }

                    showPopup("You must restart IntelliJ for the changes to take effect.", set_IPv, 5, event);
                });

                ipv_buttons.getChildren().add(set_IPv);
            }
        }

        VBox config_pane = new VBox(DEFAULT_ELEMENT_SPACING);
        config_pane.setAlignment(Pos.TOP_LEFT);
        config_pane.getChildren().addAll(makeTitle(label), config_field, buttons);
        if (ipv_buttons != null) {
            config_pane.getChildren().add(ipv_buttons);
        }

        return config_pane;
        */
        return null;
    }

}
