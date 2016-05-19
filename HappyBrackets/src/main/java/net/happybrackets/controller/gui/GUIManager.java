package net.happybrackets.controller.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import net.happybrackets.controller.network.DeviceConnection;
import net.happybrackets.controller.network.LocalDeviceRepresentation;
import net.happybrackets.controller.network.SendToDevice;
import net.happybrackets.core.ControllerConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GUIManager {

	String currentPIPO = "";
	ControllerConfig config;

	public GUIManager(ControllerConfig controllerConfig) {
		this.config = controllerConfig;
	}

	private void createButtons(Pane pane, final DeviceConnection piConnection) {

		Text globcmtx = new Text("Global commands");
		globcmtx.setTextOrigin(VPos.CENTER);
		pane.getChildren().add(globcmtx);
		
		//master buttons
		HBox globalcommands = new HBox();
		globalcommands.setSpacing(10);
		pane.getChildren().add(globalcommands);
		
//		globalcommands.getChildren().add(new Separator());
    	
		{
			Button b = new Button();
	    	b.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					piConnection.piReboot();
				}
			});
	    	b.setText("Reboot");
	    	globalcommands.getChildren().add(b);
		}


		{
			Button b = new Button();
	    	b.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					piConnection.piShutdown();
				}
			});
	    	b.setText("Shutdown");
	    	globalcommands.getChildren().add(b);
		}
    	

		{
			Button b = new Button();
	    	b.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					piConnection.piSync();
				}
			});
	    	b.setText("Sync");
	    	globalcommands.getChildren().add(b);
		}

		{
			Button b = new Button();
	    	b.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					piConnection.piReset();
				}
			});
	    	b.setText("Reset");
	    	globalcommands.getChildren().add(b);
		}

		{
			Button b = new Button();
	    	b.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					piConnection.piResetSounding();
				}
			});
	    	b.setText("Reset Sounding");
	    	globalcommands.getChildren().add(b);
		}

		{
			Button b = new Button();
	    	b.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					piConnection.piClearSound();
				}
			});
	    	b.setText("Clear Sound");
	    	globalcommands.getChildren().add(b);
		}
		
		//text sender
		pane.getChildren().add(new Separator());
		
		Text codetxt = new Text("Custom Commands");
		pane.getChildren().add(codetxt);
		
		HBox codearea = new HBox();
		pane.getChildren().add(codearea);
		codearea.setSpacing(10);
		
		final TextField codeField = new TextField();
		codeField.setMinSize(500, 50);
		codearea.getChildren().add(codeField);

		HBox messagepaths = new HBox();
		messagepaths.setSpacing(10);
		pane.getChildren().add(messagepaths);
		
		Button sendAllButton = new Button("Send All");
		sendAllButton.setMinWidth(80);
		sendAllButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				String codeText = codeField.getText();
				//need to parse the code text
				String[] elements = codeText.split("[ ]");
				String msg = elements[0];
				Object[] args = new Object[elements.length - 1];
				for(int i = 0; i < args.length; i++) {
					String s = elements[i + 1];
					try {
						args[i] = Integer.parseInt(s);
					} catch(Exception ex) {
						try {
							args[i] = Double.parseDouble(s);
						} catch(Exception exx) {
							args[i] = s;
						}
					}
				}
				piConnection.sendToAllPIs(msg, args);
			}
		});
		messagepaths.getChildren().add(sendAllButton);
		Text sendTogrpstxt = new Text("Send to group");
		messagepaths.getChildren().add(sendTogrpstxt);
//		messagepaths.getChildren().add(new Separator());
		for(int i = 0; i < 4; i++) {
			Button b = new Button();
			final int index = i;
	    	b.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					String codeText = codeField.getText();
					//need to parse the code text
					String[] elements = codeText.split("[ ]");
					String msg = elements[0];
					Object[] args = new Object[elements.length - 1];
					for(int i = 0; i < args.length; i++) {
						String s = elements[i + 1];
						try {
							args[i] = Integer.parseInt(s);
						} catch(Exception ex) {
							try {
								args[i] = Double.parseDouble(s);
							} catch(Exception exx) {
								args[i] = s;
							}
						}
					}
					piConnection.sendToPIGroup(index, msg, args);
				}
			});
	    	b.setText("" + (i + 1));
	    	messagepaths.getChildren().add(b);
		}

		pane.getChildren().add(new Separator());
    	
	}

	public Scene setupGUI(DeviceConnection piConnection) {
		//core elements
		Group masterGroup = new Group();
		BorderPane border = new BorderPane();
		masterGroup.getChildren().add(border);
		border.setPadding(new Insets(10));
		//the top button box
		VBox topBox = new VBox();
		border.setTop(topBox);
		topBox.setMinHeight(100);
		topBox.setSpacing(10);
		createButtons(topBox, piConnection);
		//list of PIs
		ListView<LocalDeviceRepresentation> list = new ListView<LocalDeviceRepresentation>();
		list.setItems(piConnection.getPIs());
		list.setCellFactory(new Callback<ListView<LocalDeviceRepresentation>, ListCell<LocalDeviceRepresentation>>() {
			@Override
			public ListCell<LocalDeviceRepresentation> call(ListView<LocalDeviceRepresentation> theView) {
				return new DeviceRepCell();
			}
		});
		list.setMinWidth(1000);
		list.setMaxWidth(1000);
		list.setMinHeight(500);
		border.setCenter(list);

		//currently not expecting the comps path to contain a valid value
		String compsPath = config.getCompositionsPath();

		//the following populates a list of Strings with class files, associated with compositions

		//populate combobox with list of compositions
		List<String> compositionFileNames = new ArrayList<String>();
		Queue<File> dirs = new LinkedList<File>();
		dirs.add(new File(compsPath));
		//load the composition files
		if(dirs != null && dirs.size() > 0) {
			while (!dirs.isEmpty()) {
				if(dirs.peek() != null) {
					for (File f : dirs.poll().listFiles()) {
						if (f.isDirectory()) {
							dirs.add(f);
						} else if (f.isFile()) {
							String path = f.getPath();
							path = path.substring(config.getCompositionsPath().length() + 1, path.length() - 6); // 6 equates to the length fo the .class extension, the + 1 is to remove path '/'
							if (!path.contains("$")) {
								System.out.println(path);
								compositionFileNames.add(path);
							}
						}
					}
				}
				else {
					//throw out the null File object so we don't get stuck in a never ending loop.
					dirs.poll();
				}
			}
		}

		//the following creates the ComboBox containing the compoositions

		ComboBox<String> menu = new ComboBox<String>();
		for(final String compositionFileName : compositionFileNames) {
			menu.getItems().add(compositionFileName);
		}
		menu.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, final String arg2) {
				if(arg2 != null) {
					currentPIPO = config.getCompositionsPath() + "/" + arg2; //re-attatch the composition path to the menu item name
				}
			}
		});

		//done with combo box

		Text sendCodetxt = new Text("Send Compositions");
		topBox.getChildren().add(sendCodetxt);
		HBox sendCodeHbox = new HBox();
		sendCodeHbox.setSpacing(10);
		topBox.getChildren().add(sendCodeHbox);
		sendCodeHbox.getChildren().add(menu);
		Button sendCode = new Button("Send");
		sendCode.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				try {
					SendToDevice.send(currentPIPO, piConnection.getPIHostnames());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		sendCodeHbox.getChildren().add(sendCode);
		topBox.getChildren().add(new Separator());
		//set up the scene
		Scene scene = new Scene(masterGroup);
		return scene;
	}
	
}
