package net.happybrackets.device.network;

import net.happybrackets.core.Device;
import net.happybrackets.device.config.DeviceController;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;

public interface ControllerDiscoverer {
	
	default DeviceController listenForController(String multicastAddress, int multicastPort) throws UnknownHostException {
		byte[] buf = new byte[256];
		String controllerHostname = null;
		String controllerAddress = null;
		try ( MulticastSocket clientSocket = new MulticastSocket(multicastPort) ) {
			clientSocket.setNetworkInterface(NetworkInterface.getByName(Device.preferedInterface));
			clientSocket.joinGroup( InetAddress.getByName(multicastAddress) );
			while (controllerHostname == null || controllerAddress == null) {
				DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
				clientSocket.receive(msgPacket);
				String[] msgParts = new String(buf, 0, buf.length).trim().split(" ");
				if ( msgParts.length == 4 && msgParts[0].equals("controllerHostname:") && msgParts[2].equals("controllerAddress:") ) {
					controllerHostname = msgParts[1];
                    controllerAddress = msgParts[3];
				}
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		return new DeviceController(controllerHostname, controllerAddress);
	}

}
