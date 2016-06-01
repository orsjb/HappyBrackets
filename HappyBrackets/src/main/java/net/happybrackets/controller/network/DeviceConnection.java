package net.happybrackets.controller.network;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.happybrackets.controller.config.ControllerConfig;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

public class DeviceConnection {
	
	public static final boolean verbose = false;
	
	OSCServer oscServer;
	ObservableList<LocalDeviceRepresentation> thePIs;
	Map<String, LocalDeviceRepresentation> pisByHostname;
	Map<String, Integer> knownPIs;
	int newID = -1;
	private ControllerConfig config;
	
	public DeviceConnection(ControllerConfig config) {
		this.config = config;
		thePIs = FXCollections.observableArrayList(new ArrayList<LocalDeviceRepresentation>());
		pisByHostname = new Hashtable<String, LocalDeviceRepresentation>();
		knownPIs = new Hashtable<String, Integer>();
		//read the known pis from file
		try {
			Scanner s = new Scanner(new File(config.getKnownDevicesFile()));
			while(s.hasNext()) {
				String[] line = s.nextLine().split("[ ]");
				knownPIs.put(line[0], Integer.parseInt(line[1]));
			}
			s.close();
		} catch (FileNotFoundException e1) {
			System.out.println("Unable to read '" + config.getKnownDevicesFile() + "'");
		}
		// create the OSC Server
		try {
			oscServer = OSCServer.newUsing(OSCServer.UDP, config.getStatusFromDevicePort());
			oscServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// set up to listen for basic messages
		oscServer.addOSCListener(new OSCListener() {
			@Override
			public void messageReceived(OSCMessage msg, SocketAddress source, long timestamp) {
				incomingMessage(msg);
			}
		});
		// set up thread to watch for lost PIs
		new Thread() {
			public void run() {
				while(true) {
					checkPIAliveness();
					try {
						Thread.sleep(config.getAliveInterval());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	public ObservableList<LocalDeviceRepresentation> getPIs() {
		return thePIs;
	}

	public String[] getPIHostnames() {
		String[] hostnames = new String[thePIs.size()];
		for(int i = 0; i < hostnames.length; i++) {
			hostnames[i] = thePIs.get(i).hostname;
		}
		return hostnames;
	}
	
	private void incomingMessage(OSCMessage msg) {
		if(msg.getName().equals("/PI/alive")) {
			String piName = (String)msg.getArg(0);
//			System.out.println("PI Alive Message: " + piName);
			//see if we have this PI yet
			LocalDeviceRepresentation thisPI = pisByHostname.get(piName);
			if(thisPI == null) { //if not add it
				int id = 0;
				if(knownPIs.containsKey(piName)) {
					id = knownPIs.get(piName);					
				} else {
					id = newID--;
				}
				thisPI = new LocalDeviceRepresentation(piName, id, oscServer, config);
	        	pisByHostname.put(piName, thisPI);
				final LocalDeviceRepresentation piToAdd = thisPI;
				//adding needs to be done in an "app" thread because it affects the GUI.
				Platform.runLater(new Runnable() {
			        @Override
			        public void run() {
			        	thePIs.add(piToAdd);
			        }
		        });
				//make sure this PI knows its ID
				//since there is a lag in assigning an InetSocketAddress, and since this is the first
				//message sent to the PI, it should be done in a separate thread.
				final LocalDeviceRepresentation piID = thisPI;
				new Thread() {
					public void run() {
						sendToPI(piID, "/PI/set_id", piID.id);		
						System.out.println("Assigning id " + piID.id + " to " + piID.hostname);
					}
				}.start();
			}
			//keep up to date
			thisPI.lastTimeSeen = System.currentTimeMillis();	//Ultimately this should be "corrected time"
			//TODO update the status in the GUI, not sure how to bind this
			if(msg.getArgCount() > 2) {
				String status = (String)msg.getArg(2);
				thisPI.setStatus(status);
//				System.out.println("Got status update from " + thisPI.hostname + ": " + status);
			}
		}
	}
	
	public void sendToPI(LocalDeviceRepresentation pi, String msgName, Object... args) {
		pi.send(msgName, args);
	}
	
	public void sendToAllPIs(String msgName, Object... args) {
		for(LocalDeviceRepresentation pi : pisByHostname.values()) {
			sendToPI(pi, msgName, args);
		}
	}
	
	public void sendToPIList(String[] list, String msgName, Object... args) {
		for(String piName : list) {
			sendToPI(pisByHostname.get(piName), msgName, args);
		}
	}
	
	public void sendToPIGroup(int group, String msgName, Object... args) {
		//send to group - group is defined by each LocalPIRep having group[i] flag
		for(LocalDeviceRepresentation pi : thePIs) {
			if(pi.groups[group]) {
				sendToPI(pi, msgName, args);
			}
		}
		
	}

	private void checkPIAliveness() {
		long timeNow = System.currentTimeMillis();
		List<String> pisToRemove = new ArrayList<String>();
		for(String piName : pisByHostname.keySet()) {
			if(!piName.startsWith("Virtual Test PI")) {
				LocalDeviceRepresentation thisPI = pisByHostname.get(piName);
				long timeSinceSeen = timeNow - thisPI.lastTimeSeen;
				if(timeSinceSeen > config.getAliveInterval() * 5) {	//config this number?
					pisToRemove.add(piName);
				}
			}
		}
		for(final String piName : pisToRemove) {
			//removal needs to be done in an "app" thread because it affects the GUI.
			Platform.runLater(new Runnable() {
		        @Override
		        public void run() {
					thePIs.remove(pisByHostname.get(piName));
					pisByHostname.remove(piName);
					System.out.println("Removed PI from list: " + piName);
		        }
		   });
		}
	}
	
	
	//standard messages to PI
	
	public void piReboot() {
		sendToAllPIs("/PI/reboot");
	}

	public void piShutdown() {
		sendToAllPIs("/PI/shutdown");
	}
	
	public void piSync() {
		long timeNow = System.currentTimeMillis();
		long timeToSync = timeNow + 5000;
		String timeAsString = "" + timeToSync;
		sendToAllPIs("/PI/sync", timeAsString);
	}
	
	public void piGain(float dest, float timeMS) {
		sendToAllPIs("/PI/gain", dest, timeMS);
	}
	
	public void piReset() {
		sendToAllPIs("/PI/reset");
	}

	public void piResetSounding() {
		sendToAllPIs("/PI/reset_sounding");
	}

	public void piClearSound() {
		sendToAllPIs("/PI/clearsound");
	}

	public void piFadeoutReset(float decay) {
		sendToAllPIs("/PI/fadeout_reset", decay);
	}

	public void piFadeoutClearsound(float decay) {
		sendToAllPIs("/PI/fadeout_clearsound", decay);
	}

	int virtualPICount = 1;
	
	public void createTestPI() {
		String name = "Virtual Test PI #" + virtualPICount++;
		LocalDeviceRepresentation virtualTestPI = new LocalDeviceRepresentation(name, 1, oscServer, config);
		thePIs.add(virtualTestPI);
		pisByHostname.put(name, virtualTestPI);
	}

	
}
