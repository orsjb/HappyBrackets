package net.happybrackets.controller.gui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import net.happybrackets.controller.network.LocalDeviceRepresentation;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class DeviceRepresentationCell extends ListCell<LocalDeviceRepresentation> {

	@Override
    public void updateItem(final LocalDeviceRepresentation item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(null);
		//gui needs to be attached to "item", can't rely on DeviceRepresentationCell to bind to item
        if (item != null) {
			//set up main panel
			HBox main = new HBox();
			main.setStyle("-fx-font-family: sample; -fx-font-size: 10;");
			main.setAlignment(Pos.CENTER_LEFT);
			main.setSpacing(5);
			main.setMaxHeight(20);
			//elements
			HBox txthbox = new HBox();
			main.getChildren().add(txthbox);
			//name of the device
			Text name = new Text(item.hostname);
			name.setUnderline(true);
			txthbox.getChildren().add(name);
			txthbox.setSpacing(5);
			txthbox.setMinWidth(200);
			txthbox.setMaxWidth(200);
			txthbox.setAlignment(Pos.CENTER);
			//reset button
			Button resetButton = new Button("R");
			resetButton.setMaxHeight(5);
			resetButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override public void handle(ActionEvent e) {
					item.send("/device/reset");
				}
			});
			main.getChildren().add(resetButton);
			//bleep button
			Button bleepButton = new Button("B");
			bleepButton.setMaxHeight(5);
			bleepButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override public void handle(ActionEvent e) {
					item.send("/device/bleep");
				}
			});
			main.getChildren().add(bleepButton);
			//group allocations
			HBox groupsHbox = new HBox();
			groupsHbox.setAlignment(Pos.CENTER);
			main.getChildren().add(groupsHbox);
			for(int i = 0; i < 4; i++) {
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
			//
			Slider s = new Slider(0, 2, 1);
			s.setOrientation(Orientation.HORIZONTAL);
			s.setMaxWidth(100);
			s.valueProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> obs, Number oldval, Number newval) {
					item.send("/device/gain", newval.floatValue(), 50f);
				}
			});
			main.getChildren().add(s);
			//a status string
			Text statusText = new Text("status unknown");
			main.getChildren().add(statusText);
			item.addStatusUpdateListener(new LocalDeviceRepresentation.StatusUpdateListener() {
				@Override
				public void update(String state) {
					Platform.runLater(new Runnable() {
						public void run() {
							statusText.setText(state);
						}
					});
				}
			});
        	setGraphic(main);
        }	

    }
	
	
	
}
