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


import com.intellij.ide.DataManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.util.Callback;
import javafx.util.Duration;
import net.happybrackets.core.ErrorListener;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;
import net.happybrackets.intellij_plugin.controller.config.ControllerConfig;
import net.happybrackets.intellij_plugin.controller.network.DeviceConnection;
import net.happybrackets.intellij_plugin.controller.network.LocalDeviceRepresentation;
import net.happybrackets.intellij_plugin.controller.network.SendToDevice;
import net.happybrackets.intellij_plugin.menu.RunSimulatorMenu;
import net.happybrackets.intellij_plugin.menu.context.SendCompositionAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.*;

import static net.happybrackets.intellij_plugin.NotificationMessage.displayNotification;

/**
 * Sets up the plugin GUI and handles associated events.
 */
public class IntelliJPluginGUIManager {
	private static final String PROBE_TEXT = "Probe";
	private String compositionsPath;
	private String projectDir; // The base directory of project
	private String locationHash;
	private ControllerConfig config;
	private DeviceConnection deviceConnection;
	private ListView<LocalDeviceRepresentation> deviceListView;
	private List<String> commandHistory;
	private int positionInCommandHistory = 0;
	private final int defaultElementSpacing = 10;

	final static Logger logger = LoggerFactory.getLogger(IntelliJPluginGUIManager.class);

	private Map<LocalDeviceRepresentation, DeviceErrorListener> deviceErrorListeners;

	private static final int minTextAreaHeight = 200;

	private static final int ALL = -1; // Send to all devices.
	private static final int SELECTED = -2; // Send to selected device(s).

	public IntelliJPluginGUIManager(Project project) {
		locationHash = project.getLocationHash();
		init();
		projectDir = project.getBaseDir().getCanonicalPath();

		compositionsPath = project.getBaseDir().getCanonicalPath() + "/" + config.getCompositionsPath() + "/" + project.getName();
		commandHistory = new ArrayList<>();
	}

