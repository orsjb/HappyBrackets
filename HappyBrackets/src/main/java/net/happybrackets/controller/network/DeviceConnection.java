/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.controller.network;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;

import com.sun.javafx.tk.Toolkit;
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
				incomingMessage(msg, sender);
			}
		});
		// create the OSC Server
		try {
			oscServer = OSCServer.newUsing(OSCServer.UDP, config.getStatusFromDevicePort());
			oscServer.start();
			logger.info("Created and started OSCServer for address {}", oscServer.getLocalAddress());
		} catch (IOException e) {
			logger.error("Error setting up new OSCServer!", e);
		}
		// set up to listen for basic messages
		oscServer.addOSCListener(new OSCListener() {
			@Override
			public void messageReceived(OSCMessage msg, SocketAddress source, long timestamp) {
				incomingMessage(msg, source);
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
			String[] line_split = line.trim().split("[ ]+");
			// Ignore blank or otherwise incorrectly formatted lines.
			if (line_split.length == 2) {
				logger.info("Adding known device mapping " + line_split[0] + " " + Integer.parseInt(line_split[1]));
				knownDevices.put(line_split[0], Integer.parseInt(line_split[1]));
			}
		}

		for (LocalDeviceRepresentation device: theDevices) {
			int id = 0;

			if (knownDevices.containsKey(device.hostName)) {
				device.setID(knownDevices.get(device.hostName));
			}
			else if (knownDevices.containsKey(device.deviceName)) {
				device.setID(knownDevices.get(device.deviceName));
			}
			else {
				device.setID(newID--);
			}

			new Thread() {
				public void run() {
					sendToDevice(device, "/device/set_id", device.getID());
					logger.info("Assigning id {} to {}", device.getID(), device.hostName);
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
			hostnames[i] = theDevices.get(i).hostName;
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

	private void incomingMessage(OSCMessage msg, SocketAddress sender) {
		if(msg.getName().equals("/device/alive")) {
			synchronized (this) {			//needs to be synchronized else we might put two copies in
				try {

					String device_name = (String) msg.getArg(0);
					String device_hostname = (String) msg.getArg(1);
					String device_address = (String) msg.getArg(2);
					logger.debug("Received message from device: " + device_name);
					//			System.out.println("Device Alive Message: " + deviceName);
					//see if we have this device yet
					LocalDeviceRepresentation this_device = devicesByHostname.get(device_name);

					logger.debug("Getting device from store: name=" + device_name + ", result=" + this_device);

					if (this_device == null) { //if not add it
						int id = 0;
						if (knownDevices.containsKey(device_name)) {
							id = knownDevices.get(device_name);
						} else {
							id = newID--;
						}
						//force names if useHostname is true
						if (config.useHostname()) {
							device_address = device_name;
						}

						this_device = new LocalDeviceRepresentation(device_name, device_hostname, device_address, id, oscServer, config);
						devicesByHostname.put(device_name, this_device);
						logger.debug("Put device in store: name=" + device_name + ", size=" + devicesByHostname.size());
						final LocalDeviceRepresentation device_to_add = this_device;
						//adding needs to be done in an "app" thread because it affects the GUI.
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								theDevices.add(device_to_add);
							}
						});
						//make sure this device knows its ID
						//since there is a lag in assigning an InetSocketAddress, and since this is the first
						//message sent to the device, it should be done in a separate thread.
						final LocalDeviceRepresentation device_id = this_device;
						new Thread() {
							public void run() {
								sendToDevice(device_id, "/device/set_id", device_id.getID());
								logger.info("Assigning id {} to {}", device_id.getID(), device_id.hostName);
							}
						}.start();
					}
					//keep up to date
					if (this_device != null) {
						// we need to update Host address
						// we will check that the InetAddress that we have stored is the same - IP address may have changed
						InetAddress sending_address = ((InetSocketAddress) sender).getAddress();
						this_device.setSocketAddress(sending_address);


						this_device.setIsConnected(true);
						this_device.lastTimeSeen = System.currentTimeMillis();    //Ultimately this should be "corrected time"

                        // Status is updated in GUI via listener
						if (msg.getArgCount() > 4) {
							String status = (String) msg.getArg(4);
							this_device.setStatus(status);
							//				System.out.println("Got status update from " + thisDevice.hostname + ": " + status);
						}
					}
				} catch (Exception e) {
					logger.error("Error reading incoming OSC message", e);
					return;
				}
			}
		}
//		logger.debug("Updated device list. Number of devices = " + devicesByHostname.size());
	}

	public void sendToDevice(LocalDeviceRepresentation device, String msg_name, Object... args) {
		device.send(msg_name, args);
	}


	public void sendToAllDevices(String msg_name, Object... args) {
		for(LocalDeviceRepresentation device : devicesByHostname.values()) {
			sendToDevice(device, msg_name, args);
		}
	}

	public void sendToDeviceList(Iterable<LocalDeviceRepresentation> devices, String msg_name, Object... args) {
		for (LocalDeviceRepresentation device : devices) {
			sendToDevice(device, msg_name, args);
		}
	}

	public void sendToDeviceList(String[] list, String msg_name, Object... args) {
		for(String deviceName : list) {
			sendToDevice(devicesByHostname.get(deviceName), msg_name, args);
		}
	}

	public void sendToDeviceGroup(int group, String msg_name, Object... args) {
		//send to group - group is defined by each LocalDeviceRep having group[i] flag
		for(LocalDeviceRepresentation device : theDevices) {
			if(device.groups[group]) {
				sendToDevice(device, msg_name, args);
			}
		}

	}

	private void checkDeviceAliveness() {
		long timeNow = System.currentTimeMillis();
		List<String> devices_to_remove = new ArrayList<String>();
		for(String deviceName : devicesByHostname.keySet()) {
			if(!deviceName.startsWith("Virtual Test Device")) {
				LocalDeviceRepresentation this_device = devicesByHostname.get(deviceName);
				long time_since_seen = timeNow - this_device.lastTimeSeen;
				if(time_since_seen > config.getAliveInterval() * 5) {	//config this number?
					//devicesToRemove.add(deviceName);
                    if (this_device.getIsConnected()) {
						this_device.setIsConnected(false);
                        // we need to invoke the update of GUI here
                    }
				}
			}
		}
		for(final String deviceName : devices_to_remove) {
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
		long time_now = System.currentTimeMillis();
		long time_to_sync = time_now + 5000;
		String time_as_string = "" + time_to_sync;
		sendToAllDevices("/device/sync", time_as_string);
	}

	public void deviceGain(float dest, float time_ms) {
		sendToAllDevices("/device/gain", dest, time_ms);
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
		LocalDeviceRepresentation virtual_test_device = new LocalDeviceRepresentation(name, hostname, address, 1, this.oscServer, this.config);
		theDevices.add(virtual_test_device);
		devicesByHostname.put(name, virtual_test_device);
	}

	/**
	 * Perform shutdown processes. Stops and disposes of the OSC server.
	 */
	public void dispose() {
		oscServer.dispose();
	}
}
