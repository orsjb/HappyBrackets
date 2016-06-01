package net.happybrackets.controller.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.UnresolvedAddressException;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import net.happybrackets.controller.config.ControllerConfig;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;



public class LocalDeviceRepresentation {

	public long lastTimeSeen;
	public final String hostname;
	public final int id;
	private InetSocketAddress addr = null;
	private final OSCServer server;
	public final boolean[] groups;
	private ControllerConfig config;
	
	private String status = "Status unknown";
	
	Pane gui = null;
	
	public LocalDeviceRepresentation(String hostname, int id, OSCServer server, ControllerConfig config) {
		this.hostname = hostname;
		this.id = id;
		this.server = server;
		this.config = config;
		groups = new boolean[4];
	}

	public synchronized void send(String msgName, Object... args) {
		if(hostname.startsWith("Virtual Test PI")) {
			return;
		}
		OSCMessage msg = new OSCMessage(msgName, args);
		if(addr == null) {
			addr = new InetSocketAddress(hostname, config.getControlToDevicePort());
		}
		try {
			server.send(msg, addr);
		} catch (UnresolvedAddressException e) {
			System.out.println("Unable to send to PI: " + hostname);
			//e.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public Node getGui() {
		return gui;
	}

	public void setGui(Pane gui) {
		this.gui = gui;
	}

	public void setStatus(String arg) {
		status = arg;
		//modify gui
		if(gui != null) {
			Platform.runLater(new Runnable() {
				public void run() {
					//do on other thread
					//the last element is the status text box
					Text statusText = (Text)gui.getChildren().get(gui.getChildren().size() - 1);
					statusText.setText(status);
				}
			});
		}
	}
	
}
