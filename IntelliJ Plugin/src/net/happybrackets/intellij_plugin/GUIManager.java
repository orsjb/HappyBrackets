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

import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import net.happybrackets.intellij_plugin.controller.config.ControllerConfig;
import net.happybrackets.intellij_plugin.controller.network.DeviceConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GUIManager {

	final static Logger logger = LoggerFactory.getLogger(GUIManager.class);

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
					piConnection.deviceReboot();
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
					piConnection.deviceShutdown();
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
					piConnection.deviceSync();
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
					piConnection.deviceReset();
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
					piConnection.deviceResetSounding();
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
					piConnection.deviceClearSound();
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
				piConnection.sendToAllDevices(msg, args);
			}
		});
		messagepaths.getChildren().add(sendAllButton);
		Text sendTogrpstxt = new Text("Send to Group");
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
					piConnection.sendToDeviceGroup(index, msg, args);
				}
			});
	    	b.setText("" + (i + 1));
	    	messagepaths.getChildren().add(b);
		}

		pane.getChildren().add(new Separator());

	}


}
