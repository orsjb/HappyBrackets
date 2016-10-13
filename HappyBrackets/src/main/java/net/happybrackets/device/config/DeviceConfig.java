package net.happybrackets.device.config;

import net.happybrackets.core.config.LoadableConfig;
import net.happybrackets.core.BroadcastManager;
import net.happybrackets.device.network.ControllerDiscoverer;

import java.net.UnknownHostException;

public class DeviceConfig extends LoadableConfig implements ControllerDiscoverer {

	private int polyLimit = 4;
	private DeviceController controller = new DeviceController("", "", 0);

	public String getControllerHostname() {
		return controller.getHostname();
	}

	public String getControllerAddress() {
	  return controller.getAddress();
	}

	public void listenForController(BroadcastManager broadcastManager) {
		ControllerDiscoverer.super.listenForController(controller, broadcastManager);
	}

	public int getMyId() {
		return controller.getDeviceId();
	}

	public int getPolyLimit() {
		return polyLimit;
	}

	public static DeviceConfig getInstance() {
		return (DeviceConfig)(LoadableConfig.getInstance());
	}

	public static DeviceConfig load(String configFile) {
		return LoadableConfig.load( configFile, new DeviceConfig() );
	}


}
