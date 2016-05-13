package net.happybrackets.core;

import java.util.HashSet;
import java.util.Set;


public abstract class Config {

	//hosts and ports for network messages
	public final static String controllerHostname  	= "woof.local"; 
	public final static String multicastSynchAddr   = "225.2.2.5";
	public final static int broadcastOSCPort 		= 2222;
	public final static int statusFromPIPort 		= 2223;
	public final static int clockSynchPort			= 2224;
	public final static int codeToPIPort			= 2225;
	public final static int controlToPIPort			= 2226;
	
	//how often the PI sends an alive message to the server
	public static final int aliveInterval = 1000;   		
	
	//places
	public static String workingDir = ".";
	public static String audioDir = workingDir + "/audio";
	public static String knownPIsFile = workingDir + "/config/known_pis";
	private String compositionsPath;

	public String getCompositionsPath() {
		return compositionsPath;
	}
}
