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

import net.happybrackets.core.Synchronizer;
import net.happybrackets.core.config.LoadableConfig;
import net.happybrackets.core.BroadcastManager;
import net.happybrackets.device.network.ControllerDiscoverer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

public class DeviceConfig extends LoadableConfig implements ControllerDiscoverer {

    final static Logger logger = LoggerFactory.getLogger(DeviceConfig.class);

	private int polyLimit = 4;
	private String logFilePath = "logs/last-run.txt";
	private DeviceController controller = new DeviceController("", "", 0);

	public String getControllerHostname() {
		return controller.getHostname();
	}

	public String getControllerAddress() {
	  return controller.getAddress();
	}

	public void listenForController(BroadcastManager broadcastManager) {
		ControllerDiscoverer.super.listenForController(controller, broadcastManager, logger);
	}

	public int getMyId() {
		return controller.getDeviceId();
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


}
