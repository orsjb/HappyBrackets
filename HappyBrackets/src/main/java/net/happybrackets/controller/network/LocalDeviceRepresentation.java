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

import java.io.IOException;
import java.net.*;
import java.nio.channels.UnresolvedAddressException;
import java.util.*;

import net.happybrackets.controller.config.ControllerConfig;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

import net.happybrackets.controller.gui.DynamicControlScreen;
import net.happybrackets.core.BuildVersion;
import net.happybrackets.core.DeviceStatus;
import net.happybrackets.core.ErrorListener;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.core.control.ControlMap;
import net.happybrackets.core.control.DynamicControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalDeviceRepresentation {

	final int MILLISECONDS_TO_REQUEST_CONTROLS = 2000;

	public static final int MAX_LOG_DISPLAY_CHARS = 5000;

	final static Logger logger = LoggerFactory.getLogger(LocalDeviceRepresentation.class);

	private long timeDisplayed; // we will set the thime this device was displayed
	public long lastTimeSeen;
	public final String deviceName;
	public final String hostName;

	private String friendlyName = "";

	private String address;
	public List<String> preferredAddressStrings;    //This list contains, in order of preference: address, hostName, deviceName, hostname.local or deviceName.local.
	private int deviceId; //
	private String status = "Status unknown"; // This is the displayed ID

	private DynamicControlScreen dynamicControlScreen = null;

	boolean controlRequestSent = false;

	private InetSocketAddress socketAddress;

	private final OSCServer server;
	public final boolean[] groups;
	private ControllerConfig controllerConfig;
	private final int replyPort; // this is the port where we will tell devices to send our messages
	private final Object[] replyPortObject; //we will use this as a cached Object to send in OSC Message
	private boolean loggingEnabled = false;

	private Map<String, DynamicControl> dynamicControls = new Hashtable<String, DynamicControl>();

	private boolean isConnected = true;
	private boolean ignoreDevice = false;
	private boolean isFavouriteDevice = false;
	private boolean encryptionEnabled = false;


	final Object dynamicControlLock = new Object();

	public boolean isEncryptionEnabled() {
		return encryptionEnabled;
	}

	public void setEncryptionEnabled(boolean enabled) {
		encryptionEnabled = enabled;
		if (encryptionEnabled){
			setStatus("Encryption Enabled");
		}
		else
		{
			setStatus("Encryption Disabled");
		}
		send(OSCVocabulary.Device.SET_ENCRYPTION, new Object[]{enabled ? 1 : 0});
	}


	/**
	 * Get the Address we use to access this device over the network
	 * @return the network Address
	 */
	public String getAddress() {
		return address;
	}


	/**
	 * get The friendly name we want to display this device as
	 * @return the friendly name. If not set, will return device name
	 */
	public String getFriendlyName() {
		String ret = friendlyName;
		if (friendlyName.isEmpty())
		{
			ret = deviceName;
		}
		return ret;
	}

	/**
	 * Set the friendly name for this device
	 * @param friendlyName THe name we want to have this device displayed as
	 */
	public void setFriendlyName(String friendlyName) {
		boolean changed = !friendlyName.equals(this.friendlyName);

		if (changed) {
			this.friendlyName = friendlyName;
			synchronized (friendlyNameListenerList) {
				for (StatusUpdateListener listener : friendlyNameListenerList) {
					listener.update(getFriendlyName());
				}
			}


		}
	}


	/**
	 * If true, we will ignore this device and not respond to any of its messages
	 * @return true if we are ignoring
	 */
	public boolean isIgnoringDevice() {
		return ignoreDevice;
	}

	/**
	 * Set whether we will ignore this device. We will set status of device
	 * @param ignoreDevice true if we want to ignore
	 */
	public void setIgnoreDevice(boolean ignoreDevice) {
		this.ignoreDevice = ignoreDevice;
		if (ignoreDevice){
			setStatus("Ignore Device");
		}
		else
		{
			setStatus("Stoped Ignoring");
			sendStatusRequest();
		}
	}




	public boolean isFavouriteDevice() {
		return isFavouriteDevice;
	}


	public void setFavouriteDevice(boolean enabled) {
		if (isFavouriteDevice != enabled) {
			this.isFavouriteDevice = enabled;
			synchronized (favouriteChangedListeners) {
				for (FavouriteChangedListener deviceModifiedListener : favouriteChangedListeners) {
					deviceModifiedListener.favouriteChanged(this);
				}
			}
		}
	}


	public boolean getIsConnected() {
		return this.isConnected;
	}

	/**
	 * If our major and minor version do not match plugin, we will be an invalid version
	 * @return true if plugin and device do not match major and minor
	 */
	public boolean isInvalidVersion() {
		return invalidVersion;
	}

	boolean invalidVersion = false;

	private int majorVersion = 0;
	private int minorVersion = 0;
	private int buildVersion = 0;
	private int dateVersion = 0;

	/**
	 * Get the HB Version of this device as a string
	 * @return Device HB Version
	 */
	public String getVersionText(){
		return majorVersion + "." + minorVersion + "." + buildVersion + "." + dateVersion;
	}

	/**
	 * Return a message to display to the user that their device veriosn is incompatible
	 * with plugin
	 * @return warning message
	 */
	public String getInvalidVersionWarning(){
		return  "Invalid device version. Device has "  + getVersionText() + ". Must be " + BuildVersion.getMinimumCompatibilityVersion();
	}

	public void setVersion(int major, int minor, int build, int date) {
		majorVersion = major;
		minorVersion = minor;
		buildVersion = build;
		dateVersion = date;

		if (BuildVersion.getMajor() != majorVersion ||
				BuildVersion.getMinor() != minorVersion)
		{
			invalidVersion = true;
		}


		String status_text = "V: " + getVersionText();
		setStatus(status_text);
	}


	public interface StatusUpdateListener {
		void update(String state);
	}

	public interface ConnectedUpdateListener {
		void update(boolean connected);
	}

	public interface DeviceIdUpdateListener {
		void update(int new_id);
	}

	public interface SocketAddressChangedListener {
		void socketChanged(InetAddress old_address, InetAddress inet_address);
	}

	public interface DeviceRemovedListener {
		void deviceRemoved(LocalDeviceRepresentation device);
	}

	public interface FavouriteChangedListener {
		void favouriteChanged(LocalDeviceRepresentation device);
	}

	private List<StatusUpdateListener> statusUpdateListenerList = new ArrayList<>();
	private List<StatusUpdateListener> friendlyNameListenerList = new ArrayList<>();

	private List<ConnectedUpdateListener> connectedUpdateListenerList = new ArrayList<>();
	private List<ConnectedUpdateListener> loggingStateListener = new ArrayList<>();
	private List<SocketAddressChangedListener> socketAddressChangedListenerList = new ArrayList<>();
	private List<DeviceIdUpdateListener> deviceIdUpdateListenerList = new ArrayList<>();
	private List<DeviceRemovedListener> deviceRemovedListenerList = new ArrayList<>();
	private List<FavouriteChangedListener> favouriteChangedListeners = new ArrayList<>()
;
	private List<DynamicControl.DynamicControlListener> addDynamicControlListenerList = new ArrayList<>();
	private List<DynamicControl.DynamicControlListener> removeDynamicControlListenerList = new ArrayList<>();

	private List<ErrorListener> errorListenerList = new ArrayList<>();

	private String currentLogPage = "";
	private ArrayList<String> completeLog = new ArrayList<String>();

	public interface LogListener {
		void newLogMessage(String message, int page);
	}

	private List<LogListener> logListenerList = new ArrayList<>();

	public void addDynamicControlListenerCreatedListener(DynamicControl.DynamicControlListener listener) {
		synchronized (addDynamicControlListenerList) {
			addDynamicControlListenerList.add(listener);
		}
	}

	public void removeDynamicControlListenerCreatedListener(DynamicControl.DynamicControlListener listener) {
		synchronized (addDynamicControlListenerList) {
			addDynamicControlListenerList.remove(listener);
		}
	}

	public void addDynamicControlListenerRemovedListener(DynamicControl.DynamicControlListener listener) {
		synchronized (removeDynamicControlListenerList) {
			removeDynamicControlListenerList.add(listener);
		}
	}

	public void addFavouriteListener(FavouriteChangedListener listener){
		synchronized (favouriteChangedListeners)
		{
			favouriteChangedListeners.add(listener);
		}
	}


	public void removeDynamicControlListenerRemovedListener(DynamicControl.DynamicControlListener listener) {
		synchronized (removeDynamicControlListenerList) {
			removeDynamicControlListenerList.add(listener);
		}
	}

	void sendInitialControlRequest(){
		if (!controlRequestSent && timeActive() > MILLISECONDS_TO_REQUEST_CONTROLS)
		{
			controlRequestSent = true;
			sendControlsRequest();
		}
	}


	public void showControlScreen()
	{
		sendInitialControlRequest();
		dynamicControlScreen.show();
	}

	/**
	 * Return the time in milliseconds that we have had this appeared
	 * @return The time the device has been active in our list
	 */
	public long timeActive()
	{
		return System.currentTimeMillis() - timeDisplayed;
	}
	/**
	 * Add A dynamic Control
	 *
	 * @param control The DynamicControl we are making
	 */
	public void addDynamicControl(DynamicControl control) {
		synchronized (dynamicControlLock) {
			dynamicControls.put(control.getControlMapKey(), control);
			dynamicControlScreen.addDynamicControl(control);

			synchronized (addDynamicControlListenerList) {
				for (DynamicControl.DynamicControlListener listener : addDynamicControlListenerList) {
					listener.update(control);
				}
			}
			control.addControlListener(new DynamicControl.DynamicControlListener() {
				@Override
				public void update(DynamicControl control) {

					System.out.println("Dynamic Control value changed");
				}
			});

			// add listener so we will send changes that occu to control back to device
			control.addValueSetListener(new DynamicControl.DynamicControlListener() {
				@Override
				public void update(DynamicControl control) {
					sendDynamicControl(control);
				}
			});
		}
	}

	/**
	 * Reset the device and clear dynamic controls
	 */
	public void resetDevice() {
		this.send(OSCVocabulary.Device.RESET);
		clearDynamicControls();
	}

	/**
	 * We need to remove all dynamic controls From This device
	 */
	public void clearDynamicControls() {
		// we need to get the collection synchronised with map
		// or we will get an access vioaltion

		dynamicControlScreen.eraseDynamicControls();
		Collection<DynamicControl> removal_list;
		synchronized (dynamicControlLock) {
			removal_list = dynamicControls.values();
		}

		for (DynamicControl control : removal_list) {
			control.eraseListeners();
			removeDynamicControl(control);
		}
	}

	/**
	 * Remove A dynamic Control
	 * @param control The DynamicControl we are removing
	 */
	public void removeDynamicControl(DynamicControl control) {
		synchronized (dynamicControlLock) {
			dynamicControls.remove(control.getControlMapKey());

			dynamicControlScreen.removeDynamicControl(control);

			synchronized (removeDynamicControlListenerList) {
				for (DynamicControl.DynamicControlListener listener : removeDynamicControlListenerList) {
					listener.update(control);
				}
			}
		}
	}


	// Overload constructors. Construct with a SocketAddress
	public LocalDeviceRepresentation(String deviceName, String hostname, String addr, int id, OSCServer server, ControllerConfig config, InetSocketAddress socketAddress, int reply_port) {
		this(deviceName, hostname, addr, id, server, config, reply_port);
		this.socketAddress = socketAddress;
	}

	public LocalDeviceRepresentation(String deviceName, String hostname, String addr, int id, OSCServer server, ControllerConfig config, int reply_port) {

		//timeCreated = System.nanoTime();
		// We will set timeDisplayed so it will not make a request for a control until it has been set by the display Cell
		timeDisplayed = System.currentTimeMillis();
		replyPort = reply_port;
		replyPortObject = new Object[] {replyPort};
		this.deviceName = deviceName;
		this.hostName = hostname;
		this.address = addr;
		this.socketAddress = null;
		this.deviceId = id;
		this.server = server;
		this.controllerConfig = config;
		groups = new boolean[4];


		this.isConnected = true;

		dynamicControlScreen = new DynamicControlScreen(this);

		dynamicControlScreen.createDynamicControlStage();

		// Set-up log monitor.
		currentLogPage = "";
		/*
		server.addOSCListener(new OSCListener() {
			@Override
			public void messageReceived(OSCMessage msg, SocketAddress source, long timestamp) {
				if (OSCVocabulary.match(msg, OSCVocabulary.Device.LOG) && ((Integer) msg.getArg(0)) == id) {
					String new_log_output = (String) msg.getArg(1);
					log = log + "\n" + new_log_output;
					logger.debug("Received new log output from device {} ({}): {}", hostname, id, new_log_output);
					for (LogListener listener : logListenerList) {
						listener.newLogMessage(new_log_output);
					}
				}
			}
		});*/
	}


	/**
	 * Notifiy Device that it has been displayed and we can start any functions that required the item to be displayed
	 */
	public void setDeviceHasDisplayed(){
		timeDisplayed = System.currentTimeMillis();
	}

	public void resetDeviceHasDisplayed(){
		timeDisplayed = System.currentTimeMillis();
	}
	/**
	 * Process and incoming OSC Message for this device
	 *
	 * @param msg    the OSC Message
	 * @param sender Socket address of sender
	 */
	public synchronized void incomingMessage(OSCMessage msg, SocketAddress sender) {
		if (OSCVocabulary.match(msg, OSCVocabulary.Device.STATUS)) {
			processStatusMessage(msg, sender);
		} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.VERSION)) {
			processVersionMessage(msg, sender);

		} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.FRIENDLY_NAME)) {
			//processFriendlyNameMessage(msg, sender);

		} else if (OSCVocabulary.startsWith(msg, OSCVocabulary.DynamicControlMessage.CONTROL)) {
			processDynamicControlMessage(msg, sender);
		} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.LOG)) {
			processLogMessage(msg, sender);
		}

	}


	private synchronized void processLogMessage(OSCMessage msg, SocketAddress sender) {
		String new_log_output = (String) msg.getArg(1);

		// see if our new logpage will exceed our max log page size
		if (currentLogPage.length() + new_log_output.length() > MAX_LOG_DISPLAY_CHARS && currentLogPage.length() != 0) {
			completeLog.add(currentLogPage);
			currentLogPage = new String(new_log_output);
		}
		else {
			currentLogPage = currentLogPage + "\n" + new_log_output;
		}


		//logger.debug("Received new log output from device {} ({}): {}", deviceName, socketAddress, new_log_output);
		for (LogListener listener : logListenerList) {
			listener.newLogMessage(new_log_output, numberLogPages() - 1);
		}
	}

	/**
	 * process alive message from device
	 *
	 * @param msg    OSC Message
	 * @param sender the Socket Address of where the message originated
	 */
	private void processStatusMessage(OSCMessage msg, SocketAddress sender) {
		// Lets put some constants here so we can read them
		DeviceStatus status = new DeviceStatus(msg);

		setStatus(status.getStatusText());
		if (loggingEnabled != status.isLoggingEnabled()) {
			loggingEnabled = status.isLoggingEnabled();
			try {
				for (ConnectedUpdateListener listener : loggingStateListener) {
					listener.update(loggingEnabled);
				}
			}catch (Exception ex){}
		}

		encryptionEnabled = status.isClassEncryption();
	}

	/**
	 * Process the Build Version message of this device
	 * @param msg OSC Message
	 * @param sender Socket address of sender
	 */
	private void processVersionMessage(OSCMessage msg, SocketAddress sender) {
		final int DEVICE_NAME = 0;
		final int DEVICE_MAJOR = 1;
		final int DEVICE_MINOR = 2;
		final int DEVICE_BUILD = 3;
		final int DEVICE_DATE = 4;

		int major, minor, build, date;

		major = (int) msg.getArg(DEVICE_MAJOR);
		minor = (int) msg.getArg(DEVICE_MINOR);
		build = (int) msg.getArg(DEVICE_BUILD);
		date = (int) msg.getArg(DEVICE_DATE);

		setVersion(major, minor, build, date);
	}

	/**
	 * Process the Build Version message of this device
	 * @param msg OSC Message
	 * @param sender Socket address of sender
	 */
	private void processFriendlyNameMessage(OSCMessage msg, SocketAddress sender) {
		final int DEVICE_NAME = 0;
		final int NAME = 1;


		String name = (String)msg.getArg(NAME);

		setFriendlyName(name);
	}



	/**
	 * Process Messages with Dynamic Control
	 * @param msg OSC Message
	 * @param sender socket addrss of sender
	 */
	private void processDynamicControlMessage(OSCMessage msg, SocketAddress sender) {
		final int CONTROL_MAP_KEY = 1;
		String map_key = (String) msg.getArg(CONTROL_MAP_KEY);

		if (OSCVocabulary.match(msg, OSCVocabulary.DynamicControlMessage.CREATE)) {
			DynamicControl new_control = new DynamicControl(msg);
				addDynamicControl(new_control);
		} else if (OSCVocabulary.match(msg, OSCVocabulary.DynamicControlMessage.DESTROY)) {
			DynamicControl control = ControlMap.getInstance().getControl(map_key);
			if (control != null) {
				control.eraseListeners();
				removeDynamicControl(control);
				ControlMap.getInstance().removeControl(control);
			}
		} else if (OSCVocabulary.match(msg, OSCVocabulary.DynamicControlMessage.UPDATE)) {
			// This call will send an update to all listeners
			DynamicControl.processUpdateMessage(msg);
		}

	}


	/**
	 * We will recieve this message and send the dynamic control message back to the device
	 * @param dynamic_control The Dynamic Control
	 */
	void sendDynamicControl(DynamicControl dynamic_control){
		OSCMessage msg = dynamic_control.buildUpdateMessage();
		sendOscMsg(msg);
	}

	public final InetSocketAddress getSocketAddress()
	{
		return this.socketAddress;
	}


	// First test if our stored socket address is the same as the argument
	// If it is different, store new value and raise event to notify that change occurred
	public void setSocketAddress(InetAddress new_socket_address)
	{
		InetAddress old = null;

		String old_host_address = "";

		if (this.socketAddress != null) {
			old = this.socketAddress.getAddress();
			old_host_address = old.getHostAddress();
		}

		String new_host_address = new_socket_address.getHostAddress();
		boolean same_address = old_host_address.equals(new_host_address);
		if (!same_address)
		{
			this.address = new_host_address;
			this.socketAddress = new InetSocketAddress(new_socket_address, controllerConfig.getControlToDevicePort());

			synchronized (socketAddressChangedListenerList) {
				// now raise event
				for (SocketAddressChangedListener listener : socketAddressChangedListenerList) {
					listener.socketChanged(old, new_socket_address);
				}
			}
		}
	}

	/**
	 * Lets device that know about this to remove their listeners
	 * It then removes all listeners from list
	 */
	public void removeDevice() {
		synchronized (deviceRemovedListenerList) {
			for (DeviceRemovedListener listener : deviceRemovedListenerList) {
				listener.deviceRemoved(this);
			}
			// Just because a device is removed does not mean it is no longer a favourite
			favouriteChangedListeners.clear();
			deviceRemovedListenerList.clear();
			dynamicControlScreen.removeDynamicControlScene();
			dynamicControlScreen = null;

		}
	}

	/**
	 * Set the device Id of the this. If it has changed, it will notify any listeners
	 * @param id The ID set for this device
	 */
	public void setID(int id) {
		boolean changed = this.deviceId != id;
		this.deviceId = id;

		if (changed) {
			for (DeviceIdUpdateListener listener: deviceIdUpdateListenerList){
				listener.update(id);
			}
		}

	}

	public int getID() {
		return deviceId;
	}


	/**
	 * Send a request to get version number of HappyBrackets from device
	 */
	public void sendVersionRequest(){
		send(OSCVocabulary.Device.VERSION, replyPortObject);
		send(OSCVocabulary.Device.FRIENDLY_NAME, replyPortObject);
	}

	/**
	 * Send a request to get the dynamic controls on this device
	 */
	public void sendControlsRequest(){
		clearDynamicControls();
		send(OSCVocabulary.DynamicControlMessage.GET, replyPortObject);
	}

	/**
	 * Send a status request to the device
	 */
	public void sendStatusRequest(){
		send(OSCVocabulary.Device.STATUS, replyPortObject);
	}

	private void lazySetupAddressStrings() {
		if(preferredAddressStrings == null) {
			preferredAddressStrings = new LinkedList<>();
			preferredAddressStrings.add(deviceName + ".local");
			preferredAddressStrings.add(address);
			preferredAddressStrings.add(hostName + ".local");
			preferredAddressStrings.add(hostName);
			preferredAddressStrings.add(deviceName);
		}
	}

	public synchronized void sendOscMsg(OSCMessage msg)
	{
		lazySetupAddressStrings();
		boolean success = false;
		int count = 0;
		while(!success) {
			try {
				if (this.socketAddress == null) {
					this.socketAddress =  new InetSocketAddress(preferredAddressStrings.get(0), controllerConfig.getControlToDevicePort());
				}
				server.send(msg, socketAddress);
				success = true;
			} catch (UnresolvedAddressException | IOException e1) {
				logger.error("Error sending to device {} using address {}! (Setting socketAddress back to null).",
						deviceName, preferredAddressStrings.get(0), e1);

				//set the socketAddress back to null as it will need to be rebuilt
				socketAddress = null;
				//rotate the preferredAddressStrings list to try the next one in the list
				String failed_string = preferredAddressStrings.remove(0);
				preferredAddressStrings.add(failed_string);
				if(count > 4) break;
				count++;
			}
		}

	}

	public synchronized void send(String msg_name, Object... args) {
		if(hostName.startsWith("Virtual Test Device")) {
			return;
		}
		OSCMessage msg = new OSCMessage(msg_name, args);
		sendOscMsg(msg);
	}

	public synchronized void send(byte[]... data) {
		if (isConnected) {
			lazySetupAddressStrings();
			boolean success = false;
			int count = 0;
			boolean possible_IP_vIssue = false;
			List<Exception> exceptions = new ArrayList<>(5);
			while (!success) {
				try {
					String client_address = null;
					if (this.socketAddress != null) {
						//client_address = this.socketAddress.getHostName();
						client_address = this.socketAddress.getAddress().getHostAddress();
					} else {
						client_address = preferredAddressStrings.get(0);
					}

					Socket s = new Socket(client_address, ControllerConfig.getInstance().getCodeToDevicePort());
					for (byte[] d : data) {
						s.getOutputStream().write(d);
					}
					s.close();
					success = true;
					logger.debug("Success sending to device {} using address {}!",
							deviceName, preferredAddressStrings.get(0));
				} catch (IOException | IllegalArgumentException e1) {
					logger.error("Error sending to device {} using address {}! (Setting socketAddress back to null).",
							deviceName, preferredAddressStrings.get(0), e1);
					//set the socketAddress back to null as it will need to be rebuilt
					this.setSocketAddress(null);//socketAddress = null;
					//rotate the preferredAddressStrings list to try the next one in the list
					String failedString = preferredAddressStrings.remove(0);    //remove from front
					preferredAddressStrings.add(failedString);        //add to end

					exceptions.add(e1);
					possible_IP_vIssue |= e1 instanceof java.net.SocketException && e1.getMessage().contains("rotocol");
					if (count > 4) break;
					count++;
				}
			}

			if (possible_IP_vIssue) {
				logger.error("It looks like there might be an IPv4/IPv6 incompatibility, try setting the JVM option -Djava.net.preferIPv6Addresses=true or -Djava.net.preferIPv4Addresses=true");
			}
			// Communicate the errors to the plugin gui if it's running (and anything else that's listening).
			exceptions.forEach((e) -> sendError("Error sending to device!", e));
		}
	}

	public void addStatusUpdateListener(StatusUpdateListener listener) {
		synchronized (statusUpdateListenerList) {
			statusUpdateListenerList.add(listener);
		}
	}

	public void removeStatusUpdateListener(StatusUpdateListener listener) {
		StatusUpdateListener removal_object = null;

		synchronized (statusUpdateListenerList) {
			statusUpdateListenerList.remove(listener);
		}
	}

	public void addFriendlyNameUpdateListener(StatusUpdateListener listener) {
		synchronized (friendlyNameListenerList) {
			friendlyNameListenerList.add(listener);
		}
	}

	public void removeFriendlyNameUpdateListener(StatusUpdateListener listener) {
		StatusUpdateListener removal_object = null;

		synchronized (friendlyNameListenerList) {
			friendlyNameListenerList.remove(listener);
		}
	}



	public void addConnectedUpdateListener(ConnectedUpdateListener listener) {
		synchronized (connectedUpdateListenerList) {
			connectedUpdateListenerList.add(listener);
		}
	}

	public void addLoggingStateListener(ConnectedUpdateListener listener){
		synchronized (loggingStateListener)
		{
			loggingStateListener.add(listener);
		}
	}

	public void removeConnectedUpdateListener(ConnectedUpdateListener listener) {
		synchronized (connectedUpdateListenerList) {
			connectedUpdateListenerList.remove(listener);
		}
	}

	public void removeLoggingStateListener(ConnectedUpdateListener listener){
		synchronized (loggingStateListener)
		{
			loggingStateListener.remove(listener);
		}
	}
    public void addSocketAddressChangedListener(SocketAddressChangedListener listener) {
		synchronized (socketAddressChangedListenerList) {
			socketAddressChangedListenerList.add(listener);
		}
	}

	public void addDeviceRemovedListener(DeviceRemovedListener listener) {
		synchronized (deviceRemovedListenerList) {
			deviceRemovedListenerList.add(listener);
		}
	}


	public void addDeviceIdUpdateListener(DeviceIdUpdateListener listener) {
		synchronized (deviceIdUpdateListenerList) {
			deviceIdUpdateListenerList.add(listener);
		}
	}

	public void removeDeviceIdUpdateListener(DeviceIdUpdateListener listener) {
		synchronized (deviceIdUpdateListenerList) {
			deviceIdUpdateListenerList.remove(listener);
		}
	}

	public void addErrorListener(ErrorListener listener) {
		synchronized (errorListenerList) {
			errorListenerList.add(listener);
		}
	}

	public void removeErrorListener(ErrorListener listener) {
		synchronized (errorListenerList) {
			errorListenerList.remove(listener);
		}
	}

	public String getStatus(){
		return status;
	}

	/**
	 * Store new status. If status has changed, will generate an event
	 * @param new_status the new status to write
	 */
	public void setStatus(String new_status) {


		status = new_status;
		synchronized (statusUpdateListenerList) {
			for (StatusUpdateListener statusUpdateListener : statusUpdateListenerList) {
				statusUpdateListener.update(status);
			}
	}}

    public void setIsConnected(boolean connected) {

		if (isConnected != connected) {
			this.isConnected = connected;
			synchronized (connectedUpdateListenerList) {
				for (ConnectedUpdateListener listener : connectedUpdateListenerList) {
					listener.update(connected);
				}
			}
			setStatus(isConnected? "Connected" : "Disconnected");
		}


		if (!connected)
		{
			controlRequestSent = false;
		}

    }

	private void sendError(String description, Exception ex) {
		synchronized (errorListenerList) {
			for (ErrorListener l : errorListenerList) {
				l.errorOccurred(this.getClass(), description, ex);
			}
		}
	}

	public void addLogListener(LogListener listener) {
		synchronized (logListenerList) {
			logListenerList.add(listener);
		}
	}

	public void removeLogListener(LogListener listener) {
		synchronized (logListenerList) {
			logListenerList.remove(listener);
		}
	}

	/**
	 * Get the log by page
	 * @param page the page number
	 * @return the log for that page
	 */
	public String getDeviceLog(int page) {
		String ret = currentLogPage;
		if (page < completeLog.size()) {
			ret = completeLog.get(page);
		}
		return ret;
	}

	/**
	 * Gets the number of pages in our log + our current last page
	 * @return number of pages
	 */
	public int numberLogPages(){
		return completeLog.size() + 1;
	}

	/**
	 * Is Device Logging available for this device
	 * @return Whether logging is enabled for device
	 */
	public boolean isLoggingEnabled(){
		return loggingEnabled;
	}

	/**
	 * Tell the device to start or stop sending log messages
	 * @param enable true if we want logs
	 */
	public void setLogging (boolean enable){

		// we need to send a start logging message to the device

		loggingEnabled = enable;

		send(OSCVocabulary.Device.GET_LOGS, new Object[]{enable ? 1 : 0});

	}

	/**
	 * Send a reboot message to the device
	 */
	public void rebootDevice()
	{
		send(OSCVocabulary.Device.REBOOT);
	}

	/**
	 * Send a shutdown message to the device
	 */
	public void shutdownDevice()
	{
		send((OSCVocabulary.Device.SHUTDOWN));
	}
}
