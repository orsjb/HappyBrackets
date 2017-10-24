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

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.happybrackets.controller.config.ControllerConfig;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

import net.happybrackets.core.BroadcastManager;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.core.control.ControlMap;
import net.happybrackets.core.control.DynamicControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceConnection {

	final static Logger logger = LoggerFactory.getLogger(DeviceConnection.class);
	public static final boolean verbose = false;

	private OSCServer oscServer;
	private ObservableList<LocalDeviceRepresentation> theDevices = FXCollections.observableArrayList(new ArrayList<LocalDeviceRepresentation>());
	private Map<String, LocalDeviceRepresentation> devicesByHostname = new Hashtable<String, LocalDeviceRepresentation>();;
	private Map<String, Integer> knownDevices = new Hashtable<String, Integer>();
	private int newID = -1;
	private ControllerConfig config;
	private boolean loggingEnabled;

	BroadcastManager broadcastManager;

	// flag to disable sending and receiving OSC
	private boolean disableAdvertising = false;

	public DeviceConnection(ControllerConfig config, BroadcastManager broadcast) {
		this.config = config;
		broadcastManager = broadcast;


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
	 * Flag if we are disabling OSC
	 * @return
	 */
	public final boolean getDisabledAdvertise(){
		return disableAdvertising;
	}

	public void setDisableAdvertise(boolean disable){
		disableAdvertising = disable;
		broadcastManager.setDisableSend(disable);
		logger.debug("OSC Advertising Disabled: " + disable);

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
					sendToDevice(device, OSCVocabulary.Device.SET_ID, device.getID());
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

	/**
	 * process alive message from device
	 * @param msg OSC Message
	 * @param sender the Socket Address of where the message originated
	 */
	private void processStatusMessage(OSCMessage msg, SocketAddress sender) {
		// Lets put some constants here so we can read them
		final int DEVICE_NAME = 0;
		final int DEVICE_STATUS = 1;

		try {
			String device_name = (String) msg.getArg(DEVICE_NAME);
			LocalDeviceRepresentation this_device = devicesByHostname.get(device_name);

			if (this_device != null) {
				String status = (String) msg.getArg(DEVICE_STATUS);
				this_device.setStatus(status);
			}

		}
		catch (Exception ex){}
	}

		/**
         * process alive message from device
         * @param msg OSC Message
         * @param sender the Socket Address of where the message originated
         */
	private void processAliveMessage(OSCMessage msg, SocketAddress sender) {
		// Lets put some constants here so we can read them
		final int DEVICE_NAME = 0;
		final int DEVICE_HOSTNAME = 1;
		final int DEVICE_ADDRESS = 2;
		final int DEVICE_ID = 3;
		try {
			boolean invalid_pi = false;

			InetAddress sending_address = ((InetSocketAddress) sender).getAddress();
			int device_id = 0;

			String device_name = (String) msg.getArg(DEVICE_NAME);
			String device_hostname = (String) msg.getArg(DEVICE_HOSTNAME);
			String device_address = (String) msg.getArg(DEVICE_ADDRESS);
			logger.debug("Received message from device: " + device_name);
			//			System.out.println("Device Alive Message: " + deviceName);
			//see if we have this device yet
			LocalDeviceRepresentation this_device = devicesByHostname.get(device_name);

			// we need to prevent overwriting an ID that has already been set
			if (msg.getArgCount() > DEVICE_ID) {
				try {
					device_id =  (int) msg.getArg(DEVICE_ID);

				} catch (Exception ex) {}
			}
			else
			{
				invalid_pi = true;
			}

			logger.debug("Getting device from store: name=" + device_name + ", result=" + this_device);

			if (this_device == null) { //if not add it

				//force names if useHostname is true
				if (config.useHostname()) {
					device_address = device_name;
				}

				if (device_id == 0) {
					if (knownDevices.containsKey(device_name)) {
						device_id = knownDevices.get(device_name);
					} else {
						device_id = newID--;
					}
				}

				this_device = new LocalDeviceRepresentation(device_name, device_hostname, device_address, device_id, oscServer, config);

				this_device.addDeviceRemovedListener(new LocalDeviceRepresentation.DeviceRemovedListener() {
					@Override
					public void deviceRemoved(LocalDeviceRepresentation device) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								String device_name = device.deviceName;
								theDevices.remove(devicesByHostname.get(device_name));
								devicesByHostname.remove(device_name);
								logger.info("Removed Device from list: {}", device_name);
							}
						});
					}
				});

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
				this_device.setSocketAddress(sending_address);
				sendIdToDevice(this_device);
				sendControlRequestToDevice(this_device);
			}

			//keep up to date
			if (this_device != null) {
				// we need to update Host address
				// we will check that the InetAddress that we have stored is the same - IP address may have changed

				this_device.setSocketAddress(sending_address);

				// Make sure we don't have a zero device_id from device
				if (device_id != 0) {
					this_device.setID(device_id);
				}
				else if (invalid_pi){
					this_device.setStatus("Invalid Version on Device PI");
				}
				else // we are not new but device does not have an ID
				{
					// Send Id to Device and Request status
					sendIdToDevice(this_device);

				}
				this_device.setIsConnected(true);
				this_device.lastTimeSeen = System.currentTimeMillis();    //Ultimately this should be "corrected time"


			}
		} catch (Exception e) {
			logger.error("Error reading incoming OSC message", e);
			return;
		}

	}

	private void sendIdToDevice(final LocalDeviceRepresentation local_device){
		new Thread() {
			public void run() {
				sendToDevice(local_device, OSCVocabulary.Device.SET_ID, local_device.getID());
				logger.info("Assigning id {} to {}", local_device.getID(), local_device.hostName);

				// now ask for status
				//local_device.send(OSCVocabulary.Device.STATUS);
				local_device.send(OSCVocabulary.Device.VERSION);
			}
		}.start();
	}

	private void sendControlRequestToDevice(final LocalDeviceRepresentation local_device){
		new Thread() {
			public void run() {
				local_device.send(OSCVocabulary.DynamicControlMessage.GET);
			}
		}.start();
	}
	/**
	 * The incoming OSC Message
	 * Must be synchronised to prevent multiple copies going in case we get a message in response to this and we are not ready for it yet
	 * @param msg OSC Message
	 * @param sender the Socket Address of where the message originated
	 */
	private synchronized void incomingMessage(OSCMessage msg, SocketAddress sender) {

		if (!disableAdvertising) {
			if (OSCVocabulary.match(msg, OSCVocabulary.Device.ALIVE)) {
				processAliveMessage(msg, sender);
			} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.STATUS)) {
				processStatusMessage(msg, sender);
			} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.VERSION)) {
				processVersionMessage(msg, sender);
			}
			else if (OSCVocabulary.startsWith(msg, OSCVocabulary.DynamicControlMessage.CONTROL))
			{
				processDynamicControlMessgage (msg, sender);
			}
		}
