package net.happybrackets.core.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;

import com.google.gson.Gson;


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
	//use Integer instead of int so we can delegate to the interface default value on null
	private Boolean useHostname;
	private String  MyHostName;
	private String  MyInterface;	
	private String  MulticastSynchAddr;
	private Integer BroadcastOSCPort;
	private Integer StatusFromDevicePort;
	private Integer ClockSynchPort;
	private Integer CodeToDevicePort;
	private Integer ControlToDevicePort;
	private Integer ControllerDiscoveryPort;
    private Integer ControllerHTTPPort;

	//how often the PI sends an aslive message to the server
	private Integer AliveInterval;

	//places
	private String  WorkingDir;
	private String  AudioDir;
	private String KnownDevicesFile;
	
	public static <T> T load(String fileName, T config) {			
		System.out.println("Loading: " + fileName);
		
		if (config == null) {
			System.err.println("Argument 2, Config must be an instantiated object!");
			return null;
		}
		
		File f = new File(fileName);
        //try again for the default
        if ( !f.isFile() ) {
            System.err.println("Unable to open file: " + fileName);
            fileName += ".default"; //append .default for second try
            System.out.println("Trying default: " + fileName);
            f = new File(fileName);
        }

		if ( !f.isFile() ) {
			System.err.println("File: '" + f.getAbsolutePath() + "' does not exist!");
			return null;
		}
		
		Gson gson = new Gson();

        try {
            BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath()));
            config = gson.fromJson(br, (Type) config.getClass());
        } catch (IOException e) {
            System.err.println("Unable to open file: " + fileName);
            e.printStackTrace();
        }

        if (config == null) {
            System.err.println("Failed loading config file: " + fileName);
        }
		
		return config;
	}
	
	//Override getters
    public boolean useHostname() {
        if (useHostname != null) {
            return useHostname;
        }
        else {
            return EnvironmentConfig.super.useHostname();
        }
    }
	public String getMyHostName() {
		if (MyHostName != null) {
		    return MyHostName;		
		}
		else {
		    return EnvironmentConfig.super.getMyHostName();
		}
	}
	public String getMyInterface() {
		if (MyInterface != null) {
		    return MyInterface;		
		}
		else {
		    return EnvironmentConfig.super.getMyInterface();
		}
	}
	public String getMulticastAddr() {
		if (MulticastSynchAddr != null) {
		    return MulticastSynchAddr;		
		}
		else {
		    return EnvironmentConfig.super.getMulticastAddr();
		}
	}
	public int getBroadcastPort() {
		if (BroadcastOSCPort != null) {
		    return BroadcastOSCPort;		
		}
		else {
		    return EnvironmentConfig.super.getBroadcastPort();
		}
	}
	public int getStatusFromDevicePort() {
		if (StatusFromDevicePort != null) {
		    return StatusFromDevicePort;
		}
		else {
		    return EnvironmentConfig.super.getStatusFromDevicePort();
		}
	}
	public int getClockSynchPort() {
		if (ClockSynchPort != null) {
		    return ClockSynchPort;		
		}
		else {
		    return EnvironmentConfig.super.getClockSynchPort();
		}
	}
	public int getCodeToDevicePort() {
		if (CodeToDevicePort != null) {
		    return CodeToDevicePort;
		}
		else {
		    return EnvironmentConfig.super.getCodeToDevicePort();
		}
	}
	public int getControlToDevicePort() {
		if (ControlToDevicePort != null) {
		    return ControlToDevicePort;
		}
		else {
		    return EnvironmentConfig.super.getControlToDevicePort();
		}
	}
	public int getControllerDiscoveryPort() {
		if (ControllerDiscoveryPort != null) {
		    return ControllerDiscoveryPort;		
		}
		else {
		    return EnvironmentConfig.super.getControllerDiscoveryPort();
		}
	}
	public int getAliveInterval() {
		if (AliveInterval != null) {
		    return AliveInterval;		
		}
		else {
		    return EnvironmentConfig.super.getAliveInterval();
		}
	}
	public String getWorkingDir() {
		if (WorkingDir != null) {
		    return WorkingDir;		
		}
		else {
		    return EnvironmentConfig.super.getWorkingDir();
		}
	}
	public String getAudioDir() {
		if (AudioDir != null) {
		    return AudioDir;		
		}
		else {
		    return EnvironmentConfig.super.getAudioDir();
		}
	}
	public String getKnownDevicesFile() {
		if (KnownDevicesFile != null) {
		    return KnownDevicesFile;
		}
		else {
		    return EnvironmentConfig.super.getKnownDevicesFile();
		}
	}

	public int getControllerHTTPPort() {
		if (ControllerHTTPPort != null) {
			return ControllerHTTPPort;
		}
		else {
			return EnvironmentConfig.super.getControllerHTTPPort();
		}
	}

}