	private void init() {
		config = HappyBracketsToolWindow.config;
		deviceConnection = ControllerEngine.getInstance().getDeviceConnection();
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
		final String DEVICES_TEXT = "Devices";
		final String DEVICE_FAVOURITES = DEVICES_TEXT + " (favourites only)";
		DeviceConnection connection = ControllerEngine.getInstance().getDeviceConnection();

		TitledPane device_pane = new TitledPane(connection.isShowOnlyFavourites()? DEVICE_FAVOURITES: DEVICES_TEXT, makeDevicePane());
		ControllerEngine.getInstance().getDeviceConnection().
				addFavouritesChangedListener(enabled -> {
					Platform.runLater(new Runnable() {
						@Override public void run() {
							device_pane.textProperty().setValue(enabled? DEVICE_FAVOURITES: DEVICES_TEXT);
						}
					});
				});

		TitledPane commands_pane = new TitledPane("Commands pane edited by JacJacobob", makeCustomCommandsPane());

		Node probe_pane = makeProbePanel();

		device_pane.setOnMouseClicked(event -> {
			ControllerEngine.getInstance().doProbe();
		});

		VBox main_container = new VBox(5);
		main_container.setFillWidth(true);
		main_container.getChildren().addAll(commands_pane,  probe_pane, device_pane);

		commands_pane.setExpanded(false);

		ScrollPane main_scroll = new ScrollPane();
		main_scroll.setFitToWidth(true);
		main_scroll.setFitToHeight(true);
		main_scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		main_scroll.setStyle("-fx-font-family: sample; -fx-font-size: 12;");
		main_scroll.setMinHeight(100);
		main_scroll.setContent(main_container);

		deviceListView.prefWidthProperty().bind(main_scroll.widthProperty().subtract(4));

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
					}
				}
			}
		});

		FlowPane messagepaths = new FlowPane(defaultElementSpacing, defaultElementSpacing);
		messagepaths.setAlignment(Pos.TOP_LEFT);

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

		Image playImage = new Image(getClass().getResourceAsStream("/icons/play.png"));
		ImageView playIV =new ImageView(playImage);
		playIV.setFitHeight(12);
		playIV.setFitWidth(12);
		playIV.setSmooth(true);

		Image stopImage = new Image(getClass().getResourceAsStream("/icons/stop.png"));
		ImageView stopIV =new ImageView(stopImage);
		stopIV.setFitHeight(12);
		stopIV.setFitWidth(12);
		stopIV.setSmooth(true);

		runSimulatorButton.setGraphic(playIV);

		runSimulatorButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (SimulatorShell.isRunning()){
					SimulatorShell.killSimulator();
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							runSimulatorButton.setText("Run Simulator");
							runSimulatorButton.setGraphic(playIV);
						}
					});
				}
				// run simulator
				else if (SimulatorShell.runSimulator(RunSimulatorMenu.getLastSdkPath(), RunSimulatorMenu.getLastProjectPath())) // we will put in a path later
				{
					NotificationMessage.displayNotification("Started simulator", NotificationType.INFORMATION);
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							runSimulatorButton.setText("Stop Simulator");
							runSimulatorButton.setGraphic(stopIV);
						}
					});
				}
				else {
					NotificationMessage.displayNotification("Failed to start simulator", NotificationType.ERROR);
				}
			}
		});

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

		Button probe_button = new Button();


		probe_button.setTooltip(new Tooltip(buildProbeToolTipText(num_devices, connected_devices)));

		probe_button.setOnMouseClicked(event -> {
			ControllerEngine.getInstance().doProbe();

		});

		FlowPane device_buttons = new FlowPane(defaultElementSpacing, defaultElementSpacing);

		Button reset_button = new Button("Reset all");
		reset_button.setTooltip(new Tooltip("Reset all devices"));

		Image resetImage = new Image(getClass().getResourceAsStream("/icons/reset.png"));
		ImageView resetIV =new ImageView(resetImage);
		resetIV.setFitHeight(12);
		resetIV.setFitWidth(12);
		resetIV.setSmooth(true);
		reset_button.setGraphic(resetIV);

		reset_button.setOnMouseClicked(event -> {
			ControllerEngine.getInstance().getDeviceConnection().deviceReset();

		});

		device_buttons.getChildren().add(reset_button);

		Button settingsButton = new Button("Old Settings");
		settingsButton.setOnMouseClicked(event -> {
			DataContext dataContext = DataManager.getInstance().getDataContext();
			Project project = DataKeys.PROJECT.getData(dataContext);
			ConfigurationScreen settings = new ConfigurationScreen(project);
			settings.show();
		});
		device_buttons.getChildren().add(settingsButton);

		Button newSettingsButton = new Button("New Settings");
		newSettingsButton.setOnMouseClicked(event -> {
			// Run this in the Swing event dispatch thread.
			SwingUtilities.invokeLater(() -> {
				DataContext dataContext = DataManager.getInstance().getDataContext();
				Project project = DataKeys.PROJECT.getData(dataContext);
				new ConfigurationScreenSwing(new ConfigurationScreenModel(ControllerEngine.getInstance(), project)).showAndGet();

			});
		});
		device_buttons.getChildren().add(newSettingsButton);

		Button ping_all_button = new Button("Ping all");
		ping_all_button.setTooltip(new Tooltip("Synchronised Ping all devices"));

		Image pingImage = new Image(getClass().getResourceAsStream("/icons/ping.png"));
		ImageView pingIV =new ImageView(pingImage);
		pingIV.setFitHeight(13);
		pingIV.setFitWidth(13);
		pingIV.setSmooth(true);
		ping_all_button.setGraphic(pingIV);

		ping_all_button.setOnMouseClicked(event -> {
			ControllerEngine.getInstance().getDeviceConnection().synchonisedPingAll(500);

		});

		Button send_all_button = new Button("Send all");
		send_all_button.setTooltip(new Tooltip("Send sketch to all devices all devices"));

		Image sendImage = new Image(getClass().getResourceAsStream("/icons/send.png"));
		ImageView sendIV =new ImageView(sendImage);
		sendIV.setFitHeight(14);
		sendIV.setFitWidth(14);
		sendIV.setSmooth(true);
		send_all_button.setGraphic(sendIV);

		send_all_button.setOnMouseClicked(event -> {
			try {
				try {
					//Project project = projects[i];
					ApplicationManager.getApplication().invokeLater(() -> {
						try {

							DataContext dataContext = DataManager.getInstance().getDataContext();

							Project project = DataKeys.PROJECT.getData(dataContext);
							Document current_doc = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();
							VirtualFile current_java_file = FileDocumentManager.getInstance().getFile(current_doc);

							VirtualFile class_file = SendCompositionAction.getClassFileFromJava(project, current_java_file);

							if (class_file != null) {
								String full_class_name = SendCompositionAction.getFullClassName(class_file.getCanonicalPath());

								try {
									SendToDevice.send(full_class_name, ControllerEngine.getInstance().getDeviceConnection().getDevices());
									displayNotification("Sent " + class_file.getNameWithoutExtension() + " to all devices", NotificationType.INFORMATION);
								} catch (Exception e) {
									displayNotification(e.getMessage(), NotificationType.ERROR);
									displayNotification(class_file.getName() + " may not have finished compiling or you may have an error in your code.", NotificationType.ERROR);
								}
							}
							else
							{
								displayNotification("Unable to find class. The class may not have finished compiling or you may have an error in your code.", NotificationType.ERROR);
							}
						} catch (Exception ex2) {

						}
					});
				} catch (Exception ex) {
					displayNotification("Unable to find class. The class may not have finished compiling or you may have an error in your code.",  NotificationType.ERROR);

				}

			}catch (Exception ex){
				System.out.println(ex.getMessage());
				displayNotification("Unable to find class. The class may not have finished compiling or you may have an error in your code.", NotificationType.ERROR);
			}
		});

		device_buttons.getChildren().add(ping_all_button);
		device_buttons.getChildren().add(send_all_button);
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
				return new DeviceRepresentationCell(projectDir);
			}
		});
		deviceListView.setMinHeight(minTextAreaHeight);

		return deviceListView;
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
