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

//import com.sun.org.apache.bcel.internal.generic.NEW;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import net.happybrackets.controller.gui.DynamicControlScreen;
import net.happybrackets.controller.network.LocalDeviceRepresentation;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.text.Text;
import net.happybrackets.core.BuildVersion;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.core.control.DynamicControl;

public class DeviceRepresentationCell extends ListCell<LocalDeviceRepresentation> {


    // define the username to use for SSH Command
    final String DEF_USERNAME = "pi";
    private String username = DEF_USERNAME;

    // we need to store our Item and listeners here so we can remove them
	// anything you add here will need to get reset if item is updated
	private LocalDeviceRepresentation localDevice = null;
	private LocalDeviceRepresentation.StatusUpdateListener updateListener = null;
	private LocalDeviceRepresentation.DeviceIdUpdateListener deviceIdUpdateListener = null;
	private LocalDeviceRepresentation.ConnectedUpdateListener connectedUpdateListener = null;

	//in case the user is not using pi as the default username for ssh
	public void setUsername(String val)
	{
		this.username = val;
	}
	String buildSSHCommand(String device_name)
    {
        return "ssh " + username + "@" + device_name + ".local";
    }

    Text invalidTextWarning = null;


	/**
	 * The cell parameters need to be reset every time a device is updated because they are bound to the olde LocalDevicerepresentation
	 */
	private void resetCellParameters(){
		localDevice = null;
		updateListener = null;
		deviceIdUpdateListener = null;
		connectedUpdateListener = null;
		invalidTextWarning = null;
	}

	@Override
	public void updateItem(final LocalDeviceRepresentation item, boolean empty) {
		super.updateItem(item, empty);

		if (localDevice != null && empty)
		{
			// This is where we will clear out all old listeners
			localDevice.removeStatusUpdateListener(updateListener);
			localDevice.removeDeviceIdUpdateListener(deviceIdUpdateListener);
			localDevice.removeConnectedUpdateListener(connectedUpdateListener);
			localDevice.resetDeviceHasDisplayed();


		}

		resetCellParameters();

		localDevice = item;

		setGraphic(null);

		//gui needs to be attached to "item", can't rely on DeviceRepresentationCell to bind to item
		if (item != null) {
			addCellRow(item);
			item.setDeviceHasDisplayed();
		}

		this.prefWidthProperty().bind(this.getListView().widthProperty().subtract(4));
	}

