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

import net.happybrackets.core.OSCUDPReceiver;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.core.config.KnownDeviceID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceConnection {

	/**
	 * Define  an interface for detecting sending address of device alive messages
	 */
	public interface DeviceAliveListener{
		void deviceAlive(SocketAddress device_address);
	}

	final static Logger logger = LoggerFactory.getLogger(DeviceConnection.class);
	public static final boolean verbose = false;

	//private OSCServer oscServer;
	private OSCUDPReceiver oscServer;
	private ObservableList<LocalDeviceRepresentation> theDevices = FXCollections.observableArrayList(new ArrayList<LocalDeviceRepresentation>());
	private Map<String, LocalDeviceRepresentation> devicesByHostname = new Hashtable<String, LocalDeviceRepresentation>();
	private Map<String, KnownDeviceID> knownDevices = new Hashtable<String, KnownDeviceID>();
	// We will have a selected device based on project
	private Map<String, LocalDeviceRepresentation> selectedDevices = new Hashtable<String, LocalDeviceRepresentation>();


	private int newID = -1;
	private ControllerConfig config;
	private boolean loggingEnabled;

	private boolean showOnlyFavourites = false;


	public boolean isShowOnlyFavourites() {
		return showOnlyFavourites;
	}

	// create locks for our device lists
	private final Object devicesByHostnameLock = new Object();


	private List<DeviceAliveListener> deviceAliveListenerList = new ArrayList<>();

	private final Object deviceAliveListenerListLock = new Object();

	/**
	 * Add listener to see which IP address is live
	 * @param listener the listener
	 */
	public void addDeviceAliveListener(DeviceAliveListener listener){
		synchronized (deviceAliveListenerListLock){
			deviceAliveListenerList.add(listener);
		}
	}

	/**
	 * Get the LocalDevice  that is selected in this project
	 * @param project_hash the project
	 * @return the selected device if there. otherwise null
	 */
	public LocalDeviceRepresentation getSelectedDevice(String project_hash){
		LocalDeviceRepresentation ret = null;
		ret = selectedDevices.get(project_hash);

		return ret;
	}

	/**
	 * Insert the new selected device for the project
	 * @param project_hash the project we are in
	 * @param selected_device the new device. This can be null
	 */
	public void setDeviceSelected (String project_hash, LocalDeviceRepresentation selected_device){
		if (selectedDevices.containsKey(project_hash)) {
			selectedDevices.remove(project_hash);
		}

		if (selected_device != null) {
			selectedDevices.put(project_hash, selected_device);
		}
	}

	// If we only want to show favourites, we will also want to remove them from our list
	public void setShowOnlyFavourites(boolean enable) {
		if (showOnlyFavourites != enable) {
			this.showOnlyFavourites = enable;

			synchronized (favouritesChangedListeners){
				for(FavouritesChangedListener listener:  favouritesChangedListeners ){
					listener.isFavourite(showOnlyFavourites);
				}
			}

			// Now we will erase non favourites if we are set to true
			if (showOnlyFavourites){
				clearDevices(false);
			}
		}
	}




	List<DisableAdvertiseChangedListener> disabledAdvertiseListener = new ArrayList<>();
	List<FavouritesChangedListener> favouritesChangedListeners = new ArrayList<>();

	Set<String> favouriteDevices = new HashSet<>();

	public void addDisabledAdvertiserListener(DisableAdvertiseChangedListener listener){
		synchronized (disabledAdvertiseListener){
			disabledAdvertiseListener.add(listener);
		}

	}

	public void addFavouritesChangedListener(FavouritesChangedListener listener){
		synchronized (favouritesChangedListeners){
			favouritesChangedListeners.add(listener);
		}

	}

	//BroadcastManager broadcastManager;

	/**
	 * Get the reply port for the new OSC Server
	 * @return the Port created
	 */
	public int getReplyPort() {
		return replyPort;
	}

	int replyPort = 0; // this is the port we want the device to return calls to

	// flag to disable sending and receiving OSC
	private static boolean disableAdvertising = true;

	/**
	 * Remove all devices from List and make them become rescanned;
	 */
	public void rescanDevices() {
		setShowOnlyFavourites(false);
		clearDevices(true);
	}


	/**
	 * Remove the devices from list
	 * @param clear_favourites true if we are also removing favourite devices
	 */
	public void clearDevices(boolean clear_favourites){
		List<LocalDeviceRepresentation> devices_to_remove = new ArrayList<LocalDeviceRepresentation>();

		synchronized (devicesByHostnameLock) {
			for (LocalDeviceRepresentation device : devicesByHostname.values()) {
				if (device != null) {
					if (clear_favourites || !device.isFavouriteDevice()) {
						devices_to_remove.add(device);
					}
				}
			}

			for (LocalDeviceRepresentation device : devices_to_remove) {
				device.removeDevice();
			}

		}

	}

	public interface DisableAdvertiseChangedListener{
		void isDisabled(boolean disabled);
	}

	public interface FavouritesChangedListener{
		void isFavourite(boolean enabled);
	}

	public void startDeviceConnection(){
		try {



			oscServer = new OSCUDPReceiver(true); //OSCServer.newUsing(OSCServer.UDP);
			//replyPort = oscServer.getLocalAddress().getPort();

			replyPort = oscServer.getPort();
			logger.info("Created and started OSCServer for address {}", replyPort);
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
						logger.error("Poll interval interrupted for alive devices checkup", e);
					}
				}
			}
		}.start();

	}

	public DeviceConnection(ControllerConfig config) {
		this.config = config;
		//broadcastManager = broadcast;
		//replyPort = broadcast.getPort();


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

		/*
		broadcast.addPersistentBroadcastListener(new OSCListener() {
			@Override
			public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
				incomingMessage(msg, sender);
			}
		});
		*/

		// create the OSC Server

	}

	/**
	 * Flag if we are disabling OSC
	 * @return true if we are disabled
	 */
	public static final boolean getDisabledAdvertise(){
		return disableAdvertising;
	}

	public void setDisableAdvertise(boolean disable){
		disableAdvertising = disable;
		//broadcastManager.setDisableSend(disable);

		synchronized (disabledAdvertiseListener)
		{
			for (DisableAdvertiseChangedListener listener : disabledAdvertiseListener) {
				listener.isDisabled(disable);
			}
		}
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
			if (line_split.length >= 2) {

				KnownDeviceID device = KnownDeviceID.restore(line);

				if (device != null) {
					logger.info("Adding known device mapping " + device.getHostName() + " " + device.getDeviceId() + " " + device.getFriendlyName());
					knownDevices.put(device.getHostName(), device);
				}
			}
		}

		for (LocalDeviceRepresentation device: theDevices) {
			int id = 0;

			if (knownDevices.containsKey(device.hostName)) {
				device.setID(knownDevices.get(device.hostName).getDeviceId());
				device.setFriendlyName(knownDevices.get(device.hostName).getFriendlyName());
			}
			else if (knownDevices.containsKey(device.deviceName)) {
				device.setID(knownDevices.get(device.deviceName).getDeviceId());
				device.setFriendlyName(knownDevices.get(device.deviceName).getFriendlyName());
			}
			else {
				device.setID(newID--);
				device.setFriendlyName("");
			}

			new Thread() {
				public void run() {
					sendToDevice(device, OSCVocabulary.Device.SET_ID, device.getID());
					sendToDevice(device, OSCVocabulary.Device.SET_NAME, device.getFriendlyName());


					logger.info("Assigning id {} to {}", device.getID(), device.hostName);
				}
			}.start();
		}
	}

	/**
	 * Get the mapping of known device host names to device IDs. The returned map is not modifiable.
	 * @return Map of Known devices
	 */
	public Map<String, KnownDeviceID> getKnownDevices() {
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
            addresses[i] = theDevices.get(i).getAddress();
        }
        return addresses;
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
		final int DEVICE_SERVER_PORT = 4;

		try {
			boolean invalid_pi = false;

			InetAddress sending_address = ((InetSocketAddress) sender).getAddress();
			int device_id = 0;
			int device_server_port = 0;

			String device_name = (String) msg.getArg(DEVICE_NAME);

			// if we only want to look at our favourites, then ignore what we do not have as a favourite
			if (!showOnlyFavourites || favouriteDevices.contains(device_name)) {
				String device_hostname = (String) msg.getArg(DEVICE_HOSTNAME);
				String device_address = (String) msg.getArg(DEVICE_ADDRESS);
				//logger.debug("Received message from device: " + device_name);
				//			System.out.println("Device Alive Message: " + deviceName);
				//see if we have this device yet

				LocalDeviceRepresentation this_device = null;

				synchronized (devicesByHostnameLock) {
					this_device = devicesByHostname.get(device_name);
				}

				// we need to prevent overwriting an ID that has already been set
				if (msg.getArgCount() > DEVICE_ID) {
					try {
						device_id = (int) msg.getArg(DEVICE_ID);

					} catch (Exception ex) {
					}
				} else {
					invalid_pi = true;
				}

				if (msg.getArgCount() > DEVICE_SERVER_PORT) {
					try {
						device_server_port = (int) msg.getArg(DEVICE_SERVER_PORT);
					} catch (Exception ex) {
					}
				}

				//logger.debug("Getting device from store: name=" + device_name + ", result=" + this_device);

				if (this_device == null) { //if not add it

					//force names if useHostname is true
					if (config.useHostname()) {
						device_address = device_name;
					}

					String friendly_name = "";
					KnownDeviceID knownDeviceID = null;
					if (knownDevices.containsKey(device_name)) {
						knownDeviceID = knownDevices.get(device_name);
						device_id = knownDeviceID.getDeviceId();
						friendly_name = knownDeviceID.getFriendlyName();
					}

					if (device_id == 0) {
						if (knownDeviceID != null) {
							device_id = knownDeviceID.getDeviceId();

						} else {
							device_id = newID--;
						}
					}

					this_device = new LocalDeviceRepresentation(device_name, device_hostname, device_address, device_id, null, config, replyPort);
					this_device.setFriendlyName(friendly_name);

					if (favouriteDevices.contains(device_name)) {
						this_device.setFavouriteDevice(true);
					}

					this_device.addFavouriteListener(new LocalDeviceRepresentation.FavouriteChangedListener() {
						@Override
						public void favouriteChanged(LocalDeviceRepresentation device) {
							if (device.isFavouriteDevice()) {
								favouriteDevices.add(device.deviceName);
							} else {
								favouriteDevices.remove(device.deviceName);
							}
						}
					});
					this_device.addDeviceRemovedListener(new LocalDeviceRepresentation.DeviceRemovedListener() {
						@Override
						public void deviceRemoved(LocalDeviceRepresentation device) {

							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									synchronized (devicesByHostnameLock) {
										String device_name = device.deviceName;
										theDevices.remove(devicesByHostname.get(device_name));
										devicesByHostname.remove(device_name);
										logger.info("Removed Device from list: {}", device_name);
										device.sendConnectionListeners();
									}
								}
							});
						}
					});

					synchronized (devicesByHostnameLock) {
						devicesByHostname.put(device_name, this_device);
					}

					//logger.debug("Put device in store: name=" + device_name + ", size=" + devicesByHostname.size());
					final LocalDeviceRepresentation device_to_add = this_device;
					//adding needs to be done in an "app" thread because it affects the GUI.
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							theDevices.add(device_to_add);
							device_to_add.sendConnectionListeners();
						}
					});
					//make sure this device knows its ID
					//since there is a lag in assigning an InetSocketAddress, and since this is the first
					//message sent to the device, it should be done in a separate thread.
					this_device.setSocketAddress(sending_address);
					sendIdToDevice(this_device);

					// we can't do this here as the loading and unloading of the DeviceRepresentation cell
					//sendControlRequestToDevice(this_device);
				}

				//keep up to date
				if (this_device != null) {
					// we need to update Host address
					// we will check that the InetAddress that we have stored is the same - IP address may have changed

					if (!this_device.isIgnoringDevice()) {
						this_device.setServerPort(device_server_port);

						this_device.setSocketAddress(sending_address);

						// if we are localhost then we should make a tcp connection
						if(sending_address.isLoopbackAddress()){
							this_device.openClientPort(device_server_port);
						}

						// Make sure we don't have a zero device_id from device
						if (device_id != 0) {
							this_device.setID(device_id);
						} else if (invalid_pi) {
							this_device.setStatus("Invalid Version on Device PI");
						} else // we are not new but device does not have an ID
						{
							// Send Id to Device and Request status
							sendIdToDevice(this_device);

						}
						this_device.setIsConnected(true);
						this_device.lastTimeSeen = System.currentTimeMillis();    //Ultimately this should be "corrected time"
					}
				}
			} //if (!showOnlyFavourites || favouriteDevices.contains(device_name))
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
				local_device.sendVersionRequest();
			}
		}.start();
	}

	private void sendControlRequestToDevice(final LocalDeviceRepresentation local_device){
		new Thread() {
			public void run() {
				local_device.sendControlsRequest();
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

				synchronized (deviceAliveListenerListLock){
					for (DeviceAliveListener listener:deviceAliveListenerList) {
						listener.deviceAlive(sender);
					}
				}
			} else {
				final int DEVICE_NAME = 0;
				try {
					String device_name = (String) msg.getArg(DEVICE_NAME);
					synchronized (devicesByHostnameLock) {
						LocalDeviceRepresentation this_device = devicesByHostname.get(device_name);

						if (this_device != null) {

							if (!this_device.isIgnoringDevice()) {
								this_device.incomingMessage(msg, sender);
							}
						}
					}
				}
				catch (Exception ex){}
			}

		}
//		logger.debug("Updated device list. Number of devices = " + devicesByHostname.size());
	}



	public void sendToDevice(LocalDeviceRepresentation device, String msg_name, Object... args) {
		device.send(msg_name, args);
	}


	public void sendToAllDevices(String msg_name, Object... args) {
		synchronized (devicesByHostnameLock) {
			for (LocalDeviceRepresentation device : devicesByHostname.values()) {
				sendToDevice(device, msg_name, args);
			}
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

		synchronized (devicesByHostnameLock) {
			for (String deviceName : devicesByHostname.keySet()) {
				if (!deviceName.startsWith("Virtual Test Device")) {
					LocalDeviceRepresentation this_device = devicesByHostname.get(deviceName);
					if (!this_device.isIgnoringDevice()) {
						long time_since_seen = timeNow - this_device.lastTimeSeen;
						if (time_since_seen > config.getAliveInterval() * 5) {    //config this number?
							this_device.setIsConnected(false);
						}

					}
				}
			}
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
		LocalDeviceRepresentation virtual_test_device = new LocalDeviceRepresentation(name, hostname, address, 1, null, this.config, replyPort);
		theDevices.add(virtual_test_device);

		synchronized (devicesByHostnameLock) {
			devicesByHostname.put(name, virtual_test_device);
		}
	}

	/**
	 * Perform shutdown processes. Stops and disposes of the OSC server.
	 */
	public void dispose() {

		//oscServer.dispose();
	}
}
