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
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import net.happybrackets.intellij_plugin.controller.gui.DialogDisplay;
import net.happybrackets.intellij_plugin.controller.gui.DynamicControlScreen;
import net.happybrackets.intellij_plugin.controller.network.LocalDeviceRepresentation;
import javafx.geometry.Orientation;
import javafx.scene.text.Text;
import net.happybrackets.intellij_plugin.controller.network.SendToDevice;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.intellij_plugin.menu.context.SendCompositionAction;
import net.happybrackets.intellij_plugin.controller.gui.device.PingMenu;
import net.happybrackets.intellij_plugin.controller.gui.device.SoundMenu;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static net.happybrackets.intellij_plugin.NotificationMessage.displayNotification;

public class DeviceRepresentationCell extends ListCell<LocalDeviceRepresentation> {

	final static org.slf4j.Logger logger = LoggerFactory.getLogger(DeviceRepresentationCell.class);

	final int ICON_FIT_SIZE = 12;

	final String TRANSPARENT_STYLE = "-fx-background-color: transparent;";
	final String MAIN_FONT_STYLE = "-fx-font-family: sample; -fx-font-size: 10;";

	final String RED_IMAGE_NAME = "/icons/red.png";
	final String RED_STAR_NAME = "/icons/redstar.png";
	final String GREEN_IMAGE_NAME = "/icons/green.png";
	final String GREEN_STAR_NAME = "/icons/greenstar.png";

	Image disconnectedImage = new Image(getClass().getResourceAsStream(RED_IMAGE_NAME));
	Image disconnectedFavouriteImage = new Image(getClass().getResourceAsStream(RED_STAR_NAME));
	Image connectedImage = new Image(getClass().getResourceAsStream(GREEN_IMAGE_NAME));
	Image connectedFavouriteImage = new Image(getClass().getResourceAsStream(GREEN_STAR_NAME));


	String projectDir;

    // define the username to use for SSH Command
    final String DEF_USERNAME = "pi";
    private String username = DEF_USERNAME;

    // we need to store our Item and listeners here so we can remove them
	// anything you add here will need to get reset if item is updated
	private LocalDeviceRepresentation localDevice = null;
	private LocalDeviceRepresentation.StatusUpdateListener updateListener = null;
	private LocalDeviceRepresentation.StatusUpdateListener configListener = null;
	private LocalDeviceRepresentation.DeviceIdUpdateListener deviceIdUpdateListener = null;
	private LocalDeviceRepresentation.ConnectedUpdateListener connectedUpdateListener = null;
	private LocalDeviceRepresentation.StatusUpdateListener friendlyNameListener = null;
	private LocalDeviceRepresentation.FavouriteChangedListener favouriteChangedListener = null;
	private LocalDeviceRepresentation.GainChangedListener gainChangedListener = null;


	public DeviceRepresentationCell (String project_dir){
		projectDir = project_dir;
	}

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
	 * Check that none of the modifiers are down so we can prevent mouse
	 * action. This enables us to do a context menu and not this one
	 * @param event the mouse event
	 * @return true if there are no modifiers
	 */
	boolean mouseModifiersClear(MouseEvent event){
		return !(event.isControlDown() || event.isAltDown()
				|| event.isMetaDown()  || event.isShiftDown());
	}

	/**
	 * The cell parameters need to be reset every time a device is updated because they are bound to the olde LocalDevicerepresentation
	 */
	private void resetCellParameters(){
		localDevice = null;
		updateListener = null;
		configListener = null;
		deviceIdUpdateListener = null;
		connectedUpdateListener = null;
		invalidTextWarning = null;
		friendlyNameListener = null;
		favouriteChangedListener = null;
		gainChangedListener = null;
	}

