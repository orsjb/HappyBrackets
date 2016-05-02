package net.happybrackets.controller.gui;

import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import net.happybrackets.controller.network.DeviceConnection;

public abstract class GUIBuilder {

	public static void createButtons(Pane pane, final DeviceConnection piConnection) {
    	
		

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
	
}
