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

import net.happybrackets.core.config.LoadableConfig;
import net.happybrackets.core.BroadcastManager;
import net.happybrackets.device.network.ControllerDiscoverer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.Map;

public class DeviceConfig extends LoadableConfig implements ControllerDiscoverer {

    final static Logger logger = LoggerFactory.getLogger(DeviceConfig.class);

	/**
	 * We will store controllers as a map rather than as a single controller
	 */
	private Map<Integer, DeviceController> deviceControllers = new Hashtable<Integer, DeviceController>();


	/**
	 * We need to create a default controller to pass tests
	 */
	DeviceController lastController = new DeviceController("", "", 0);

	private int polyLimit = 4;
	private String logFilePath = "stdout";


	public String getControllerHostname() {
		return lastController.getHostname();
	}


	public String getControllerAddress() {
	  return lastController.getAddress().getAddress().getHostAddress();
	}


	/**
	 * Get the list of controllers registered
	 * @return
	 */
	public Map<Integer, DeviceController> getDeviceControllers(){
		return deviceControllers;
	}

	/**
	 * Register to listen for controllers
	 * @param broadcastManager
	 */
	public void listenForController(BroadcastManager broadcastManager) {
		ControllerDiscoverer.super.listenForController(this, broadcastManager, logger);
	}

	public int getPolyLimit() {
		return polyLimit;
	}

	public String getLogFilePath() {return logFilePath; };

	public static DeviceConfig getInstance() {
		return (DeviceConfig)(LoadableConfig.getInstance());
	}

	public static DeviceConfig load(String configFile) {
		return LoadableConfig.load( configFile, new DeviceConfig() );
	}

	public void deviceControllerFound (String hostname, String address, int port)
	{
		String hash_build = address + port;
		int hash = hash_build.hashCode();

		DeviceController controller = deviceControllers.get(hash);
		if (controller == null)
		{
			controller = new DeviceController(hostname, address, port);
			deviceControllers.put(controller.hashCode(), controller);
		}

		controller.controllerSeen();
		lastController = controller;
	}

}
