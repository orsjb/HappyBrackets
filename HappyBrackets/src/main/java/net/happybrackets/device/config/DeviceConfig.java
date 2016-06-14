package net.happybrackets.device.config;

import net.happybrackets.core.config.LoadableConfig;
import net.happybrackets.device.network.ControllerDiscoverer;

import java.net.UnknownHostException;

public class DeviceConfig extends LoadableConfig implements ControllerDiscoverer {

	private int polyLimit = 4;
	private DeviceController controller;

	public synchronized String getControllerHostname() {
		if (controller != null) {
			return controller.getHostname();
		}

		return waitForController().getHostname();
	}

    public synchronized String getControllerAddress() {
        if (controller != null) {
            return controller.getAddress();
        }

        return waitForController().getAddress();
    }

    private DeviceController waitForController() {
        //Block and search for a controller
        try {
            controller = listenForController( getMulticastAddr(), getControllerDiscoveryPort());
        } catch (UnknownHostException e) {
            System.out.println("Error obtaining controller hostname and address.");
            e.printStackTrace();
        }
        return controller;
    }

	public int getMyId() {
		return -1;
	}

	public int getPolyLimit() {
		return polyLimit;
	}

	public static DeviceConfig getInstance() {
		return (DeviceConfig)(LoadableConfig.getInstance());
	}

	public static DeviceConfig load(String configFile) {
		return (DeviceConfig)(LoadableConfig.load(configFile, new DeviceConfig()));
	}


}
