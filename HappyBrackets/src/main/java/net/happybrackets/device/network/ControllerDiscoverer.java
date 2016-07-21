package net.happybrackets.device.network;

import net.happybrackets.core.Device;
import net.happybrackets.device.config.DeviceController;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;

public interface ControllerDiscoverer {

	default DeviceController listenForController(String multicastAddress, int multicastPort) throws UnknownHostException {
		System.out.println("Listening for controller");
		byte[] buf = new byte[256];
		String controllerHostname = null;
		String controllerAddress = null;
		InetSocketAddress mcSockAddr = new InetSocketAddress(multicastAddress, multicastPort);
		try ( MulticastSocket mSocket = new MulticastSocket( multicastPort ) ) {
			//mSocket.setReuseAddress(true);
			mSocket.setSoTimeout(5000); 	//timeout after 5 seconds
			//mSocket.bind(mcSockAddr);
			//mSocket.joinGroup(mcSockAddr.getAddress());
			mSocket.joinGroup( InetAddress.getByName(multicastAddress) );


			//TODO this is still needed on a Mac. General confusion about when we need to set network intefaces or not.
			//System.out.println("Preferred network interface = " + Device.getInstance().preferredInterface);
			//mSocket.setNetworkInterface(NetworkInterface.getByName(Device.getInstance().preferredInterface));

			//create a DatagramSocket to catch our UDP messages commming via our multicast group
			DatagramSocket datagramListnerSocket = new DatagramSocket(null);
			datagramListnerSocket.setReuseAddress(true);
			datagramListnerSocket.bind(mcSockAddr);
			//lets try running with a timeout just to keep things sane trying 30 seconds as a good failsafe
			datagramListnerSocket.setSoTimeout(30000);
			while (controllerHostname == null && controllerAddress == null) {
				DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
				mSocket.receive(msgPacket);
				System.out.println("Received message");
				String[] msgParts = new String(buf, 0, buf.length).trim().split(" ");
				if ( msgParts.length == 4 && msgParts[0].equals("controllerHostname:") && msgParts[2].equals("controllerAddress:") ) {
					controllerHostname = msgParts[1];
                    controllerAddress = msgParts[3];
				}
				else {
					System.err.println("recieved malformed controller discovery packet!");
				}
			}
			System.out.println("Found controller");
		}
		catch (SocketTimeoutException ex) {
			System.err.println("Controller discovery socket receive operation reached timeout. Returning a localhost controller.");
			//ex.printStackTrace();
			//set to local host as we can't find a controller
			controllerHostname = "localhost";
			controllerAddress = "127.0.0.1";
		}
		catch (IOException ex) {
			System.err.println("Error with controller discovery socket IO:");
			ex.printStackTrace();
		}

		return new DeviceController(controllerHostname, controllerAddress);
	}

}
