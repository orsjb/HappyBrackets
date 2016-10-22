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
