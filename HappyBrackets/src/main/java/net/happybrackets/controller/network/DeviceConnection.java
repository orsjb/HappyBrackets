package net.happybrackets.controller.network;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.*;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.happybrackets.controller.config.ControllerConfig;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

import net.happybrackets.core.BroadcastManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceConnection {

	final static Logger logger = LoggerFactory.getLogger(DeviceConnection.class);
	public static final boolean verbose = false;

	private OSCServer oscServer;
	private ObservableList<LocalDeviceRepresentation> theDevices;
	private Map<String, LocalDeviceRepresentation> devicesByHostname;
	private Map<String, Integer> knownDevices;
	private int newID = -1;
	private ControllerConfig config;
	private boolean loggingEnabled;

	public DeviceConnection(ControllerConfig config, BroadcastManager broadcast) {
		this.config = config;
		theDevices = FXCollections.observableArrayList(new ArrayList<LocalDeviceRepresentation>());
		devicesByHostname = new Hashtable<String, LocalDeviceRepresentation>();
		knownDevices = new Hashtable<String, Integer>();
		//read the known devices from file
		try {
			Scanner s = new Scanner(new File(config.getKnownDevicesFile()));
			List<String> lines = new ArrayList<>();
			while (s.hasNext()) {
				lines.add(s.nextLine());
			}
			s.close();
			setKnownDevices(lines.toArray(new String[0]));
		} catch (FileNotFoundException e1) {
			logger.error("Unable to read '{}'", config.getKnownDevicesFile());
		}

		broadcast.addBroadcastListener(new OSCListener() {
			@Override
			public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
				incomingMessage(msg);
			}
		});
		// create the OSC Server
		try {
			oscServer = OSCServer.newUsing(OSCServer.UDP, config.getStatusFromDevicePort());
			oscServer.start();
		} catch (IOException e) {
			logger.error("Error setting up new OSCServer!", e);
		}
		// set up to listen for basic messages
		oscServer.addOSCListener(new OSCListener() {
			@Override
			public void messageReceived(OSCMessage msg, SocketAddress source, long timestamp) {
				incomingMessage(msg);
			}
		});
		// set up thread to watch for lost devices
		new Thread() {
			public void run() {
				while(true) {
					checkDeviceAliveness();
					try {
						Thread.sleep(config.getAliveInterval());
					} catch (InterruptedException e) {
						logger.error("Poll interval interupted for alive devices checkup", e);
					}
				}
			}
		}.start();
	}

	/**
	 * Change the known devices mapping. This will re-assign IDs to all connected devices.
	 * @param lines Array of strings containing mappings from hostname to ID.
	 */
	public void setKnownDevices(String[] lines) {
		knownDevices.clear();
		newID = -1;

		for (String line : lines) {
			String[] lineSplit = line.trim().split("[ ]+");
			// Ignore blank or otherwise incorrectly formatted lines.
			if (lineSplit.length == 2) {
				logger.info("Adding known device mapping " + lineSplit[0] + " " + Integer.parseInt(lineSplit[1]));
				knownDevices.put(lineSplit[0], Integer.parseInt(lineSplit[1]));
			}
		}

		for (LocalDeviceRepresentation device: theDevices) {
			int id = 0;
			if(knownDevices.containsKey(device.hostname)) {
				device.setID(knownDevices.get(device.hostname));
			} else {
				device.setID(newID--);
			}

			new Thread() {
				public void run() {
					sendToDevice(device, "/device/set_id", device.getID());
					logger.info("Assigning id {} to {}", device.getID(), device.hostname);
				}
			}.start();
		}
	}

	/**
	 * Get the mapping of known device host names to device IDs. The returned map is not modifiable.
	 */
	public Map<String, Integer> getKnownDevices() {
		return Collections.unmodifiableMap(knownDevices);
	}

	public ObservableList<LocalDeviceRepresentation> getDevices() {
		return theDevices;
	}

	public String[] getDeviceHostnames() {
		String[] hostnames = new String[theDevices.size()];
		for(int i = 0; i < hostnames.length; i++) {
			hostnames[i] = theDevices.get(i).hostname;
		}
		return hostnames;
	}

    public String[] getDeviceAddresses() {
        String[] addresses = new String[theDevices.size()];
        for(int i = 0; i < addresses.length; i++) {
            addresses[i] = theDevices.get(i).address;
        }
        return addresses;
    }

	private void incomingMessage(OSCMessage msg) {
		if(msg.getName().equals("/device/alive")) {
			String deviceName       = (String)msg.getArg(0);
			String deviceHostname	= (String)msg.getArg(1);
			String deviceAddress    = (String)msg.getArg(2);
//			System.out.println("Device Alive Message: " + deviceName);
			//see if we have this device yet
			LocalDeviceRepresentation thisDevice = devicesByHostname.get(deviceName);
			if(thisDevice == null) { //if not add it
				int id = 0;
				if(knownDevices.containsKey(deviceName)) {
					id = knownDevices.get(deviceName);
				} else {
					id = newID--;
				}
				//force names if useHostname is true
                if (config.useHostname()) deviceAddress = deviceName;
				thisDevice = new LocalDeviceRepresentation(deviceName, deviceHostname, deviceAddress, id, oscServer, config);
	        	devicesByHostname.put(deviceName, thisDevice);
				final LocalDeviceRepresentation deviceToAdd = thisDevice;
				//adding needs to be done in an "app" thread because it affects the GUI.
				Platform.runLater(new Runnable() {
			        @Override
			        public void run() {
			        	theDevices.add(deviceToAdd);
			        }
		        });
				//make sure this device knows its ID
				//since there is a lag in assigning an InetSocketAddress, and since this is the first
				//message sent to the device, it should be done in a separate thread.
				final LocalDeviceRepresentation deviceID = thisDevice;
				new Thread() {
					public void run() {
						sendToDevice(deviceID, "/device/set_id", deviceID.getID());
						logger.info("Assigning id {} to {}", deviceID.getID(), deviceID.hostname);
					}
				}.start();
			}
			//keep up to date
			thisDevice.lastTimeSeen = System.currentTimeMillis();	//Ultimately this should be "corrected time"
			//TODO update the status in the GUI, not sure how to bind this
			if(msg.getArgCount() > 4) {
				String status = (String)msg.getArg(4);
				thisDevice.setStatus(status);
//				System.out.println("Got status update from " + thisDevice.hostname + ": " + status);
			}
		}
	}

	public void sendToDevice(LocalDeviceRepresentation device, String msgName, Object... args) {
		device.send(msgName, args);
	}

	public void sendToAllDevices(String msgName, Object... args) {
		for(LocalDeviceRepresentation device : devicesByHostname.values()) {
			sendToDevice(device, msgName, args);
		}
	}

	public void sendToDeviceList(String[] list, String msgName, Object... args) {
		for(String deviceName : list) {
			sendToDevice(devicesByHostname.get(deviceName), msgName, args);
		}
	}

	public void sendToDeviceGroup(int group, String msgName, Object... args) {
		//send to group - group is defined by each LocalDeviceRep having group[i] flag
		for(LocalDeviceRepresentation device : theDevices) {
			if(device.groups[group]) {
				sendToDevice(device, msgName, args);
			}
		}

	}

	private void checkDeviceAliveness() {
		long timeNow = System.currentTimeMillis();
		List<String> devicesToRemove = new ArrayList<String>();
		for(String deviceName : devicesByHostname.keySet()) {
			if(!deviceName.startsWith("Virtual Test Device")) {
				LocalDeviceRepresentation thisDevice = devicesByHostname.get(deviceName);
				long timeSinceSeen = timeNow - thisDevice.lastTimeSeen;
				if(timeSinceSeen > config.getAliveInterval() * 5) {	//config this number?
					devicesToRemove.add(deviceName);
				}
			}
		}
		for(final String deviceName : devicesToRemove) {
			//removal needs to be done in an "app" thread because it affects the GUI.
			Platform.runLater(new Runnable() {
		        @Override
		        public void run() {
					theDevices.remove(devicesByHostname.get(deviceName));
					devicesByHostname.remove(deviceName);
					logger.info("Removed Device from list: {}", deviceName);
		        }
		   });
		}
	}


	//standard messages to Device

	public void deviceReboot() {
		sendToAllDevices("/device/reboot");
	}

	public void deviceShutdown() {
		sendToAllDevices("/device/shutdown");
	}

	public void deviceSync() {
		long timeNow = System.currentTimeMillis();
		long timeToSync = timeNow + 5000;
		String timeAsString = "" + timeToSync;
		sendToAllDevices("/device/sync", timeAsString);
	}

	public void deviceGain(float dest, float timeMS) {
		sendToAllDevices("/device/gain", dest, timeMS);
	}

	public void deviceReset() {
		sendToAllDevices("/device/reset");
	}

	public void deviceResetSounding() {
		sendToAllDevices("/device/reset_sounding");
	}

	public void deviceClearSound() {
		sendToAllDevices("/device/clearsound");
	}

	public void deviceFadeoutReset(float decay) {
		sendToAllDevices("/device/fadeout_reset", decay);
	}

	public void deviceFadeoutClearsound(float decay) {
		sendToAllDevices("/device/fadeout_clearsound", decay);
	}

	public void deviceEnableLogging(boolean enable) {
		loggingEnabled = enable;
		// Send as int because OSCPacketCodec.encodeMessage falls over if we try to send a boolean for some reason.
		sendToAllDevices("/device/get_logs", enable ? 1 : 0);
	}
	public boolean isDeviceLoggingEnabled() {
		return loggingEnabled;
	}


	int virtualDeviceCount = 1;

	public void createTestDevice() {
		String name     = "Virtual Test Device #" + virtualDeviceCount++;
		String hostname = "myHostname!";
        String address  = "127.0.0.1";
		LocalDeviceRepresentation virtualTestDevice = new LocalDeviceRepresentation(name, hostname, address, 1, oscServer, config);
		theDevices.add(virtualTestDevice);
		devicesByHostname.put(name, virtualTestDevice);
	}

	/**
	 * Perform shutdown processes. Stops and disposes of the OSC server.
	 */
	public void dispose() {
		oscServer.dispose();
	}
}
