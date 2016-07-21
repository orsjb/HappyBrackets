package net.happybrackets.controller.network;

import net.happybrackets.controller.config.ControllerConfig;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.DatagramSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;

public class ControllerAdvertiser {
	ControllerConfig env;
	private Thread advertismentService;

	public ControllerAdvertiser(ControllerConfig env) throws UnknownHostException {
		this.env = env;

		InetAddress group = InetAddress.getByName(env.getMulticastAddr());
		//set up an indefinite thread to advertise the controller
		advertismentService = new Thread() {
			public void run() {
				try ( DatagramSocket serverSocket = new DatagramSocket() ) {
					//System.out.println("Creating ControllerAdvertiser with interface " + env.getMyInterface());
					//serverSocket.setNetworkInterface( NetworkInterface.getByName(env.getMyInterface()) );
					//serverSocket.joinGroup(group);
					String msg = "controllerHostname: " + env.getMyHostName() + " controllerAddress: " + env.getMyAddress();
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
					System.err.println("Warning: Error in controller advertisment service, controller advertisment is no longer functioning.");
					ex.printStackTrace();
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
