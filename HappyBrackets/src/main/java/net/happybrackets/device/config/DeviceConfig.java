package net.happybrackets.device.config;

import net.happybrackets.core.config.LoadableConfig;
import net.happybrackets.device.network.ControllerDiscoverer;

import java.net.UnknownHostException;

public class DeviceConfig extends LoadableConfig implements ControllerDiscoverer {

	public static DeviceConfig singletonInstance;

	private String controllerHostName;
	private int polyLimit = 4;

	public synchronized String getControllerHostname() {
		if (controllerHostName != null) {
			return controllerHostName;
		}
		//Block and search for a controller
		// TODO use a proper logging framework
		try {
			controllerHostName = listenForController( getMulticastAddr(), getControllerDiscoveryPort());
		} catch (UnknownHostException e) {
			System.out.println("Error obtaining controller hostname.");
			e.printStackTrace();
		}
		return controllerHostName;
	}
	
	public int getMyId() {
		return 0;
	}

	public int getPolyLimit() {
		return polyLimit;
	}

	public static DeviceConfig load(String configFile) {
		singletonInstance = LoadableConfig.load(configFile, new DeviceConfig());
		return singletonInstance;
	}

	public static DeviceConfig getInstance() {
		return singletonInstance;
	}
}