	synchronized void  addCellRow(LocalDeviceRepresentation item) {
		//set up main panel
		GridPane main = new GridPane();
		main.setStyle("-fx-font-family: sample; -fx-font-size: 10;");
		main.setVgap(5);

		//name of the device
		HBox txthbox = new HBox();
		txthbox.setAlignment(Pos.CENTER_LEFT);
		main.add(txthbox, 0, 0);
		Text name = new Text(item.deviceName);

		name.setUnderline(true);


		//if item not currently active, make that obvious by putting strikethrough through disconnected device
		name.setStrikethrough(!item.getIsConnected());

		item.addConnectedUpdateListener(connectedUpdateListener = new LocalDeviceRepresentation.ConnectedUpdateListener() {
			@Override
			public void update(boolean connected) {
				Platform.runLater(new Runnable() {
					public void run() {
						name.setStrikethrough(!connected);
					}
				});
			}
		});
		txthbox.getChildren().add(name);
		txthbox.setMinWidth(100);

		HBox controls = new HBox(5);
		controls.setAlignment(Pos.CENTER_LEFT);
		main.add(controls, 0, 1, 2, 1);

		//reset button
		Button resetButton = new Button("R");
		resetButton.setTooltip(new Tooltip("Reset device to its initial state."));
		resetButton.setMaxHeight(5);
		resetButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {

				item.resetDevice();
			}
		});
		controls.getChildren().add(resetButton);

		//reset sounding button
		Button resetSoundingButton = new Button("RS");
		resetSoundingButton.setTooltip(new Tooltip("Reset Sounding. Resets device to its initial state except for audio that is currently playing."));
		resetSoundingButton.setMaxHeight(5);
		resetSoundingButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				item.send(OSCVocabulary.Device.RESET_SOUNDING);
			}
		});
		controls.getChildren().add(resetSoundingButton);

		//reset sounding button
		Button clearSoundButton = new Button("CS");
		clearSoundButton.setTooltip(new Tooltip("Clear Sound. Stop audio that is currently playing on this device."));
		clearSoundButton.setMaxHeight(5);
		clearSoundButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				item.send(OSCVocabulary.Device.CLEAR_SOUND);
			}
		});
		controls.getChildren().add(clearSoundButton);

		//bleep button
		Button bleepButton = new Button("B");
		bleepButton.setTooltip(new Tooltip("Tell device to emit a bleep sound."));
		bleepButton.setMaxHeight(5);
		bleepButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				item.send(OSCVocabulary.Device.BLEEP);
			}
		});
		controls.getChildren().add(bleepButton);
		//group allocations
		HBox groupsHbox = new HBox();
		groupsHbox.setAlignment(Pos.CENTER);
		controls.getChildren().add(groupsHbox);
		for (int i = 0; i < 4; i++) {
			final int index = i;
			CheckBox c = new CheckBox();
			c.selectedProperty().addListener(new ChangeListener<Boolean>() {
				public void changed(ObservableValue<? extends Boolean> ov,
									Boolean oldval, Boolean newval) {
					item.groups[index] = newval;
				}
			});
			groupsHbox.getChildren().add(c);
		}

		Slider s = new Slider(0, 2, 1);
		s.setOrientation(Orientation.HORIZONTAL);
		s.setMaxWidth(100);
		s.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> obs, Number oldval, Number newval) {
				item.send(OSCVocabulary.Device.GAIN, newval.floatValue(), 50f);
			}
		});
		controls.getChildren().add(s);

		// add ID Text
		Text id_text = new Text("ID " + item.getID());
		main.add(id_text, 2, 0);
		main.setHalignment(id_text, HPos.CENTER);
		item.addDeviceIdUpdateListener(deviceIdUpdateListener = new LocalDeviceRepresentation.DeviceIdUpdateListener() {
			@Override
			public void update(int new_id) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						id_text.setText("ID " + new_id);
					}
				});
			}
		});


		//a status string
		Text statusText = new Text(localDevice.getStatus());
		main.add(statusText, 1, 0);
		main.setHalignment(statusText, HPos.CENTER);

		if (localDevice.isInvalidVersion())
		{
			if (invalidTextWarning == null)
			{
				invalidTextWarning = new Text(localDevice.getInvalidVersionWarning());

				main.add(invalidTextWarning, 0, 2);
			}
		}

		item.addStatusUpdateListener(updateListener = new LocalDeviceRepresentation.StatusUpdateListener() {
			@Override
			public void update(String state) {
				Platform.runLater(new Runnable() {
					public void run() {
						statusText.setText(state);

						if (localDevice.isInvalidVersion())
						{
							if (invalidTextWarning == null)
							{
								invalidTextWarning = new Text(localDevice.getInvalidVersionWarning());
								main.add(invalidTextWarning, 0, 2);
							}
						}
					}
				});
			}
		});



		controls.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
			@Override
			public void handle(ContextMenuEvent event) {
				// create a popup menu to allow accessing parameters
				ContextMenu contextMenu = new ContextMenu();


				MenuItem copy_name_command_menu = new MenuItem("Copy " + item.deviceName + " to clipboard");
				copy_name_command_menu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						final Clipboard clipboard = Clipboard.getSystemClipboard();
						final ClipboardContent content = new ClipboardContent();
						content.putString(item.deviceName);
						clipboard.setContent(content);
					}
				});

				MenuItem copy_ssh_command_menu = new MenuItem("Copy SSH " + item.deviceName + " to clipboard");
				copy_ssh_command_menu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						final Clipboard clipboard = Clipboard.getSystemClipboard();
						final ClipboardContent content = new ClipboardContent();
						content.putString(buildSSHCommand(item.deviceName));
						clipboard.setContent(content);
					}
				});

				MenuItem request_status_menu = new MenuItem("Request status");
				request_status_menu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						item.sendStatusRequest();
					}
				});


				MenuItem request_version_menu = new MenuItem("Request Version");
				request_version_menu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						item.sendVersionRequest();
					}
				});


				MenuItem remove_item_menu = new MenuItem("Remove " + item.deviceName);
				remove_item_menu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						item.deviceRemoved();
					}
				});


				MenuItem show_controls_item_menu = new MenuItem("Show Controls");
				show_controls_item_menu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						localDevice.showControlScreen();
					}
				});

				final String IGNORE_TEXT = "Ignore this Device";
				final String STOP_IGNORE_TEXT = "Stop Ignoring this Device";

				MenuItem ignore_controls_item_menu = new MenuItem(localDevice.isIgnoringDevice()? STOP_IGNORE_TEXT: IGNORE_TEXT);
				ignore_controls_item_menu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						localDevice.setIgnoreDevice(!localDevice.isIgnoringDevice());
					}
				});

				contextMenu.getItems().addAll(copy_name_command_menu, copy_ssh_command_menu, request_status_menu, request_version_menu, show_controls_item_menu, ignore_controls_item_menu, remove_item_menu);
				contextMenu.show(controls, event.getScreenX(), event.getScreenY());
			}

		});

		setGraphic(main);

	}

}
