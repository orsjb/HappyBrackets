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
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import net.happybrackets.controller.network.LocalDeviceRepresentation;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.text.Text;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.intellij_plugin.menu.device.PingMenu;
import net.happybrackets.intellij_plugin.menu.device.SoundMenu;

import javax.swing.*;

public class DeviceRepresentationCell extends ListCell<LocalDeviceRepresentation> {


	final String TRANSPARENT_STYLE = "-fx-background-color: transparent;";
	final String MAIN_FONT_STYLE = "-fx-font-family: sample; -fx-font-size: 10;";

	final String RED_IMAGE_NAME = "/icons/red.png";
	final String GREEN_IMAGE_NAME = "/icons/green.png";

	Image disconnectedImage = new Image(getClass().getResourceAsStream(RED_IMAGE_NAME));
	Image connectedImage = new Image(getClass().getResourceAsStream(GREEN_IMAGE_NAME));


    // define the username to use for SSH Command
    final String DEF_USERNAME = "pi";
    private String username = DEF_USERNAME;

    // we need to store our Item and listeners here so we can remove them
	// anything you add here will need to get reset if item is updated
	private LocalDeviceRepresentation localDevice = null;
	private LocalDeviceRepresentation.StatusUpdateListener updateListener = null;
	private LocalDeviceRepresentation.DeviceIdUpdateListener deviceIdUpdateListener = null;
	private LocalDeviceRepresentation.ConnectedUpdateListener connectedUpdateListener = null;
	private LocalDeviceRepresentation.StatusUpdateListener friendlyNameListener = null;


