package net.happybrackets.core;

import de.sciss.net.OSCMessage;

public class DeviceStatus {

	String name;	
	
	public OSCMessage toOSCMessage() {
		OSCMessage result = new OSCMessage("/pi_status");
		//TODO
		return result;
	}
	
	public void fromOSCMessage(OSCMessage msg) {
		//TODO
	}
	
}