	@Override
	public void updateItem(final LocalDeviceRepresentation item, boolean empty) {
		super.updateItem(item, empty);

		if (localDevice != null && empty)
		{
			// This is where we will clear out all old listeners
			localDevice.removeStatusUpdateListener(updateListener);
			localDevice.removeDeviceIdUpdateListener(deviceIdUpdateListener);
			localDevice.removeConfigUpdateListener(configListener);
			localDevice.removeConnectedUpdateListener(connectedUpdateListener);
			localDevice.removeFriendlyNameUpdateListener(friendlyNameListener);
			localDevice.removeFavouriteListener(favouriteChangedListener);
			localDevice.removeGainCHangedListener(gainChangedListener);
		}

		resetCellParameters();

		localDevice = item;

		setGraphic(null);

		//gui needs to be attached to "item", can't rely on DeviceRepresentationCell to bind to item
		if (item != null) {
			addCellRow(item);
		}

		this.prefWidthProperty().bind(this.getListView().widthProperty().subtract(4));
	}

	/**
	 * Set the undelineing and text style of our clickable text
	 * @param text_field the text filed we are setting
	 */
	void setClickTextStyle(Text text_field){
		text_field.setUnderline(true);
	}

	/**
	 * Provide visual feedback that a text line has been clicked
	 *
	 * @param text_field the text filed we clicked
	 */
	void displayTextLineClick(Text text_field){
		text_field.setUnderline(false);
		new Thread(() -> {
			try {
				Thread.sleep(500);

			} catch (Exception ex) {
			}
			Platform.runLater(()->
			{
				text_field.setUnderline(true);
			});
		}).start();
	}

	/**
	 * Get the image based on it's state
	 * @param item the LocalDeviceRepresentation item
	 * @return the image to display
	 */
	private ImageView getIconImage(LocalDeviceRepresentation item){
		ImageView image;
		if (item.isFavouriteDevice()){
			image = item.getIsConnected()? new ImageView(connectedFavouriteImage): new ImageView(disconnectedFavouriteImage);
		}
		else{
			image = item.getIsConnected()? new ImageView(connectedImage): new ImageView(disconnectedImage);
		}

		return image;
	}

