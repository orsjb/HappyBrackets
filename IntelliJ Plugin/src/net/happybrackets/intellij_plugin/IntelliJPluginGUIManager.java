/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.intellij_plugin;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.sun.javafx.css.Style;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import javafx.util.Callback;
import javafx.util.Duration;
import net.happybrackets.controller.ControllerEngine;
import net.happybrackets.controller.config.ControllerConfig;
import net.happybrackets.controller.network.ControllerAdvertiser;
import net.happybrackets.controller.network.DeviceConnection;
import net.happybrackets.controller.network.LocalDeviceRepresentation;
import net.happybrackets.controller.network.SendToDevice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import net.happybrackets.core.ErrorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;


/**
 * Sets up the plugin GUI and handles associated events.
 */
public class IntelliJPluginGUIManager {
	private static final String PROBE_TEXT = "Probe";
	private String compositionsPath;
	private String currentCompositionSelection = null;
	private ControllerConfig config;
	private final Project project;
	private DeviceConnection deviceConnection;
	private ListView<LocalDeviceRepresentation> deviceListView;
	private ComboBox<String> compositionSelector;
	private Text compositionPathText;
	private List<String> commandHistory;
	private int positionInCommandHistory = 0;
	private Style style;
	private final int defaultElementSpacing = 10;
	private Button[] configApplyButton = new Button[2]; // 0 = overall config, 1 = known devices.

	final static Logger logger = LoggerFactory.getLogger(IntelliJPluginGUIManager.class);
	private LocalDeviceRepresentation logDevice; // The device we're currently monitoring for log events, if any.
	private LocalDeviceRepresentation.LogListener logListener; // The listener for new log events, so we can remove when necessary.

	private Map<LocalDeviceRepresentation, DeviceErrorListener> deviceErrorListeners;

	private static final int minTextAreaHeight = 200;

	private static final int ALL = -1; // Send to all devices.
	private static final int SELECTED = -2; // Send to selected device(s).



	public IntelliJPluginGUIManager(Project project) {
		this.project = project;
		init();
		commandHistory = new ArrayList<>();
	}

	private void init() {
		config = HappyBracketsToolWindow.config;
		deviceConnection = ControllerEngine.getInstance().getDeviceConnection();
		//initial compositions path
		//assume that this path is a path to a root classes folder, relative to the project
		//e.g., build/classes/tutorial or build/classes/compositions
		compositionsPath = project.getBaseDir().getCanonicalPath() + "/" + config.getCompositionsPath() + "/" + project.getName();

		deviceErrorListeners = new HashMap<>();
		// Add ErrorListener's to the devices so we can report to the user when an error occurs communicating
		// with the device.
		deviceConnection.getDevices().addListener(new ListChangeListener<LocalDeviceRepresentation>() {
			@Override
			public void onChanged(Change<? extends LocalDeviceRepresentation> change) {
				while (change.next()) {
					if (change.wasAdded()) {
						change.getAddedSubList().forEach((device) -> {
							DeviceErrorListener listener = new DeviceErrorListener(device);
							deviceErrorListeners.put(device, listener);
							device.addErrorListener(listener);
						});
					}
					if (change.wasRemoved()) {
						change.getRemoved().forEach((device) -> {
							device.removeErrorListener(deviceErrorListeners.get(device));
							deviceErrorListeners.remove(device);
						});
					}
				}
			}
		});
	}


