package net.happybrackets.core.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;

import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Provides a loadable abstract class of our core configuration parameters.
 * This class wraps the default methods of the core interface with overridable values.
 * Each time a getInstance call is executed it first checks the the relevant class value.
 *   If the value is undefined it delegates to the interface default else it returns the stored value.
 *
 * To instantiate an object extending this class call the static buildFromJSON method.
 *
 * This class holds the static method load which provides a generic interface for building descendants
 *  of this object from JSON config files via the Gson library.
 *
 */
public abstract class LoadableConfig implements EnvironmentConfig {

	final static Logger logger = LoggerFactory.getLogger(LoadableConfig.class);

	//use Integer instead of int so we can delegate to the interface default value on null
	private String 	multicastAddr;
	private Integer broadcastOSCPort;
	private Integer statusFromDevicePort;
	private Integer clockSynchPort;
	private Integer codeToDevicePort;
	private Integer controlToDevicePort;
	private Integer controllerDiscoveryPort;
  private Integer controllerHTTPPort;

	//how often the PI sends an alive message to the server
	private Integer aliveInterval;

	protected static LoadableConfig singletonInstance;

	//places
	private String workingDir;
	private String audioDir;
	private String knownDevicesFile;

	public static <T extends LoadableConfig> T load(String fileName, T config) {
		logger.info("Loading: {}", fileName);
		if (config == null) {
			logger.error("Argument 2, Config must be an instantiated object!");
			return null;
		}

		File f = new File(fileName);
    //try again for the default
    if ( !f.isFile() ) {
        logger.warn("Unable to open file: {}", fileName);
        fileName += ".default"; //append .default for second try
        logger.info("Trying default: {}", fileName);
        f = new File(fileName);
    }
		if ( !f.isFile() ) {
			logger.error("File: '{}' does not exist!", f.getAbsolutePath());
			return null;
		}

		Gson gson = new Gson();
    try {
        BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath()));
        config = gson.fromJson(br, (Type) config.getClass());
    } catch (IOException e) {
        logger.error("Unable to open file: {}", fileName, e);
    }

    if (config == null) {
        logger.error("Failed loading config file: {}");
    }

		singletonInstance = config;
		return config;
	}

	/**
	 * Create a configuration object from the specified JSON string. {@link #singletonInstance} is updated to the
	 * new configuration (if a parse error did not occur).
	 *
	 * @param configJSON The configuration in JSON format.
	 * @param configClass The configuration object class to create.
	 * @param <T> Configuration object type.
	 * @return Newly created configuration object of class T.
	 * @throws com.google.gson.JsonSyntaxException If the given string is not valid JSON.
	 */
	public static <T extends LoadableConfig> T loadFromString(String configJSON, Class<T> configClass) {
		Gson gson = new Gson();
		T config = gson.fromJson(configJSON, configClass);
		singletonInstance = config;
		return config;
	}

	public static LoadableConfig getInstance() {
		return singletonInstance;
	}

	//Override getters
	public String getMulticastAddr() {
		if (multicastAddr != null) {
		    return multicastAddr;
		}
		else {
		    return EnvironmentConfig.super.getMulticastAddr();
		}
	}
	public int getBroadcastPort() {
		if (broadcastOSCPort != null) {
		    return broadcastOSCPort;
		}
		else {
		    return EnvironmentConfig.super.getBroadcastPort();
		}
	}
	public int getStatusFromDevicePort() {
		if (statusFromDevicePort != null) {
		    return statusFromDevicePort;
		}
		else {
		    return EnvironmentConfig.super.getStatusFromDevicePort();
		}
	}

	public int getClockSynchPort() {
		if (clockSynchPort != null) {
		    return clockSynchPort;
		}
		else {
		    return EnvironmentConfig.super.getClockSynchPort();
		}
	}
	public int getCodeToDevicePort() {
		if (codeToDevicePort != null) {
		    return codeToDevicePort;
		}
		else {
		    return EnvironmentConfig.super.getCodeToDevicePort();
		}
	}
	public int getControlToDevicePort() {
		if (controlToDevicePort != null) {
		    return controlToDevicePort;
		}
		else {
		    return EnvironmentConfig.super.getControlToDevicePort();
		}
	}
	public int getControllerDiscoveryPort() {
		if (controllerDiscoveryPort != null) {
		    return controllerDiscoveryPort;
		}
		else {
		    return EnvironmentConfig.super.getControllerDiscoveryPort();
		}
	}
	public int getAliveInterval() {
		if (aliveInterval != null) {
		    return aliveInterval;
		}
		else {
		    return EnvironmentConfig.super.getAliveInterval();
		}
	}

	public String getAudioDir() {
		if (audioDir != null) {
		    return audioDir;
		}
		else {
		    return EnvironmentConfig.super.getAudioDir();
		}
	}

	public void setKnownDevicesFile(String path) {
		knownDevicesFile = path;
	}

	public String getKnownDevicesFile() {
		if (knownDevicesFile != null) {
		    return knownDevicesFile;
		}
		else {
		    return EnvironmentConfig.super.getKnownDevicesFile();
		}
	}

	public int getControllerHTTPPort() {
		if (controllerHTTPPort != null) {
			return controllerHTTPPort;
		}
		else {
			return EnvironmentConfig.super.getControllerHTTPPort();
		}
	}

	public void setWorkingDir(String dir) {
				this.workingDir = dir;
	}

	public String getWorkingDir() {
		if (workingDir != null) {
				return workingDir;
		}
		else {
				return EnvironmentConfig.super.getWorkingDir();
		}
	}

}
