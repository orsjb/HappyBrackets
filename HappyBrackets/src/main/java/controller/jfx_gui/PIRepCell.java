package controller.jfx_gui;

import controller.network.LocalPIRepresentation;
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
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class PIRepCell extends ListCell<LocalPIRepresentation> {
	
	int count = 0;
	
	public PIRepCell() {
//		setMinHeight(80);
	}
	
	@Override
    public void updateItem(final LocalPIRepresentation item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(null);
		//gui needs to be attached to "item", can't rely on PIRepCell to bind to item
        if (item != null) {
        	if(item.getGui() == null) {
        		//set up main panel
	        	HBox hbox = new HBox();
	        	hbox.setSpacing(5);
	        	hbox.setMaxHeight(30);
	        	//elements
	        	HBox txthbox = new HBox();
	        	hbox.getChildren().add(txthbox);
	        	//name of the PI
	        	Text name = new Text(item.hostname);
	        	name.setUnderline(true);
	        	txthbox.getChildren().add(name);
	        	txthbox.setSpacing(5);
	        	txthbox.setMinWidth(300);
	        	//reset button
	        	Button resetButton = new Button("Reset");
	        	resetButton.setOnAction(new EventHandler<ActionEvent>() {
	        	    @Override public void handle(ActionEvent e) {
	        	    	item.send("/PI/reset");
	        	    }
	        	});
	        	hbox.getChildren().add(resetButton);
	        	//bleep button
	        	Button bleepButton = new Button("Bleep");
	        	bleepButton.setOnAction(new EventHandler<ActionEvent>() {
	        	    @Override public void handle(ActionEvent e) {
	        	    	item.send("/PI/bleep");
	        	    }
	        	});
	        	hbox.getChildren().add(bleepButton);
	        	//group allocations
	        	HBox groupsHbox = new HBox();
	        	hbox.getChildren().add(groupsHbox);
	        	groupsHbox.setSpacing(5);
	        	groupsHbox.getChildren().add(new Text("G#"));
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
	        	s.valueProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> obs, Number oldval, Number newval) {
						item.send("/PI/gain", newval.floatValue(), 50f);
					}
				});
	        	hbox.getChildren().add(s);
	        	//a status string
	        	Text statusText = new Text("status unknown");
	        	hbox.getChildren().add(statusText);
	        	
	        	item.setGui(hbox);

        	}

        	setGraphic(item.getGui());
        }	

    }
	
	
	
}
