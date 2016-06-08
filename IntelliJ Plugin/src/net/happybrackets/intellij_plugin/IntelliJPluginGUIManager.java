package net.happybrackets.intellij_plugin;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.sun.javafx.css.Style;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import net.happybrackets.controller.config.ControllerConfig;
import net.happybrackets.controller.gui.DeviceRepresentationCell;
import net.happybrackets.controller.network.DeviceConnection;
import net.happybrackets.controller.network.LocalDeviceRepresentation;
import net.happybrackets.controller.network.SendToDevice;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IntelliJPluginGUIManager {

	private String compositionsPath;
	private String currentCompositionSelection = null;
	private ControllerConfig config;
	private Project project;
	private DeviceConnection piConnection;
	private ComboBox<String> menu;
	private Text compositionPathText;
	private Style style;

	public IntelliJPluginGUIManager(@NotNull ControllerConfig controllerConfig, Project project, DeviceConnection piConnection) {
		this.config = controllerConfig;
		this.project = project;
		this.piConnection = piConnection;
		//initial compositions path
		//assume that this path is a path to a root classes folder, relative to the project
		//e.g., build/classes/tutorial or build/classes/compositions
		compositionsPath = project.getBaseDir().getCanonicalPath() + "/" + config.getCompositionsPath();
	}

	private void createGlobalButtons(Pane pane) {
		Text globcmtx = new Text("Global Commands");
		globcmtx.setTextOrigin(VPos.CENTER);
		pane.getChildren().add(globcmtx);
		//master buttons
		FlowPane globalcommands = new FlowPane();
		globalcommands.setAlignment(Pos.TOP_RIGHT);
//		globalcommands.setSpacing(10);
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
	}

	private void createCompositionSender(Pane pane) {
		//Sending compositions stuff
		Text sendCodetxt = new Text("Send Composition");
		pane.getChildren().add(sendCodetxt);
		FlowPane sendCodeHbox = new FlowPane();
		sendCodeHbox.setAlignment(Pos.TOP_RIGHT);
		pane.getChildren().add(sendCodeHbox);
		Button changeCompositionPath = new Button("Change");
		changeCompositionPath.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				//select a folder
				final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
				descriptor.setTitle("Select Composition Folder");
				//needs to run in event dispatch thread
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							VirtualFile[] virtualFile = FileChooser.chooseFiles(descriptor, null, null);
							if (virtualFile != null && virtualFile[0] != null) {
								updateCompositionPath(virtualFile[0].getCanonicalPath());
							}
						} catch(IllegalStateException e) {
							System.out.println("Note, non-fatal illegal state exception in (ntelliJPluginGUIManager.");
						}
					}
				});
			}
		});
		sendCodeHbox.getChildren().add(changeCompositionPath);
		Button refreshButton = new Button("Refresh");
		refreshButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				refreshCompositionList();
			}
		});
		sendCodeHbox.getChildren().add(refreshButton);
		//the following creates the ComboBox containing the compoositions
		menu = new ComboBox<String>();