	/**
	 * A function to enable or disable a control in context of main thread
	 * @param c control to modify
	 * @param disable whether to disable
	 */
	private void disableControl(Control c, boolean disable)
	{
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				c.setDisable(disable);
			}
		});
	}


	public Scene setupGUI() {
		//core elements
		TitledPane device_pane = new TitledPane("Devices", makeDevicePane());
		//TitledPane config_pane = new TitledPane("Configuration", makeConfigurationPane(0));
		//TitledPane known_devices_pane = new TitledPane("Known Devices", makeConfigurationPane(1));
		//TitledPane global_pane = new TitledPane("Global Management", makeGlobalPane());
		TitledPane composition_pane = new TitledPane("Compositions", makeCompositionPane());
		TitledPane commands_pane = new TitledPane("Commands", makeCustomCommandsPane());

		Node probe_pane = makeProbePanel();

		device_pane.setOnMouseClicked(event -> {
			ControllerEngine.getInstance().doProbe();
		});

		//TitledPane debug_pane = new TitledPane("Debug", makeDebugPane());

		//config_pane.setExpanded(false);
		//known_devices_pane.setExpanded(false);
		//debug_pane.setExpanded(false);

		VBox main_container = new VBox(5);
		main_container.setFillWidth(true);
		//main_container.getChildren().addAll(config_pane, known_devices_pane, global_pane, composition_pane,  device_pane);
		main_container.getChildren().addAll(commands_pane,  probe_pane, device_pane);

		ScrollPane main_scroll = new ScrollPane();
		main_scroll.setFitToWidth(true);
		main_scroll.setFitToHeight(true);
		main_scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		main_scroll.setStyle("-fx-font-family: sample; -fx-font-size: 12;");
		main_scroll.setMinHeight(100);
		main_scroll.setContent(main_container);

		deviceListView.prefWidthProperty().bind(main_scroll.widthProperty().subtract(4));

		//finally update composition path
		updateCompositionPath(compositionsPath);

		//return a JavaFX Scene
		return new Scene(main_scroll);
	}


	private Text makeTitle(String title) {
		Text text = new Text(title);
		text.setTextAlignment(TextAlignment.CENTER);
		text.setTextOrigin(VPos.CENTER);
		text.setStyle("-fx-font-weight: bold;");
		return text;
	}


	private Pane makeGlobalPane() {
		//master buttons
		FlowPane globalcommands = new FlowPane(defaultElementSpacing, defaultElementSpacing);
		globalcommands.setAlignment(Pos.TOP_LEFT);
		{
			Button b = new Button("Reboot");

			b.setOnMouseClicked(event -> deviceConnection.deviceReboot());
			b.setTooltip(new Tooltip("Reboot all devices."));
			globalcommands.getChildren().add(b);

			// Disable the Button if there are no devices
			deviceListView.getItems().addListener(new ListChangeListener<LocalDeviceRepresentation>() {
				@Override
				public void onChanged(Change<? extends LocalDeviceRepresentation> c) {
					disableControl(b, deviceListView.getItems().size() < 1);
				}
			});

		}
		{
			Button b = new Button("Shutdown");

			b.setOnMouseClicked(event -> deviceConnection.deviceShutdown());
			b.setTooltip(new Tooltip("Shutdown all devices."));
			globalcommands.getChildren().add(b);
			// Disable the Button if there are no devices
			deviceListView.getItems().addListener(new ListChangeListener<LocalDeviceRepresentation>() {
				@Override
				public void onChanged(Change<? extends LocalDeviceRepresentation> c) {
					disableControl(b, deviceListView.getItems().size() < 1);
				}
			});
		}
		{
			Button b = new Button("Reset");

			b.setOnMouseClicked(e -> deviceConnection.deviceReset());
			b.setTooltip(new Tooltip("Reset all devices to their initial state (same as Reset Sounding + Clear Sound)."));
			globalcommands.getChildren().add(b);
			// Disable the Button if there are no devices
			deviceListView.getItems().addListener(new ListChangeListener<LocalDeviceRepresentation>() {
				@Override
				public void onChanged(Change<? extends LocalDeviceRepresentation> c) {
					disableControl(b, deviceListView.getItems().size() < 1);
				}
			});
		}
		{
			Button b = new Button("Reset Sounding");

			b.setOnMouseClicked(e -> deviceConnection.deviceResetSounding());
			b.setTooltip(new Tooltip("Reset all devices to their initial state except for audio that is currently playing."));
			globalcommands.getChildren().add(b);
			// Disable the Button if there are no devices
			deviceListView.getItems().addListener(new ListChangeListener<LocalDeviceRepresentation>() {
				@Override
				public void onChanged(Change<? extends LocalDeviceRepresentation> c) {
					disableControl(b, deviceListView.getItems().size() < 1);
				}
			});
		}
		{
			Button b = new Button("Clear Sound");

			b.setOnMouseClicked(e -> deviceConnection.deviceClearSound());
			b.setTooltip(new Tooltip("Clears all of the audio that is currently playing on all devices."));
			globalcommands.getChildren().add(b);
			// Disable the Button if there are no devices
			deviceListView.getItems().addListener(new ListChangeListener<LocalDeviceRepresentation>() {
				@Override
				public void onChanged(Change<? extends LocalDeviceRepresentation> c) {
					disableControl(b, deviceListView.getItems().size() < 1);
				}
			});
		}
		return globalcommands;
	}

	/**
	 * Make Configuration/Known devices pane.
	 * @param file_type 0 == configuration, 1 == known devices.
	 */
	private Pane makeConfigurationPane(final int file_type) {
		final TextArea config_field = new TextArea();
		final String label = file_type == 0 ? "Configuration" : "Known Devices";
		final String setting = file_type == 0 ? "controllerConfigPath" : "knownDevicesPath";

		//configField.setPrefSize(400, 250);
		config_field.setMinHeight(minTextAreaHeight);
		// Load initial config into text field.
		if (file_type == 0) {
			config_field.setText(ControllerEngine.getInstance().getCurrentConfigString());
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

			String currentFile = ControllerEngine.getInstance().getSettings().getString(setting);
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

			String current_file_path = ControllerEngine.getInstance().getSettings().getString(setting);
			File currentFile = current_file_path != null ? new File(ControllerEngine.getInstance().getSettings().getString(setting)) : null;
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

							ControllerEngine.getInstance().getSettings().set(setting, config_file.getAbsolutePath());
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
			ControllerEngine.getInstance().getSettings().clear(setting);

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

		FlowPane buttons = new FlowPane(defaultElementSpacing, defaultElementSpacing);
		buttons.setAlignment(Pos.TOP_LEFT);
		buttons.getChildren().addAll(load_button, save_button, reset_button, configApplyButton[file_type]);


		// If this is the main configuration pane, include buttons to set preferred IP version.
		FlowPane ipv_buttons = null;
		if (file_type == 0) {
			// Set IP version buttons.
			ipv_buttons = new FlowPane(defaultElementSpacing, defaultElementSpacing);
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

		VBox config_pane = new VBox(defaultElementSpacing);
		config_pane.setAlignment(Pos.TOP_LEFT);
		config_pane.getChildren().addAll(makeTitle(label), config_field, buttons);
		if (ipv_buttons != null) {
			config_pane.getChildren().add(ipv_buttons);
		}

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


	private void applyConfig(String config) {
		HappyBracketsToolWindow.setConfig(config, null);
		init();

		deviceListView.setItems(deviceConnection.getDevices());

		refreshCompositionList();
	}


	private void applyKnownDevices(String kd) {
		deviceConnection.setKnownDevices(kd.split("\\r?\\n"));
	}


	private Node makeCustomCommandsPane(){
		VBox container = new VBox(defaultElementSpacing);
		container.getChildren().addAll(
				makeTitle("Send Custom Command"),
				makeCustomCommandPane()
		);

		// Work around. On Mac the layout doesn't allow enough height in some instances.
		container.setMinHeight(100);

		return container;

	}

	private Node makeCompositionPane() {
		VBox container = new VBox(defaultElementSpacing);
		container.getChildren().addAll(
				makeTitle("Composition folder"),
				makeCompositionFolderPane(),
				new Separator(),
				makeTitle("Send Composition"),
				makeCompositionSendPane()
		);

		// Work around. On Mac the layout doesn't allow enough height in some instances.
		container.setMinHeight(210);

		return container;
	}

	private Pane makeCompositionFolderPane() {
		compositionPathText = new Text();
		TextFlow composition_path_text_pane = new TextFlow(compositionPathText);
		composition_path_text_pane.setTextAlignment(TextAlignment.RIGHT);

		Button change_composition_path = new Button("Change");
		change_composition_path.setTooltip(new Tooltip("Select a new folder containing composition files."));
		change_composition_path.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				//select a folder
				final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
				descriptor.setTitle("Select Composition Folder");
				//needs to run in Swing event dispatch thread, and then back again to JFX thread!!
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						VirtualFile[] virtual_file = FileChooser.chooseFiles(descriptor, null, null);
						if (virtual_file != null && virtual_file.length > 0 && virtual_file[0] != null) {
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									updateCompositionPath(virtual_file[0].getCanonicalPath());
								}
							});
						}
					}
				});
			}
		});
		Button refresh_button = new Button("Refresh");
		refresh_button.setTooltip(new Tooltip("Reload the available composition files."));
		refresh_button.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				refreshCompositionList();
			}
		});

		Button clear_button = new Button ("Erase");
		clear_button.setTooltip(new Tooltip("Erase all composition classes from list."));
		clear_button.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.setTitle("Confirmation Delet");
				alert.setHeaderText("Erasing compiled classes");
				alert.setContentText("This will erase the listed compositions. Is that OK?");
				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK){
					eraseCompositionsList();
				}


			}
		});




		FlowPane composition_folder_pane = new FlowPane(10, 10);
		composition_folder_pane.setAlignment(Pos.TOP_LEFT);
		composition_folder_pane.getChildren().addAll(compositionPathText, change_composition_path, refresh_button, clear_button);

		return composition_folder_pane;
	}

	private Pane makeCompositionSendPane() {
		GridPane composition_send_pane = new GridPane();
		composition_send_pane.setHgap(defaultElementSpacing);
		composition_send_pane.setVgap(defaultElementSpacing);

		// Create the ComboBox containing the compoositions
		compositionSelector = new ComboBox<String>();
//		compositionSelector.setMaxWidth(200);
		compositionSelector.setTooltip(new Tooltip("Select a composition file to send."));
		compositionSelector.setPrefWidth(200);
		compositionSelector.setButtonCell(
				new ListCell<String>() {
					{
						super.setPrefWidth(100);
					}

					@Override
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (item != null) {
							String[] parts = item.split("/");
							if (parts.length == 0) {
								setText(item);
							} else {
								setText(parts[parts.length - 1]);
							}
						}
					}
				}
		);
		compositionSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, final String arg2) {
				if (arg2 != null) {
					currentCompositionSelection = arg2; //re-attach the composition path to the compositionSelector item name
				}
			}
		});
		composition_send_pane.add(compositionSelector, 0, 0, 6, 1);

		int button_column = 0;

		Button composition_send_all_button = new Button("All");
		composition_send_all_button.setTooltip(new Tooltip("Send the selected composition to all devices."));
		composition_send_all_button.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				sendSelectedComposition(deviceConnection.getDevices());
			}
		});
		composition_send_pane.add(composition_send_all_button, button_column, 1);
		button_column++;
		// Disable the all Button if there are none
		deviceListView.getItems().addListener(new ListChangeListener<LocalDeviceRepresentation>() {
			@Override
			public void onChanged(Change<? extends LocalDeviceRepresentation> c) {
				disableControl(composition_send_all_button, deviceListView.getItems().size() < 1);
			}
		});


		Button composition_send_selected_button = new Button("Selected");
		// we do not want send to selected until we have one selected
		composition_send_selected_button.setDisable(true);
		composition_send_selected_button.setTooltip(new Tooltip("Send the selected composition to the selected devices."));
		composition_send_selected_button.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				sendSelectedComposition(deviceListView.getSelectionModel().getSelectedItems());
			}
		});

		// If we have none selected, we need to have selected Buttons Disabled
		// we also need to set our selected device in the project
		deviceListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<LocalDeviceRepresentation>() {
			@Override
			public void changed(ObservableValue<? extends LocalDeviceRepresentation> observable, LocalDeviceRepresentation old_value, LocalDeviceRepresentation new_value) {
				disableControl(composition_send_selected_button, new_value == null);
				deviceConnection.setDeviceSelected(project.getLocationHash(), new_value);
			}
		});


		composition_send_pane.add(composition_send_selected_button, button_column, 1);
		button_column++;


		return composition_send_pane;
	}

	private void sendSelectedComposition(List<LocalDeviceRepresentation> devices) {
		if (currentCompositionSelection != null) {
			//intelliJ specific code
			String path_to_send = compositionsPath + "/" + currentCompositionSelection;
			try {
				SendToDevice.send(path_to_send, devices);
			} catch (Exception ex) {
				logger.error("Unable to send composition: '{}'!", path_to_send, ex);
			}
		}
	}


	private Pane makeCustomCommandPane() {
		final TextField code_field = new TextField();
		code_field.setTooltip(new Tooltip("Enter a custom command to send."));
		code_field.setPrefSize(500, 40);
		code_field.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(event.getEventType() == KeyEvent.KEY_PRESSED) {
					if(event.getCode() == KeyCode.UP) {
						positionInCommandHistory--;
						if(positionInCommandHistory < 0) positionInCommandHistory = 0;
						if(commandHistory.size() > 0) {
							String command = commandHistory.get(positionInCommandHistory);
							if (command != null) {
								code_field.setText(command);
							}
						}
					} else if(event.getCode() == KeyCode.DOWN) {
						positionInCommandHistory++;
						if(positionInCommandHistory >= commandHistory.size()) positionInCommandHistory = commandHistory.size() - 1;
						if(commandHistory.size() > 0) {
							String command = commandHistory.get(positionInCommandHistory);
							if (command != null) {
								code_field.setText(command);
							}
						}
					} else if(!event.getCode().isModifierKey() && !event.getCode().isNavigationKey()){
						//nothing needs to be done here but I thought it'd be cool to have a comment in an if block.
					}
				}
			}
		});

		FlowPane messagepaths = new FlowPane(defaultElementSpacing, defaultElementSpacing);
		messagepaths.setAlignment(Pos.TOP_LEFT);

		int button_column = 0;

		Button send_all_OSC_button = new Button("All");
		send_all_OSC_button.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				sendCustomCommand(code_field.getText(), ALL);
			}
		});

		// Disable the all Button if there are none
		deviceListView.getItems().addListener(new ListChangeListener<LocalDeviceRepresentation>() {
			@Override
			public void onChanged(Change<? extends LocalDeviceRepresentation> c) {
				disableControl(send_all_OSC_button, deviceListView.getItems().size() < 1);
			}

		});


		messagepaths.getChildren().add(send_all_OSC_button);

		Button send_selected_OSC_button = new Button("Selected");
		send_selected_OSC_button.setDisable(true);
		send_selected_OSC_button.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				sendCustomCommand(code_field.getText(), SELECTED);
			}
		});
		// If we have none selected, we need to have selected Buttons Disabled
		deviceListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<LocalDeviceRepresentation>() {
			@Override
			public void changed(ObservableValue<? extends LocalDeviceRepresentation> observable, LocalDeviceRepresentation old_value, LocalDeviceRepresentation new_value) {
				disableControl(send_selected_OSC_button, new_value == null);

			}
		});
		messagepaths.getChildren().add(send_selected_OSC_button);

		Button runSimulatorButton =  new Button("Run Simulator");

		runSimulatorButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (SimulatorShell.isRunning()){
					SimulatorShell.killSimulator();
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							runSimulatorButton.setText("Run Simulator");
						}
					});
				}
				// run simulator
				else if (SimulatorShell.runSimulator("", "")) // we will put in a path later
				{
					NotificationMessage.displayNotification("Started simulator", NotificationType.INFORMATION);
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							runSimulatorButton.setText("Stop Simulator");
						}
					});
				}
				else {
					NotificationMessage.displayNotification("Failed to start simulator", NotificationType.ERROR);
				}
			}
		});

		//messagepaths.getChildren().add(runSimulatorButton); Make sure you actually make it work first
		VBox custom_command_pane = new VBox(defaultElementSpacing);
		custom_command_pane.getChildren().addAll(code_field, messagepaths);
		return custom_command_pane;
	}


	/**
	 * Send a custom command to the specified devices.
	 * @param text The command to send.
	 * @param devices_or_group If 'all' is false the group of devices to send the command to.
	 */
	public void sendCustomCommand(String text, int devices_or_group) {
		String code_text = text.trim();
		commandHistory.add(code_text);
		positionInCommandHistory = commandHistory.size() - 1;
		//need to parse the code text
		String[] commands = code_text.split("[;]");	//different commands separated by ';'
		for(String command : commands) {
			command = command.trim();
			String[] elements = command.split("[ ]");
			String msg = elements[0];
			Object[] args = new Object[elements.length - 1];
			for (int i = 0; i < args.length; i++) {
				String s = elements[i + 1];
				try {
					args[i] = Integer.parseInt(s);
				} catch (Exception ex) {
					try {
						args[i] = Double.parseDouble(s);
					} catch (Exception exx) {
						args[i] = s;
					}
				}
			}
			if (devices_or_group == ALL) {
				deviceConnection.sendToAllDevices(msg, args);
			}
			else if (devices_or_group == SELECTED) {
				deviceConnection.sendToDeviceList(deviceListView.getSelectionModel().getSelectedItems(), msg, args);
			}
			else {
				deviceConnection.sendToDeviceGroup(devices_or_group, msg, args);
			}
		}
	}


	private void updateCompositionPath(String path) {
		//TODO this needs to be saved somewhere project-specific
		compositionsPath = path;
		compositionPathText.setText(compositionsPath);
		//write the config file again
		refreshCompositionList();
	}

	private void eraseCompositionsList(){
		logger.debug("Clear Compositions: compositionsPath={}", compositionsPath);
		//TODO set up the project so that it auto-compiles and auto-refreshes on file save/edit.
		//locate the class files of composition classes
		//the following populates a list of Strings with class files, associated with compositions
		//populate combobox with list of compositions
		List<String> composition_file_names = new ArrayList<String>();
		recursivelyGatherCompositionFileNames(composition_file_names, compositionsPath);

		for (String composition_name : composition_file_names){
			String full_path =  compositionsPath + "/" + composition_name + ".class";

			try
			{
				File file = new File(full_path);
				file.delete();
			}
			catch (Exception ex){
				System.out.println("Error deleting file " +  ex.getMessage());
			}

		}
		refreshCompositionList();

	}


	private void refreshCompositionList() {
		logger.debug("refreshCompositionList: compositionsPath={}", compositionsPath);
		//TODO set up the project so that it auto-compiles and auto-refreshes on file save/edit.
		//locate the class files of composition classes
		//the following populates a list of Strings with class files, associated with compositions
		//populate combobox with list of compositions
		List<String> composition_file_names = new ArrayList<String>();
		recursivelyGatherCompositionFileNames(composition_file_names, compositionsPath);
		// Sort compositions alphabetically.
		Collections.sort(composition_file_names, String.CASE_INSENSITIVE_ORDER);
		compositionSelector.getItems().clear();
		for(final String composition_file_name : composition_file_names) {
			compositionSelector.getItems().add(composition_file_name);
		}
		if(composition_file_names.size() > 0) {
			//if there was a current dynamoAction, grab it
			if (!compositionSelector.getItems().contains(currentCompositionSelection)) {
				currentCompositionSelection = composition_file_names.get(0);
			}
			compositionSelector.setValue(currentCompositionSelection);
		} else {
			currentCompositionSelection = null;
		}
	}

	private void recursivelyGatherCompositionFileNames(List<String> composition_file_names, String current_dir) {
		//TODO best approach would be to examine code source tree, then we can gather dependencies properly as well
		//scan the current dir for composition files
		//drop into any folders encountered
		//add any file that looks like a composition file (is a top-level class)
		String[] contents = new File(current_dir).list();
		if(contents != null) {
			for(String item : contents) {
				item = current_dir + "/" + item;
				File f = new File(item);
				if(f.isDirectory()) {
					recursivelyGatherCompositionFileNames(composition_file_names, item);
				} else if(f.isFile()) {
					if(item.endsWith(".class") && !item.contains("$")) {
						item = item.substring(compositionsPath.length() + 1, item.length() - 6);
						// 6 equates to the length fo the .class extension, the + 1 is to remove the composition path and trailing '/' for presentation in the compositionSelector
						composition_file_names.add(item);
					}
				}
			}
		}
	}


	/**
	 * Create the text we want to display for the probe tooltip
	 * @param num_devices The total number of devices we have listed
	 * @param num_connected The number that have a connected status
	 * @return Text to display in tooltip
	 */
	private String buildProbeToolTipText(int num_devices, int num_connected){
		String ret = "Click to probe for devices on the network";

		if (num_devices > 0){
			ret = num_connected + " connected from " + num_devices + " listed. " + ret;
		}
		return ret;
	}
	private Node makeProbePanel(){
		FlowPane device_panel = new FlowPane(defaultElementSpacing, defaultElementSpacing);

		device_panel.setAlignment(Pos.TOP_LEFT);


		String probe_text = PROBE_TEXT;
		ObservableList<LocalDeviceRepresentation> devices = ControllerEngine.getInstance().getDeviceConnection().getDevices();

		int num_devices = 0;
		int connected_devices = 0;
		for (LocalDeviceRepresentation device:
				devices) {
			num_devices++;
			if (device.getIsConnected()){
				connected_devices++;
			}
		}

		if (num_devices > 0){
			probe_text = connected_devices + "/" + num_devices+ " - " + PROBE_TEXT;
		}
		Button probe_button = new Button(PROBE_TEXT);


		probe_button.setTooltip(new Tooltip(buildProbeToolTipText(num_devices, connected_devices)));

		probe_button.setOnMouseClicked(event -> {
			ControllerEngine.getInstance().doProbe();

		});

		FlowPane device_buttons = new FlowPane(defaultElementSpacing, defaultElementSpacing);
		device_buttons.getChildren().add(probe_button);

		device_panel.getChildren().add(device_buttons);

		// we need to display how many devices

		LocalDeviceRepresentation.addDeviceConnectedUpdateListener(new LocalDeviceRepresentation.DeviceConnectedUpdateListener() {
			@Override
			public void update(LocalDeviceRepresentation localdevice, boolean connected) {

				int num_devices = 0;
				int connected_devices = 0;
				for (LocalDeviceRepresentation device:
						devices) {
					num_devices++;
					if (device.getIsConnected()){
						connected_devices++;
					}
				}

				int finalNum_devices = num_devices;
				int finalConnected_devices = connected_devices;
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (finalNum_devices == 0){
							probe_button.setText(PROBE_TEXT);
						}
						else {
							probe_button.setText(finalConnected_devices + "/" + finalNum_devices+ " - " + PROBE_TEXT);
						}
						probe_button.setTooltip(new Tooltip(buildProbeToolTipText(finalNum_devices, finalConnected_devices)));
					}
				});
			}
		});




		return device_panel;
	}
	private Node makeDevicePane() {

		//list of Devices
		deviceListView = new ListView<LocalDeviceRepresentation>();
		deviceListView.setItems(deviceConnection.getDevices());
		deviceListView.setCellFactory(new Callback<ListView<LocalDeviceRepresentation>, ListCell<LocalDeviceRepresentation>>() {
			@Override
			public ListCell<LocalDeviceRepresentation> call(ListView<LocalDeviceRepresentation> theView) {
				return new DeviceRepresentationCell();
			}
		});
		deviceListView.setMinHeight(minTextAreaHeight);


		return deviceListView;
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


	private class DeviceErrorListener implements ErrorListener {
		LocalDeviceRepresentation device;
		public DeviceErrorListener(LocalDeviceRepresentation device) {
			this.device = device;
		}
		@Override
		public void errorOccurred(Class clazz, String description, Exception ex) {
			Point2D pos = deviceListView.localToScreen(0, 0);
			if (pos != null) {
				// If it appears we have an IPv4/IPv6 incompatibility.
				if (ex != null && ex instanceof java.net.SocketException && ex.getMessage().contains("rotocol")) {
					showPopup("Error communicating with device " + device.getID() + ". It looks like there might be an IPv4/IPv6\nincompatibility. Try setting the protocol to use in the Configuration panel.", deviceListView, 10, pos.getX(), pos.getY());
				} else {
					showPopup("Error communicating with device " + device.getID() + ".", deviceListView, 5, pos.getX(), pos.getY());
				}
			}
		}
	}
}
