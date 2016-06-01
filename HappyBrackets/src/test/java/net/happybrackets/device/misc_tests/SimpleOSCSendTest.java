package net.happybrackets.device.misc_tests;

import java.io.IOException;
import java.net.InetSocketAddress;

import net.happybrackets.device.config.DeviceConfig;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

public class SimpleOSCSendTest {
	protected static final DeviceConfig config = new DeviceConfig();

	public static void main(String[] args) throws IOException {
		OSCServer s = OSCServer.newUsing(OSCServer.UDP);
		InetSocketAddress addr = new InetSocketAddress("pisound-009e959c510a.local", config.getControlToDevicePort());
		
		s.send(new OSCMessage("/PI/"), null);
	}

}