//		menu.setMaxWidth(200);
		menu.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, final String arg2) {
				if(arg2 != null) {
					currentCompositionSelection = arg2; //re-attatch the composition path to the menu item name
				}
			}
		});
		//done with combo box
		sendCodeHbox.getChildren().add(menu);
		Button sendCode = new Button("Send");
		sendCode.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if(currentCompositionSelection != null) {
					try {
						//intelliJ specific code
						String pathToSend = compositionsPath + "/" + currentCompositionSelection;
						SendToDevice.send(pathToSend, piConnection.getPIHostnames());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		sendCodeHbox.getChildren().add(sendCode);
		compositionPathText = new Text();
		TextFlow compositionPathTextPane = new TextFlow(compositionPathText);
		compositionPathTextPane.setTextAlignment(TextAlignment.RIGHT);
		pane.getChildren().add(compositionPathTextPane);
	}

	private void createTextSender(Pane pane) {
		//text sender
		Text codetxt = new Text("Send Custom Commands");
		pane.getChildren().add(codetxt);
//		HBox codearea = new HBox();
//		pane.getChildren().add(codearea);
//		codearea.setSpacing(10);
		final TextField codeField = new TextField();
		codeField.setPrefSize(500, 40);
		pane.getChildren().add(codeField);
		HBox messagepaths = new HBox();
		messagepaths.setAlignment(Pos.TOP_RIGHT);
		pane.getChildren().add(messagepaths);
		Button sendAllButton = new Button("All");
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
	}

	public Scene setupGUI() {
		//core elements
		VBox mainContainer = new VBox();
		mainContainer.setStyle("-fx-font-family: sample; -fx-font-size: 12;");
		mainContainer.setAlignment(Pos.TOP_RIGHT);
		mainContainer.setFillWidth(true);
		mainContainer.setMinHeight(100);
		mainContainer.setSpacing(8);
		mainContainer.setPadding(new Insets(10,10,10,10));
		//global buttons
		createGlobalButtons(mainContainer);
		mainContainer.getChildren().add(new Separator());
		createCompositionSender(mainContainer);
		mainContainer.getChildren().add(new Separator());
		createTextSender(mainContainer);
		mainContainer.getChildren().add(new Separator());
		//list of PIs
		ListView<LocalDeviceRepresentation> list = new ListView<LocalDeviceRepresentation>();
		list.setItems(piConnection.getPIs());
		list.setCellFactory(new Callback<ListView<LocalDeviceRepresentation>, ListCell<LocalDeviceRepresentation>>() {
			@Override
			public ListCell<LocalDeviceRepresentation> call(ListView<LocalDeviceRepresentation> theView) {
				return new DeviceRepresentationCell();
			}
		});
		mainContainer.getChildren().add(list);
		//finally update composition path
		updateCompositionPath(compositionsPath);
		//return a JavaFX Scene
		return new Scene(mainContainer);
	}

	private void updateCompositionPath(String path) {
		//TODO this needs to be saved somewhere project-specific
		compositionsPath = path;
		compositionPathText.setText("Compositions: " + compositionsPath);
		//write the config file again
		refreshCompositionList();
	}

	private void refreshCompositionList() {
		System.out.println("refreshCompositionList: compositionsPath=" + compositionsPath);
		//TODO set up the project so that it auto-compiles and auto-refreshes on file save/edit.
		//locate the class files of composition classes
		//the following populates a list of Strings with class files, associated with compositions
		//populate combobox with list of compositions
		List<String> compositionFileNames = new ArrayList<String>();
		recursivelyGatherCompositionFileNames(compositionFileNames, compositionsPath);
		menu.getItems().clear();
		for(final String compositionFileName : compositionFileNames) {
			menu.getItems().add(compositionFileName);
		}
		if(compositionFileNames.size() > 0) {
			//if there was a current dynamoAction, grab it
			if (!menu.getItems().contains(currentCompositionSelection)) {
				currentCompositionSelection = compositionFileNames.get(0);
			}
			menu.setValue(currentCompositionSelection);
		} else {
			currentCompositionSelection = null;
		}
	}

	private void recursivelyGatherCompositionFileNames(List<String> compositionFileNames, String currentDir) {
		//TODO best approach would be to examine code source tree, then we can gather dependencies properly as well
		//scan the current dir for composition files
		//drop into any folders encountered
		//add any file that looks like a composition file (is a top-level class)
		String[] contents = new File(currentDir).list();
		if(contents != null) {
			for(String item : contents) {
				item = currentDir + "/" + item;
				File f = new File(item);
				if(f.isDirectory()) {
					recursivelyGatherCompositionFileNames(compositionFileNames, item);
				} else if(f.isFile()) {
					if(item.endsWith(".class") && !item.contains("$")) {
						item = item.substring(compositionsPath.length() + 1, item.length() - 6);
						// 6 equates to the length fo the .class extension, the + 1 is to remove the composition path and trailing '/' for presentation in the menu
						compositionFileNames.add(item);
					}
				}
			}
		}
	}

}
