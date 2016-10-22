package net.happybrackets.controller.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.UnresolvedAddressException;
import java.util.ArrayList;
import java.util.List;

import de.sciss.net.OSCListener;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import net.happybrackets.controller.config.ControllerConfig;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalDeviceRepresentation {

	final static Logger logger = LoggerFactory.getLogger(LocalDeviceRepresentation.class);

	public long lastTimeSeen;
	public final String hostname;
	public final String address;
	private int id;
	private InetSocketAddress socket;
	private final OSCServer server;
	public final boolean[] groups;
	private ControllerConfig config;

	public interface StatusUpdateListener {
		public void update(String state);
	}

	private List<StatusUpdateListener> statusUpdateListenerList;


	private String log;
	public interface LogListener {
		public void newLogMessage(String message);
	}
	private List<LogListener> logListenerList;



	private String status = "Status unknown";

	public LocalDeviceRepresentation(String hostname, String addr, int id, OSCServer server, ControllerConfig config) {

		this.hostname   					= hostname;
    	this.address    					= addr;
		this.socket     					= new InetSocketAddress(addr, config.getControlToDevicePort());
		this.id         					= id;
		this.server     					= server;
		this.config     					= config;
		groups          					= new boolean[4];
		statusUpdateListenerList  = new ArrayList<>();
		logListenerList = new ArrayList<>();

		// Set-up log monitor.
		log = "";
		server.addOSCListener(new OSCListener() {
			@Override
			public void messageReceived(OSCMessage msg, SocketAddress source, long timestamp) {
				if (msg.getName().equals("/device/log") && ((Integer) msg.getArg(0)) == id) {
					String newLogOutput = (String) msg.getArg(1);

					log = log + "\n" + newLogOutput;

					logger.debug("Received new log output from device {} ({}): {}", hostname, id, newLogOutput);

					for (LogListener listener : logListenerList) {
						listener.newLogMessage(newLogOutput);
					}
				}
			}
		});
	}

	public void setID(int id) {
		this.id = id;
	}

	public int getID() {
		return id;
	}

	public synchronized void send(String msgName, Object... args) {
		if(hostname.startsWith("Virtual Test Device")) {
			return;
		}
		OSCMessage msg = new OSCMessage(msgName, args);
		if(socket == null) {
			socket = new InetSocketAddress(hostname, config.getControlToDevicePort()); //TODO this could be problematic
		}
		try {
			server.send(msg, socket);	//TODO this may need a solution like BroadcastManger?
		} catch (UnresolvedAddressException e) {
			logger.error("Unable to send to Device: {}", hostname, e);
		} catch (IOException e) {
			logger.error("Error sending to device!", e);
		}
	}

	public void addStatusUpdateListener(StatusUpdateListener listener) {
		statusUpdateListenerList.add(listener);
	}

	public void setStatus(String arg) {
		status = arg;
		for(StatusUpdateListener statusUpdateListener : statusUpdateListenerList) {
			statusUpdateListener.update(status);
		}
	}

	public void addLogListener(LogListener listener) {
		logListenerList.add(listener);
	}
	public void removeLogListener(LogListener listener) {
		logListenerList.remove(listener);
	}

	public String getDeviceLog() {
		return log;
	}
}
