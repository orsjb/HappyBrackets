package net.happybrackets.controller;

import javafx.scene.Scene;
import net.happybrackets.controller.gui.GUIManager;
import net.happybrackets.controller.http.FileServer;
import net.happybrackets.controller.network.DeviceConnection;
import net.happybrackets.core.ControllerAdvertiser;
import net.happybrackets.core.ControllerConfig;
import net.happybrackets.core.LoadableConfig;
import net.happybrackets.core.Synchronizer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.UnknownHostException;

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
        GUIManager guiManager = new GUIManager(config);
		Scene scene = guiManager.setupGUI(piConnection);
		stage.setTitle("PI Controller");
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
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