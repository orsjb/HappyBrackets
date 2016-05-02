package net.happybrackets.core;

//SG 2016-04-04
//We should move from a static configuration definition to a dynamic definition.
//By moving to a dynamic method we can load settings on launch from external files 
// and we can run init level operations as part of the object instantiation.
//Key bonuses are the PIs being able to look for the host controller instead of the current hard coded approach.


public interface EnvironmentConf {

	//hosts and ports for network messages
	default public boolean useHostname()				{ return true; }
	default public String getMyHostName() 				{ if (useHostname()) return Device.myHostname; else return Device.myIP; }
	default public String getMyInterface() 				{ return Device.preferedInterface; }	
	default public String getMulticastSynchAddr()		{ return "225.2.2.5"; }
	default public int getBroadcastOSCPort() 			{ return 2222; }
	default public int getStatusFromPIPort() 			{ return 2223; }
	default public int getClockSynchPort()				{ return 2224; }
	default public int getCodeToPIPort()				{ return 2225; }
	default public int getControlToPIPort()				{ return 2226; }
	default public int getControllerDiscoveryPort()		{ return 2227; }
	default public int getControllerHTTPPort()		    { return 2228; }
	
	//how often the PI sends an alive message to the server
	default public int getAliveInterval() 				{ return 1000; }
	
	//places
	default public String getWorkingDir() 				{ return "."; }
	default public String getAudioDir() 				{ return getWorkingDir() + "/audio"; }
	default public String getKnownPIsFile() 			{ return getWorkingDir() + "/config/known_pis"; }
}
