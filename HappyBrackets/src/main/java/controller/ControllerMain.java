package controller;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import controller.http.FileServer;
import core.LoadableConfig;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import controller.jfx_gui.GUIBuilder;
import controller.jfx_gui.PIRepCell;
import controller.launchpad.LaunchPad;
import controller.launchpad.LaunchPadBehaviour;
import controller.network.LocalPIRepresentation;
import controller.network.PIConnection;
import controller.network.SendToPI;
import core.ControllerAdvertiser;
import core.ControllerConfig;
import core.Synchronizer;

/**
 * MasterServer keeps contact with all PIs. Can control them etc.
 * Connects over OSC. This is kept entirely separate from the network synch tool, which only runs on the PIs.
 * 
 * @author ollie
 */

public class ControllerMain extends Application implements LaunchPadBehaviour {

	PIConnection piConnection;
	Synchronizer synchronizer;
	LaunchPad launchpad;
	String currentPIPO = "";
	protected ControllerConfig config;
	protected ControllerAdvertiser controllerAdvert;
    private FileServer httpServer;
	
    @Override 
    public void start(Stage stage) {
    	config = new ControllerConfig();
		config = LoadableConfig.load("config/controller-config.json", config);
    	piConnection = new PIConnection(config);
    	
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

        setupGUI(stage);
    	//test code...
//    	piConnection.createTestPI();
//    	piConnection.createTestPI();
//    	piConnection.createTestPI();
//    	piConnection.createTestPI();
//    	piConnection.createTestPI();
//    	piConnection.createTestPI();
    	synchronizer = Synchronizer.get();
    	//get normal desktop application behaviour - closing the stage terminates the app
    	stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	      @Override
	      public void handle (final WindowEvent event) {
	          System.exit(0);
	        }
	    });
    	//set up the LaunchPad
    	launchpad = new LaunchPad(new String[] {"Launchpad"}, this);
    	//set the colors
    	for(int i = 0; i < 8; i++) {
    		launchpad.setButtonColour(0, 0, i, LaunchPad.HIGH_AMBER);
    		launchpad.setButtonColour(0, i+1, 8, LaunchPad.HIGH_AMBER);
    		for(int j = 0; j < 8; j++) {
    			if(i == 0 && j % 2 == 0) {
    				launchpad.setButtonColour(0, i+1, j, LaunchPad.MEDIUM_RED);
    			} else {
    				launchpad.setButtonColour(0, i+1, j, LaunchPad.HIGH_GREEN);
    			}
    		}
    	}
    	launchpad.show();
    }
    
    
    
	private void setupGUI(Stage stage) {
		
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
    	GUIBuilder.createButtons(topBox, piConnection);
    	
    	
    	//list of PIs
		ListView<LocalPIRepresentation> list = new ListView<LocalPIRepresentation>();
    	list.setItems(piConnection.getPIs());
    	list.setCellFactory(new Callback<ListView<LocalPIRepresentation>, ListCell<LocalPIRepresentation>>() {
			@Override
			public ListCell<LocalPIRepresentation> call(ListView<LocalPIRepresentation> theView) {
				return new PIRepCell();
			}
		});
    	list.setMinWidth(1000);
    	list.setMaxWidth(1000);
    	list.setMinHeight(500);
       	border.setCenter(list);
       	//populate combobox with list of compositions
       	List<String> compositionFileNames = new ArrayList<String>();
       	Queue<File> dirs = new LinkedList<File>();
       	dirs.add(new File(config.getCompositionsPath()));
       	while (!dirs.isEmpty()) {
       	  for (File f : dirs.poll().listFiles()) {
       	    if (f.isDirectory()) {
       	      dirs.add(f);
       	    } else if (f.isFile()) {
       	    	String path = f.getPath();
       	    	path = path.substring(config.getCompositionsPath().length() + 1, path.length() - 6); // 6 equates to the length fo the .class extension, the + 1 is to remove path '/'
       	    	if(!path.contains("$")) {
           	    	System.out.println(path);
           	    	compositionFileNames.add(path);
       	    	}
       	    }
       	  }
       	}
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

       	Text sendCodetxt = new Text("Send PIPOs");
       	topBox.getChildren().add(sendCodetxt);
       	
       	HBox sendCodeHbox = new HBox();
       	sendCodeHbox.setSpacing(10);
       	topBox.getChildren().add(sendCodeHbox);
       	sendCodeHbox.getChildren().add(menu);
       	Button sendCode = new Button("Send >>");
       	sendCode.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
   				try {
   					SendToPI.send(currentPIPO, piConnection.getPIHostnames());
   				} catch (Exception ex) {
   					ex.printStackTrace();
   				}
			}
		});
       	sendCodeHbox.getChildren().add(sendCode);
       	topBox.getChildren().add(new Separator());
       	
       	
       	//set up the scene
        Scene scene = new Scene(masterGroup); 
        stage.setTitle("--PI Controller--"); 
        stage.setScene(scene); 
        stage.sizeToScene(); 
        stage.show(); 
    }

    @Override
    public void stop() throws Exception {
        //ensure our http server ends
        httpServer.stop();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

	@Override
	public void buttonAction(LaunchPad parent, int launchPadID, int row, int col, boolean push) {
//		System.out.println("Launchpad: " + parent + " " + launchPadID + " " + row + " " + col + " " + push);
		piConnection.sendToAllPIs("/launchpad", new Object[] {row, col, push?1:0});
	}
	
	
	
}