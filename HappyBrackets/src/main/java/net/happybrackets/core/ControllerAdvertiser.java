package net.happybrackets.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;

public class ControllerAdvertiser {
	ControllerConfig env;
	private Thread advertismentService;

	public ControllerAdvertiser(ControllerConfig env) throws UnknownHostException {
		super();
		this.env = env;
		
		InetAddress group = InetAddress.getByName(env.getMulticastSynchAddr());
		//set up an indefinite thread to advertise the controller
		advertismentService = new Thread() {
			public void run() {
				try (MulticastSocket serverSocket = new MulticastSocket(env.getControllerDiscoveryPort()) ) {
					serverSocket.joinGroup(group);
					serverSocket.setNetworkInterface( NetworkInterface.getByName(env.getMyInterface()) );
					String msg = "controllerHostname: " + env.getMyHostName();
					DatagramPacket msgPacket = new DatagramPacket(
						msg.getBytes(),
						msg.getBytes().length, 
						group, 
						env.getControllerDiscoveryPort()
					);
					while(true) {
						serverSocket.send(msgPacket);
						try {
							Thread.sleep(env.getAliveInterval());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				catch (IOException ex) {
					System.err.println("Warning: Your current network does not support multicast communication. Some features of Happy Brackets will not work.");
//					ex.printStackTrace();
				}
 				
			}
		};
	}
	
	public void start() {
		advertismentService.start();
	}
	
	public void interrupt() {
		advertismentService.interrupt();
	}
	
	public boolean isAlive() {
		return advertismentService.isAlive();
	}
}
