package net.happybrackets.controller;

import net.happybrackets.controller.gui.DeviceRepCell;
import net.happybrackets.controller.gui.GUIBuilder;
import net.happybrackets.controller.http.FileServer;
import net.happybrackets.controller.network.LocalDeviceRepresentation;
import net.happybrackets.controller.network.DeviceConnection;
import net.happybrackets.controller.network.SendToDevice;
import net.happybrackets.core.ControllerAdvertiser;
import net.happybrackets.core.ControllerConfig;
import net.happybrackets.core.LoadableConfig;
import net.happybrackets.core.Synchronizer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * MasterServer keeps contact with all PIs. Can control them etc.
 * Connects over OSC. This is kept entirely separate from the network synch tool, which only runs on the PIs.
 * 
 * @author ollie
 */

public class ControllerMain extends Application {

	DeviceConnection piConnection;
	Synchronizer synchronizer;
	String currentPIPO = "";
	protected ControllerConfig config;
	protected ControllerAdvertiser controllerAdvert;
    private FileServer httpServer;
	
    @Override 
    public void start(Stage stage) {
    	config = new ControllerConfig();
		config = LoadableConfig.load("config/controller-config.json", config);
    	piConnection = new DeviceConnection(config);
    	
    	//setup controller broadcast
    	try {
			controllerAdvert = new ControllerAdvertiser(config);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	controllerAdvert.start();

		//setup http httpServer
        try {
            httpServer = new FileServer(config);
        } catch (IOException e) {
            System.err.println("Unable to start http httpServer!");
            e.printStackTrace();
        }
        GUIBuilder guiBuilder = new GUIBuilder();
		guiBuilder.setupGUI(stage, piConnection, config);
    	//you can create a test pi if you don't have a real pi...
//    	piConnection.createTestPI();
    	synchronizer = Synchronizer.get();
    	//get normal desktop application behaviour - closing the stage terminates the app
    	stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	      @Override
	      public void handle (final WindowEvent event) {
	          System.exit(0);
	        }
	    });
    }

    @Override
    public void stop() throws Exception {
        //ensure our http server ends
        httpServer.stop();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
	
	
}