	@SuppressWarnings("deprecation")
	synchronized void  addCellRow(LocalDeviceRepresentation item) {
		//set up main panel
		GridPane main = new GridPane();
		main.setStyle(MAIN_FONT_STYLE);
		main.setVgap(5);
		main.setHgap(5);
		int next_column = 0;
		int current_row = 0;

		ImageView image  = getIconImage(item);



		image.setFitHeight(ICON_FIT_SIZE);
		image.setFitWidth(ICON_FIT_SIZE);

		Button connected_icon = new Button("");
		connected_icon.setPrefSize(ICON_FIT_SIZE, ICON_FIT_SIZE);
		connected_icon.setMaxSize(ICON_FIT_SIZE, ICON_FIT_SIZE);
		connected_icon.setStyle(TRANSPARENT_STYLE);


		connected_icon.setGraphic(image);
		main.add(connected_icon, next_column, current_row);
		next_column++;
		connected_icon.setOnAction(event -> {localDevice.showControlScreen();});

		connected_icon.setOnMouseClicked(event -> {
			ContextMenu contextMenu = new ContextMenu();
			MenuItem show_controls_menu = new MenuItem("Show Controls");
			if (event.isControlDown()){

				contextMenu.getItems().add(show_controls_menu);

				if (item.getDynamicControlScreen() != null) {
					MenuItem rebuild_controls_menu = new MenuItem("Rebuild Controls");
					rebuild_controls_menu.setOnAction(event2 -> {
						DynamicControlScreen screen = item.getDynamicControlScreen();
						if (screen != null) {
							screen.eraseDynamicControls();
						}

						item.sendControlsRequest();
					});

					contextMenu.getItems().add(rebuild_controls_menu);

					rebuild_controls_menu.setDisable(!item.getDynamicControlScreen().getIsShowing());
				}

				contextMenu.getItems().add(new SeparatorMenuItem());

				PingMenu menus = new PingMenu(item, projectDir);
				contextMenu.getItems().addAll(menus.getMenuItems());

				contextMenu.show(main, event.getScreenX(), event.getScreenY());
			}
		});

		connected_icon.setAlignment(Pos.CENTER_LEFT);

		connected_icon.setTooltip(new Tooltip("Click to display Controls"));
		main.getColumnConstraints().add(new ColumnConstraints(ICON_FIT_SIZE * 2));
		main.getColumnConstraints().add(new ColumnConstraints(100));

		//if item not currently active, change icon to green
		item.addConnectedUpdateListener(connectedUpdateListener = connected -> Platform.runLater(new Runnable() {
            public void run() {
				ImageView image  = getIconImage(item);

				image.setFitHeight(ICON_FIT_SIZE);
				image.setFitWidth(ICON_FIT_SIZE);

				connected_icon.setGraphic(image);
            }
        }));

		String display_name = formatName(item.getFriendlyName());
		Text name = new Text(display_name);

		name.setOnMouseClicked(event -> {
			// we do not want to show controls if it is a context menu
			if (mouseModifiersClear(event)) {
				localDevice.showControlScreen();

			}
		});


		name.setOnContextMenuRequested(event -> {
			ContextMenu contextMenu = new ContextMenu();
			MenuItem show_controls_menu = new MenuItem("Show Controls");
			show_controls_menu.setOnAction(event2 -> {
				if (!item.showControlScreen()) {
					DialogDisplay.displayDialog("Unable to create connection with device");
				}
			});
			contextMenu.getItems().add(show_controls_menu);

			if (item.getDynamicControlScreen() != null) {
				MenuItem rebuild_controls_menu = new MenuItem("Rebuild Controls");
				rebuild_controls_menu.setOnAction(event2 -> {
					DynamicControlScreen screen = item.getDynamicControlScreen();
					if (screen != null) {
						screen.eraseDynamicControls();
					}

					item.sendControlsRequest();
				});

				contextMenu.getItems().add(rebuild_controls_menu);

				rebuild_controls_menu.setDisable(!item.getDynamicControlScreen().getIsShowing());
			}


			contextMenu.getItems().add(new SeparatorMenuItem());

			PingMenu menus = new PingMenu(item, projectDir);
			contextMenu.getItems().addAll(menus.getMenuItems());

			contextMenu.show(main, event.getScreenX(), event.getScreenY());
		});


		main.add(name, next_column, current_row);

		next_column++;

		item.addFriendlyNameUpdateListener(friendlyNameListener = new_name -> Platform.runLater(new Runnable() {
            public void run() {

                name.setText(new_name);

            }
        }));

		item.addFavouriteListener(favouriteChangedListener = device -> Platform.runLater(new Runnable() {
			public void run() {
				ImageView image  = getIconImage(item);

				image.setFitHeight(ICON_FIT_SIZE);
				image.setFitWidth(ICON_FIT_SIZE);

				connected_icon.setGraphic(image);
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

		item.addConfigUpdateListener(configListener = config -> Platform.runLater(new Runnable() {
			public void run() {
				displayNotification(config, NotificationType.INFORMATION);
			}
		}));

		// Display a reset Text
		Text reset_text = new Text("Reset");
		main.add(reset_text, next_column, current_row);
		next_column++;
		main.setHalignment(reset_text, HPos.CENTER);

		setClickTextStyle(reset_text);

		reset_text.setOnMouseClicked(event->{
			if (mouseModifiersClear(event)) {
				displayTextLineClick(reset_text);
				item.resetDevice();
			}
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
		setClickTextStyle(ping_text);

		ping_text.setOnMouseClicked(event->{
			if (mouseModifiersClear(event)) {
				displayTextLineClick(ping_text);
				item.send(OSCVocabulary.Device.BLEEP);
			}
		});

		ping_text.setOnContextMenuRequested(event -> {
			ContextMenu contextMenu = new ContextMenu();
			PingMenu menus = new PingMenu(item, projectDir);
			contextMenu.getItems().addAll(menus.getMenuItems());
			contextMenu.show(main, event.getScreenX(), event.getScreenY());
		});

		Text send_text = new Text("Send");
		main.add(send_text, next_column, current_row);
		next_column++;

		main.setHalignment(send_text, HPos.CENTER);

		setClickTextStyle(send_text);
		send_text.setOnMouseClicked(event->{

			try {
				try {
					//Project project = projects[i];
					displayTextLineClick(send_text);
					ApplicationManager.getApplication().invokeLater(() -> {
						try {

							DataContext dataContext = DataManager.getInstance().getDataContext();

							Project project = DataKeys.PROJECT.getData(dataContext);
							Document current_doc = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();
							VirtualFile current_java_file = FileDocumentManager.getInstance().getFile(current_doc);

							VirtualFile class_file = SendCompositionAction.getClassFileFromJava(project, current_java_file);

							if (class_file != null) {
								String full_class_name = SendCompositionAction.getFullClassName(class_file.getCanonicalPath());
								List<LocalDeviceRepresentation> selected = new ArrayList<>();
								selected.add(item);
								try {
									SendToDevice.send(full_class_name, selected);
									displayNotification("Sent " + class_file.getNameWithoutExtension() + " to " + item.getFriendlyName(), NotificationType.INFORMATION);
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

			// we will send current composition to device
		});

		// Now add slider

		Slider s = new Slider(0, 2, 1);
		s.setOrientation(Orientation.HORIZONTAL);
		s.setMaxWidth(100);


		s.valueProperty().addListener((obs, oldval, newval) ->
		{

			if (Math.abs(oldval.floatValue() - newval.floatValue()) > Math.abs(Float.MIN_VALUE)) {
				item.send(OSCVocabulary.Device.GAIN, newval.floatValue(), 50f);
			}
		});

		item.addGainChangedListener(gainChangedListener = gain -> Platform.runLater(new Runnable() {
			public void run() {

				try {
					float current_value = (float) s.getValue();
					if (Math.abs(gain - current_value) > Math.abs(Float.MIN_VALUE)) {
						s.setValue(gain);
					}

				} catch (Exception ex) {
				}
				;

			}
		}));

		main.add(s, next_column, current_row);
		next_column++;


		// add a button to display menu items
		Button menu_button = new Button("...");

		menu_button.setOnAction(event -> {
			Bounds local_bounds = menu_button.getBoundsInLocal();
			Bounds screen_bounds = menu_button.localToScreen(local_bounds);

			ContextMenu contextMenu = new ContextMenu();
			MenuItem show_controls_menu = new MenuItem("Show Controls");
			show_controls_menu.setOnAction(event2 -> {
				if (!item.showControlScreen()) {
					DialogDisplay.displayDialog("Unable to create connection with device");
				}
			});
			contextMenu.getItems().add(show_controls_menu);
			// see if we can add a rebuild Controls menu here

			if (item.getDynamicControlScreen() != null) {
				MenuItem rebuild_controls_menu = new MenuItem("Rebuild Controls");
				rebuild_controls_menu.setOnAction(event2 -> {
					DynamicControlScreen screen = item.getDynamicControlScreen();
					if (screen != null) {
						screen.eraseDynamicControls();
					}

					item.sendControlsRequest();
				});

				contextMenu.getItems().add(rebuild_controls_menu);

				rebuild_controls_menu.setDisable(!item.getDynamicControlScreen().getIsShowing());
			}

			contextMenu.getItems().add(new SeparatorMenuItem());

			PingMenu menus = new PingMenu(item, projectDir);
			contextMenu.getItems().addAll(menus.getMenuItems());
			contextMenu.show(main, screen_bounds.getMinX(), screen_bounds.getMinY() );
		});

		main.add(menu_button, next_column, current_row);
		next_column++;


		HBox controls = new HBox(5);
		controls.setAlignment(Pos.CENTER_LEFT);
		main.add(controls, 0, 1, 2, 1);

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

                if (localDevice != null) {
					if (localDevice.isInvalidVersion()) {
						if (invalidTextWarning == null) {
							invalidTextWarning = new Text(localDevice.getInvalidVersionWarning());
							main.add(invalidTextWarning, 0, 2, num_columns, 1);
						}
					}
				}
            }
        }));

		setGraphic(main);
		item.sendVersionRequest();
	}

	/**
	 * Format the name so it is no longer than the maximum allowed.
	 * Put three dots after
	 * @param friendlyName the original name
	 * @return the name as we want to display it
	 */
	private String formatName(String friendlyName) {
		final int MAX_NAME_LEN = 20;
		final String CONT_TEXT = " ..."; // we display this to show name is not complete

		String ret  = friendlyName;

		if (friendlyName.length() > MAX_NAME_LEN){
			ret =  friendlyName.substring(0, MAX_NAME_LEN - CONT_TEXT.length() - 1) + CONT_TEXT;
		}

		return ret;
	}
}
