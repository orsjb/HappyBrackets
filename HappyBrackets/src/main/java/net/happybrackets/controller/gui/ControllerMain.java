package net.happybrackets.controller.gui;

import javafx.scene.Scene;
import net.happybrackets.controller.gui.GUIManager;
import net.happybrackets.controller.http.FileServer;
import net.happybrackets.controller.network.DeviceConnection;
import net.happybrackets.controller.network.ControllerAdvertiser;
import net.happybrackets.controller.config.ControllerConfig;
import net.happybrackets.core.Device;
import net.happybrackets.core.BroadcastManager;
import net.happybrackets.core.config.LoadableConfig;
import net.happybrackets.core.Synchronizer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MasterServer keeps contact with all PIs. Can control them etc.
 * Connects over OSC. This is kept entirely separate from the network synch tool, which only runs on the PIs.
 *
 * @author ollie
 */

public class ControllerMain extends Application {

	final static Logger logger = LoggerFactory.getLogger(ControllerMain.class);

	DeviceConnection piConnection;
	Synchronizer synchronizer;
	BroadcastManager broadcastManager;
	String currentPIPO = "";
	protected ControllerConfig config;
	protected ControllerAdvertiser controllerAdvert;
    private FileServer httpServer;

    @Override
    public void start(Stage stage) {
			config = new ControllerConfig();
			config = LoadableConfig.load("config/controller-config.json", config);
	    if (!config.useHostname()) logger.info("Use host names is disabled");

	    //setup controller broadcast
			broadcastManager = new BroadcastManager(config.getMulticastAddr(), config.getBroadcastPort());
			controllerAdvert = new ControllerAdvertiser(broadcastManager);
	    controllerAdvert.start();

		piConnection = new DeviceConnection(config, broadcastManager);

			//setup http httpServer
	    try {
	        httpServer = new FileServer(config);
	    } catch (IOException e) {
	        logger.error("Unable to start http httpServer!", e);
	    }
	    GUIManager guiManager = new GUIManager(config);

			Scene scene = guiManager.setupGUI(piConnection);
			stage.setTitle("PI Controller");
			stage.setScene(scene);
			stage.sizeToScene();
			stage.show();
    	//you can create a test pi if you don't have a real pi...
//    	piConnection.createTestDevice();
    	synchronizer = Synchronizer.getInstance();
    	//getInstance normal desktop application behaviour - closing the stage terminates the app
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