	//in case the user is not using pi as the default username for ssh
	public void setUsername(String val)
	{
		this.username = val;
	}
	String buildSSHCommand(String device_name)
    {
        return "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no " + username + "@" + device_name;
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
		friendlyNameListener = null;
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
			localDevice.removeFriendlyNameUpdateListener(friendlyNameListener);
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
		main.setStyle(MAIN_FONT_STYLE);
		main.setVgap(5);
		main.setHgap(5);
		int next_column = 0;
		int current_row = 0;


		Button connected_icon = new Button("");
		connected_icon.setMaxSize(2,2);
		connected_icon.setStyle(TRANSPARENT_STYLE);

		connected_icon.setGraphic(item.getIsConnected()? new ImageView(connectedImage): new ImageView(disconnectedImage));
		main.add(connected_icon, next_column, current_row);
		next_column++;
		connected_icon.setOnAction(event -> {localDevice.showControlScreen();});
		connected_icon.setAlignment(Pos.CENTER_LEFT);

		//name of the device
		//HBox txthbox = new HBox();
		//txthbox.setAlignment(Pos.CENTER_LEFT);
		//main.add(txthbox, next_column, current_row);

		//if item not currently active, change icon to green
		item.addConnectedUpdateListener(connectedUpdateListener = connected -> Platform.runLater(new Runnable() {
            public void run() {
				connected_icon.setGraphic(item.getIsConnected()? new ImageView(connectedImage): new ImageView(disconnectedImage));
            }
        }));

		Text name = new Text(item.getFriendlyName());
		name.setOnMouseClicked(event -> {
			localDevice.showControlScreen();
		});

		main.add(name, next_column, current_row);

		next_column++;

		item.addFriendlyNameUpdateListener(friendlyNameListener = new_name -> Platform.runLater(new Runnable() {
            public void run() {

                name.setText(new_name);

            }
        }));


		// Now let us display ID
		// add ID Text
		Text id_text = new Text("ID " + item.getID());
		main.add(id_text, next_column, current_row);
		next_column++;
		main.setHalignment(id_text, HPos.CENTER);
		item.addDeviceIdUpdateListener(deviceIdUpdateListener = new_id -> Platform.runLater(new Runnable() {
			@Override
			public void run() {
				id_text.setText("ID " + new_id);
			}
		}));


		// Display a reset Text
		Text reset_text = new Text("Reset");
		main.add(reset_text, next_column, current_row);
		next_column++;
		main.setHalignment(reset_text, HPos.CENTER);
		reset_text.setUnderline(true);
		reset_text.setOnMouseClicked(event->{
			item.resetDevice();
		});
		reset_text.setOnContextMenuRequested(event -> {
			ContextMenu contextMenu = new ContextMenu();
			SoundMenu menus = new SoundMenu(item);
			contextMenu.getItems().addAll(menus.getMenuItems());
			contextMenu.show(main, event.getScreenX(), event.getScreenY());
		});


		// Add our beep
		Text ping_text = new Text("Ping");
		main.add(ping_text, next_column, current_row);
		next_column++;
		main.setHalignment(ping_text, HPos.CENTER);
		ping_text.setUnderline(true);
		ping_text.setOnMouseClicked(event->{
			item.send(OSCVocabulary.Device.BLEEP);
		});

		ping_text.setOnContextMenuRequested(event -> {
			ContextMenu contextMenu = new ContextMenu();
			PingMenu menus = new PingMenu(item);
			contextMenu.getItems().addAll(menus.getMenuItems());
			contextMenu.show(main, event.getScreenX(), event.getScreenY());
		});

		Text send_text = new Text("Send");
		main.add(send_text, next_column, current_row);
		next_column++;

		main.setHalignment(send_text, HPos.CENTER);
		send_text.setUnderline(true);
		send_text.setOnMouseClicked(event->{
			// we will send current composition to device
		});

		// Now add slider

		Slider s = new Slider(0, 2, 1);
		s.setOrientation(Orientation.HORIZONTAL);
		s.setMaxWidth(100);
		s.valueProperty().addListener((obs, oldval, newval) -> item.send(OSCVocabulary.Device.GAIN, newval.floatValue(), 50f));
		main.add(s, next_column, current_row);
		next_column++;


		// add a button to display menu items
		Button menu_button = new Button("...");

		menu_button.setOnAction(event -> {
			Bounds local_bounds = menu_button.getBoundsInLocal();
			Bounds screen_bounds = menu_button.localToScreen(local_bounds);

			ContextMenu contextMenu = new ContextMenu();

			PingMenu menus = new PingMenu(item);
			contextMenu.getItems().addAll(menus.getMenuItems());
			contextMenu.show(main, screen_bounds.getMinX(), screen_bounds.getMinY() );
		});

		main.add(menu_button, next_column, current_row);
		next_column++;


		HBox controls = new HBox(5);
		controls.setAlignment(Pos.CENTER_LEFT);
		main.add(controls, 0, 1, 2, 1);



		/*
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
		*/


		// now start on third line if required

		final int num_columns = next_column;

		current_row++;
		next_column = 0;
		//a status string
		final String STATUS_PREFIX = "Status :";
		Text statusText = new Text(STATUS_PREFIX + localDevice.getStatus());
		main.add(statusText, next_column, current_row, num_columns, 1);
		main.setHalignment(statusText, HPos.LEFT);

		if (localDevice.isInvalidVersion())
		{
			if (invalidTextWarning == null)
			{
				invalidTextWarning = new Text(localDevice.getInvalidVersionWarning());

				main.add(invalidTextWarning, 0, 2, num_columns, 1);
			}
		}


		item.addStatusUpdateListener(updateListener = state -> Platform.runLater(new Runnable() {
            public void run() {
                statusText.setText(STATUS_PREFIX + state);

                if (localDevice.isInvalidVersion())
                {
                    if (invalidTextWarning == null)
                    {
                        invalidTextWarning = new Text(localDevice.getInvalidVersionWarning());
                        main.add(invalidTextWarning, 0, 2, num_columns, 1);
                    }
                }
            }
        }));


		controls.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
			@Override
			public void handle(ContextMenuEvent event) {
				// create a popup menu to allow accessing parameters
				ContextMenu contextMenu = new ContextMenu();


				MenuItem copy_name_command_menu = new MenuItem("Copy " + item.getAddress() + " to clipboard");
				copy_name_command_menu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						final Clipboard clipboard = Clipboard.getSystemClipboard();
						final ClipboardContent content = new ClipboardContent();
						content.putString(item.getAddress());
						clipboard.setContent(content);
					}
				});

				MenuItem copy_ssh_command_menu = new MenuItem("Copy SSH " + item.getAddress() + " to clipboard");
				copy_ssh_command_menu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						final Clipboard clipboard = Clipboard.getSystemClipboard();
						final ClipboardContent content = new ClipboardContent();
						content.putString(buildSSHCommand(item.getAddress()));
						clipboard.setContent(content);
					}
				});


				MenuItem copy_host_command_menu = new MenuItem("Copy " + item.deviceName + " to clipboard");
				copy_host_command_menu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						final Clipboard clipboard = Clipboard.getSystemClipboard();
						final ClipboardContent content = new ClipboardContent();
						content.putString(item.deviceName);
						clipboard.setContent(content);
					}
				});

				MenuItem request_status_menu = new MenuItem("Request status");
				request_status_menu.setDisable(localDevice.isIgnoringDevice());
				request_status_menu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						item.sendStatusRequest();
					}
				});


				MenuItem request_version_menu = new MenuItem("Request Version");
				request_version_menu.setDisable(localDevice.isIgnoringDevice());
				request_version_menu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						item.sendVersionRequest();
					}
				});


				MenuItem remove_item_menu = new MenuItem("Remove " + item.deviceName);
				remove_item_menu.setOnAction(event1 -> item.removeDevice());


				MenuItem show_controls_item_menu = new MenuItem("Show Controls");
				show_controls_item_menu.setDisable(localDevice.isIgnoringDevice());
				show_controls_item_menu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						localDevice.showControlScreen();
					}
				});


				CheckMenuItem ignore_controls_item_menu = new CheckMenuItem("Ignore this Device");
				ignore_controls_item_menu.setSelected(localDevice.isIgnoringDevice());
				ignore_controls_item_menu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						localDevice.setIgnoreDevice(!localDevice.isIgnoringDevice());
						Platform.runLater(() -> {

							ignore_controls_item_menu.setSelected(localDevice.isIgnoringDevice());
						});

					}
				});

				CheckMenuItem favourite_item_menu = new CheckMenuItem("Favourite");
				favourite_item_menu.setSelected(localDevice.isFavouriteDevice());
				favourite_item_menu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {

						localDevice.setFavouriteDevice(!localDevice.isFavouriteDevice());
						Platform.runLater(() -> {
							favourite_item_menu.setSelected(localDevice.isFavouriteDevice());
						});
					}
				});

				CheckMenuItem encrypt_item_menu = new CheckMenuItem("Encrypt Classes");
				encrypt_item_menu.setSelected(localDevice.isEncryptionEnabled());
				encrypt_item_menu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {

						localDevice.setEncryptionEnabled(!localDevice.isEncryptionEnabled());
						Platform.runLater(() -> {
							encrypt_item_menu.setSelected(localDevice.isFavouriteDevice());
						});
					}
				});

				MenuItem reboot_menu = new MenuItem("Reboot Device");
				reboot_menu.setDisable(localDevice.isIgnoringDevice());
				reboot_menu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {

						new Thread(() -> {
							try {

								int dialog_button = JOptionPane.YES_NO_OPTION;
								int dialog_result = JOptionPane.showConfirmDialog(null,
										"Are you sure you want to reboot " + localDevice.getFriendlyName() + "?", "Rebooting " + localDevice.getFriendlyName(), dialog_button);

								if (dialog_result == JOptionPane.YES_OPTION) {
									localDevice.rebootDevice();
								}
							} catch (Exception ex) {
							}
						}).start();


					}
				});

				MenuItem shutdown_menu = new MenuItem("Shutdown Device");
				shutdown_menu.setDisable(localDevice.isIgnoringDevice());
				shutdown_menu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {

						new Thread(() -> {
							try {

								int dialog_button = JOptionPane.YES_NO_OPTION;
								int dialog_result = JOptionPane.showConfirmDialog(null,
										"Are you sure you want to shutdown " + localDevice.getFriendlyName() + "?", "Shutting Down " + localDevice.getFriendlyName(), dialog_button);

								if (dialog_result == JOptionPane.YES_OPTION) {
									localDevice.shutdownDevice();
								}
							} catch (Exception ex) {
							}
						}).start();

					}
				});

				contextMenu.getItems().addAll(copy_name_command_menu, copy_ssh_command_menu, copy_host_command_menu, request_status_menu,
						request_version_menu, show_controls_item_menu, ignore_controls_item_menu, favourite_item_menu, encrypt_item_menu,
						remove_item_menu, reboot_menu, shutdown_menu);
				//contextMenu.show(controls, event.getScreenX(), event.getScreenY());
			}

		});

		setGraphic(main);

	}

}
