package net.happybrackets.core;

import java.net.UnknownHostException;

public class DeviceConfig extends LoadableConfig implements EnvironmentConfig, ControllerDiscoverer {

	private String controllerHostName;
	private int polyLimit = 4;

	public synchronized String getControllerHostname() {
		if (controllerHostName != null) {
			return controllerHostName;
		}
		
		//Block and search for a controller
		// TODO we need a logging framework too now :/
		try {
			controllerHostName = listenForController( getMulticastSynchAddr(), getControllerDiscoveryPort());
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
}