//		logger.debug("Updated device list. Number of devices = " + devicesByHostname.size());
	}

	private void processDynamicControlMessgage(OSCMessage msg, SocketAddress sender) {
		final int DEVICE_NAME = 0;
		final int CONTROL_HASH = 1;

		String device_name = (String) msg.getArg(DEVICE_NAME);
		int hash_code = (int) msg.getArg(CONTROL_HASH);

		LocalDeviceRepresentation this_device = devicesByHostname.get(device_name);

		try {
			if (this_device != null)
			{
				if (OSCVocabulary.match(msg, OSCVocabulary.DynamicControlMessage.CREATE)) {
					DynamicControl new_control = new DynamicControl(msg);
					this_device.addDynamicControl(new_control);
				}
				else if (OSCVocabulary.match(msg, OSCVocabulary.DynamicControlMessage.DESTROY)) {
					DynamicControl control = ControlMap.getInstance().getControl(hash_code);
					if (control != null)
					{
						control.eraseListeners();
						this_device.removeDynamicControl(control);
						ControlMap.getInstance().removeControl(control);
					}
				}
				else if (OSCVocabulary.match(msg, OSCVocabulary.DynamicControlMessage.UPDATE)) {
					// This call will send an update to all listeners
					DynamicControl.processUpdateMessage(msg);
				}


			}

		}catch (Exception ex){

		}

	}

	/**
	 *
	 * @param msg
	 * @param sender
	 */
	private void processVersionMessage(OSCMessage msg, SocketAddress sender) {
		final int DEVICE_NAME = 0;
		final int DEVICE_MAJOR = 1;
		final int DEVICE_MINOR = 2;
		final int DEVICE_BUILD = 3;
		final int DEVICE_DATE = 4;

		try {
			String device_name = (String) msg.getArg(DEVICE_NAME);
			LocalDeviceRepresentation this_device = devicesByHostname.get(device_name);

			if (this_device != null) {
				int major, minor, build, date;

				major =(int)msg.getArg(DEVICE_MAJOR);
				minor =  (int)msg.getArg(DEVICE_MINOR);
				build = (int)msg.getArg(DEVICE_BUILD);
				date = (int)msg.getArg(DEVICE_DATE);

				this_device.setVersion (major, minor, build, date);
			}

		}
		catch (Exception ex){}
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
		sendToAllDevices(OSCVocabulary.Device.REBOOT);
	}

	public void deviceShutdown() {
		sendToAllDevices(OSCVocabulary.Device.SHUTDOWN);
	}

	public void deviceSync() {
		long time_now = System.currentTimeMillis();
		long time_to_sync = time_now + 5000;
		String time_as_string = "" + time_to_sync;
		sendToAllDevices(OSCVocabulary.Device.SYNC, time_as_string);
	}

	public void deviceGain(float dest, float time_ms) {
		sendToAllDevices(OSCVocabulary.Device.GAIN, dest, time_ms);
	}

	public void deviceReset() {
		sendToAllDevices(OSCVocabulary.Device.RESET);
	}

	public void deviceResetSounding() {
		sendToAllDevices( OSCVocabulary.Device.RESET_SOUNDING);
	}

	public void deviceClearSound() {
		sendToAllDevices(OSCVocabulary.Device.CLEAR_SOUND);
	}

	public void deviceFadeoutReset(float decay) {
		sendToAllDevices( OSCVocabulary.Device.FADEOUT_RESET, decay);
	}

	public void deviceFadeoutClearsound(float decay) {
		sendToAllDevices(OSCVocabulary.Device.FADEOUT_CLEAR_SOUND, decay);
	}

	public void deviceEnableLogging(boolean enable) {
		loggingEnabled = enable;
		// Send as int because OSCPacketCodec.encodeMessage falls over if we try to send a boolean for some reason.
		sendToAllDevices(OSCVocabulary.Device.GET_LOGS, enable ? 1 : 0);
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
