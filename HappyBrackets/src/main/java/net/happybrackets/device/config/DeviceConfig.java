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

package net.happybrackets.device.config;

import de.sciss.net.OSCServer;
import net.happybrackets.core.config.DefaultConfig;
import net.happybrackets.core.config.LoadableConfig;
import net.happybrackets.core.BroadcastManager;
import net.happybrackets.device.network.ControllerDiscoverer;
import net.happybrackets.device.network.UDPCachedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class DeviceConfig extends LoadableConfig implements ControllerDiscoverer {


	/**
	 * If we use an event we can act immediately instead of polling
	 */
	public interface DeviceControllerDiscoveredListener{
		void controllerDiscovered (DeviceController deviceController);
	}

	// Define variables we will use for broadcast
	DatagramSocket broadcastSocket = null;

	static OSCServer secondaryOscServer = null;

    final static Logger logger = LoggerFactory.getLogger(DeviceConfig.class);

	/**
	 * We will store controllers as a map rather than as a single controller
	 * Used primarily for testing
	 */
	private Map<Integer, DeviceController> deviceControllers = new Hashtable<Integer, DeviceController>();



	// A list of listeners to be notified when a controller is discovered
	List<DeviceControllerDiscoveredListener> controllerDiscoveredListeners = new ArrayList<>();

	/**
	 * Add a listener for discoverer. Used primarily for testing
	 * @param listener the listener for controller messages
	 */
	public synchronized void addDeviceDiscoveredListener(DeviceControllerDiscoveredListener listener){
		controllerDiscoveredListeners.add(listener);
	}
	/**
	 * We need to create a default controller to pass tests
	 */
	DeviceController lastController = new DeviceController("", "", 0, 0, 0, 0);

	// This is where we will place our logs. This way we can put in a ram fs
	private final String LOG_FOLDER =  "ramfs/";

	private int polyLimit = 4;

	private String logFilePath = "stdout";


	public DeviceConfig(){

		try {
			if (secondaryOscServer == null) {
				int secondary_port = DefaultConfig.SECONDARY_BROADCAST_PORT;
				secondaryOscServer = OSCServer.newUsing(OSCServer.UDP, secondary_port);
				secondaryOscServer.start();
				System.out.println("Opened secondary controller discovery port on " + secondary_port);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		new File(LOG_FOLDER).mkdirs();

		try {
			broadcastSocket = new DatagramSocket();
			broadcastSocket.setBroadcast(true);
			broadcastSocket.setReuseAddress(true);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getControllerHostname() {
		return lastController.getHostname();
	}


	public String getControllerAddress() {
	  return lastController.getAddress().getAddress().getHostAddress();
	}


	/**
	 * Register to listen for controllers
	 * @param broadcastManager The broadcast manager
	 */
	public void listenForController(BroadcastManager broadcastManager) {
		ControllerDiscoverer.super.listenForController(this, broadcastManager, logger);
		if (secondaryOscServer != null) {
			secondaryOscServer.addOSCListener(ControllerDiscoverer.super.createListener(this));
		}
	}

	public int getPolyLimit() {
		return polyLimit;
	}

	public String getLogFilePath() {return LOG_FOLDER + logFilePath; };

	public static DeviceConfig getInstance() {
		return (DeviceConfig)(LoadableConfig.getInstance());
	}

	public static DeviceConfig load(String configFile) {
		return LoadableConfig.load( configFile, new DeviceConfig() );
	}


	/**
	 * We will send all our known controllers an alive message
	 */
	public void notifyAllControllers()
	{
		UDPCachedMessage cached_message = lastController.getCachedMessage();

		DatagramPacket packet = cached_message.getCachedPacket();
		sendMessageToAllControllers(packet);
	}

	/**
	 * We will send a datagram packet to all controllers
	 * @param packet The datagram packet we are sending
	 */
	public void sendMessageToAllControllers (DatagramPacket packet)
	{
		synchronized (deviceControllers)
		{
			deviceControllers.forEach((hash, controller) -> {
				packet.setSocketAddress(controller.getAddress());

				try {
					broadcastSocket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}

	}
	/**
	 * Add the contoller to our list of known controllers
	 * @param hostname hostname
	 * @param address IP address
	 * @param port Port that it wants message transmitted to
	 * @param device_id our device ID
	 * @param connectPort the server port that controllers will connect to us through
	 * @param filePort the port we use to receive non class files on
	 */
	public synchronized void deviceControllerFound(String hostname, String address, int port, int device_id, int connectPort, int filePort)
	{

		int hash = DeviceController.buildHashCode(address, port, connectPort, filePort);

		synchronized (deviceControllers) {
			DeviceController controller = deviceControllers.get(hash);
			if (controller == null) {
				controller = new DeviceController(hostname, address, port, device_id, connectPort, filePort);
				deviceControllers.put(controller.hashCode(), controller);
			}
			else
			{
				controller.setDeviceId(device_id);
				controller.controllerSeen();
			}

			// we want to do this every time
			for (DeviceControllerDiscoveredListener listener : controllerDiscoveredListeners){
				listener.controllerDiscovered(controller);
			}


			lastController = controller;
		}

		UDPCachedMessage cached_message = lastController.getCachedMessage();

		DatagramPacket packet = cached_message.getCachedPacket();
		packet.setSocketAddress(lastController.getAddress());

		try {
			broadcastSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
