package net.happybrackets.core;

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
 * Each time a get call is executed it first checks the the relevant class value.
 *   If the value is undefined it delegates to the interface default else it returns the stored value.
 *   
 * To instantiate an object extending this class call the static buildFromJSON method.
 * 
 * This class holds the static method load which provides a generic interface for building descendants
 *  of this object from JSON config files via the Gson library. 
 *
 */
public abstract class LoadableConfig implements EnvironmentConf {
	//use Integer instead of int so we can delegate to the interface default value on null
	private Boolean useHostname;
	private String  MyHostName;
	private String  MyInterface;	
	private String  MulticastSynchAddr;
	private Integer BroadcastOSCPort;
	private Integer StatusFromPIPort;
	private Integer ClockSynchPort;
	private Integer CodeToPIPort;
	private Integer ControlToPIPort;
	private Integer ControllerDiscoveryPort;
    private Integer ControllerHTTPPort;

	//how often the PI sends an alive message to the server
	private Integer AliveInterval;

	//places
	private String  WorkingDir;
	private String  AudioDir;
	private String  KnownPIsFile;
	
	public static <T> T load(String fileName, T config) {			
		System.out.println("Loading: " + fileName);
		
		if (config == null) {
			System.err.println("Argument 2, Config must be an instantiated object!");
			return null;
		}
		
		File f = new File(fileName);
		if ( !f.isFile() ) {
			System.err.println("File: '" + f.getAbsolutePath() + "' does not exist!");
			return null;
		}
		
		Gson gson = new Gson();
		
		try {
			BufferedReader br = new BufferedReader( new FileReader(f.getAbsolutePath() ));
			config = gson.fromJson(br, (Type) config.getClass());
		}
		catch (IOException e) {
			System.out.println("Unable to open file: " + fileName);
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
            return EnvironmentConf.super.useHostname();
        }
    }
	public String getMyHostName() {
		if (MyHostName != null) {
		    return MyHostName;		
		}
		else {
		    return EnvironmentConf.super.getMyHostName();
		}
	}
	public String getMyInterface() {
		if (MyInterface != null) {
		    return MyInterface;		
		}
		else {
		    return EnvironmentConf.super.getMyInterface();
		}
	}
	public String getMulticastSynchAddr() {
		if (MulticastSynchAddr != null) {
		    return MulticastSynchAddr;		
		}
		else {
		    return EnvironmentConf.super.getMulticastSynchAddr();
		}
	}
	public int getBroadcastOSCPort() {
		if (BroadcastOSCPort != null) {
		    return BroadcastOSCPort;		
		}
		else {
		    return EnvironmentConf.super.getBroadcastOSCPort();
		}
	}
	public int getStatusFromPIPort() {
		if (StatusFromPIPort != null) {
		    return StatusFromPIPort;		
		}
		else {
		    return EnvironmentConf.super.getStatusFromPIPort();
		}
	}
	public int getClockSynchPort() {
		if (ClockSynchPort != null) {
		    return ClockSynchPort;		
		}
		else {
		    return EnvironmentConf.super.getClockSynchPort();
		}
	}
	public int getCodeToPIPort() {
		if (CodeToPIPort != null) {
		    return CodeToPIPort;		
		}
		else {
		    return EnvironmentConf.super.getCodeToPIPort();
		}
	}
	public int getControlToPIPort() {
		if (ControlToPIPort != null) {
		    return ControlToPIPort;		
		}
		else {
		    return EnvironmentConf.super.getControlToPIPort();
		}
	}
	public int getControllerDiscoveryPort() {
		if (ControllerDiscoveryPort != null) {
		    return ControllerDiscoveryPort;		
		}
		else {
		    return EnvironmentConf.super.getControllerDiscoveryPort();
		}
	}
	public int getAliveInterval() {
		if (AliveInterval != null) {
		    return AliveInterval;		
		}
		else {
		    return EnvironmentConf.super.getAliveInterval();
		}
	}
	public String getWorkingDir() {
		if (WorkingDir != null) {
		    return WorkingDir;		
		}
		else {
		    return EnvironmentConf.super.getWorkingDir();
		}
	}
	public String getAudioDir() {
		if (AudioDir != null) {
		    return AudioDir;		
		}
		else {
		    return EnvironmentConf.super.getAudioDir();
		}
	}
	public String getKnownPIsFile() {
		if (KnownPIsFile != null) {
		    return KnownPIsFile;		
		}
		else {
		    return EnvironmentConf.super.getKnownPIsFile();
		}
	}

	public int getControllerHTTPPort() {
		if (ControllerHTTPPort != null) {
			return ControllerHTTPPort;
		}
		else {
			return EnvironmentConf.super.getControllerHTTPPort();
		}
	}
}
