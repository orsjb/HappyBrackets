package net.happybrackets.device.network;

import net.happybrackets.core.Device;
import net.happybrackets.device.config.DeviceConfig;
import net.happybrackets.core.Synchronizer;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;
import net.happybrackets.device.HB;
import net.happybrackets.device.config.LocalConfigManagement;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NetworkCommunication {

	public static interface Listener {
		public void msg(OSCMessage msg);
	}

	private int myID;							//ID assigned by the controller
	private OSCServer oscServer;				//The OSC server
	private InetSocketAddress controller, broadcastAddress;		//The network details of the controller
	private Set<Listener> listeners = Collections.synchronizedSet(new HashSet<Listener>()); 	
																//Listeners to incoming OSC messages
	final private HB hb;

	/**
	 * Instantiate a new {@link NetworkCommunication} object.
	 * @param _hb the {@link HB} object this object is attached to.
	 * @throws IOException thrown if there is a problem opening the {@link OSCServer}, likely due to the port already being in use.
     */
	public NetworkCommunication(HB _hb) throws IOException {
		this.hb = _hb;
		//init the OSCServer
		try {
			oscServer = OSCServer.newUsing(OSCServer.UDP, DeviceConfig.getInstance().getControlToDevicePort());
			oscServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//add a single master listener that forwards listening to delegates
		oscServer.addOSCListener(new OSCListener() {
			@Override
			public void messageReceived(OSCMessage msg, SocketAddress src, long time) {
				//include default listener behaviour that listens for the ID assigned to this device
				//note technically messages can be sent from anyone, so ignore messages being sent from self...
				//TODO questionable approach, is this escape needed?
				if(src instanceof InetSocketAddress && 
						((InetSocketAddress)src).getHostName().contains(DeviceConfig.getInstance().getMyHostName().split("[.]")[0])) {
					return;
				}
				if(msg.getName().equals("/device/set_id")) {
					myID = (Integer)msg.getArg(0);
					System.out.println("I have been given an ID by the controller: " + myID);
					hb.setStatus("ID " + myID);
				} else {
					//master commands...
					if(msg.getName().equals("/device/sync")) {
						long timeToAct = Long.parseLong((String)msg.getArg(0));
						System.out.println(msg.getArg(0).getClass() + " " + msg.getArg(0));
						hb.syncAudioStart(timeToAct);
					} else if(msg.getName().equals("/device/reboot")) {
						HB.rebootDevice();
					} else if(msg.getName().equals("/device/shutdown")) {
						HB.shutdownDevice();
					} else if(msg.getName().equals("/device/gain")) {
						hb.masterGainEnv.addSegment((Float)msg.getArg(0), (Float)msg.getArg(1));
					} else if(msg.getName().equals("/device/reset")) {
						hb.reset();
					} else if(msg.getName().equals("/device/reset_sounding")) {
						hb.resetLeaveSounding();
					} else if(msg.getName().equals("/device/clearsound")) {
						hb.clearSound();
					} else if(msg.getName().equals("/device/fadeout_reset")) {
						hb.fadeOutReset((Float)msg.getArg(0));
					} else if(msg.getName().equals("/device/fadeout_clearsound")) {
						hb.fadeOutClearSound((Float)msg.getArg(0));
					} else if(msg.getName().equals("/device/bleep")) {
						hb.testBleep();
					} else if ( msg.getName().equals("/device/config/wifi") && msg.getArgCount() == 2) {
                        //TODO: add interfaces path to device config
                        boolean status = LocalConfigManagement.updateInterfaces(
                                "/etc/network/interfaces",
                                (String) msg.getArg(0),
                                (String) msg.getArg(1)
                        );
                        if (status) System.out.println("Updated interfaces file");
                        else System.err.println("Unable to update interfaces file");
					}
					//all other messages getInstance forwarded to delegate listeners
					synchronized(listeners) {
						Iterator<Listener> i = listeners.iterator();
						while(i.hasNext()) {
							try {
								i.next().msg(msg);	
							} catch(Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		});
		//set up the controller address
		System.out.println( "Setting up controller: " + DeviceConfig.getInstance().getControllerHostname() );
		controller = new InetSocketAddress(
				DeviceConfig.getInstance().getControllerAddress(),
				DeviceConfig.getInstance().getStatusFromDevicePort()
		);
		System.out.println( "Controller resolved to address: " + controller );
		//set up the controller address
		broadcastAddress = new InetSocketAddress(DeviceConfig.getInstance().getControllerHostname(), DeviceConfig.getInstance().getBroadcastPort());
		//set up an indefinite thread to ping the controller
		new Thread() {
			public void run() {
				while(true) {
					sendToController(
							"/device/alive",
                            new Object[] {
                                    DeviceConfig.getInstance().getMyHostName(),
                                    DeviceConfig.getInstance().getMyAddress(),
                                    Synchronizer.time(),
                                    hb.getStatus()
                            }
                    );
					try {
						Thread.sleep(DeviceConfig.getInstance().getAliveInterval());
					} catch (InterruptedException e) {
						System.out.println("/device/alive message did not getInstance through to controller.");
					}
				}
 				
			}
		}.start();
	}

	/**
	 * Send an OSC message to the controller. This assumes that you have implemented code on the controller side to respond to this message.
	 * @param msg the message name.
	 * @param args the message arguments.
     */
	public void sendToController(String msg, Object[] args) {
		try {
			oscServer.send(new OSCMessage(msg, args), controller);
		} catch (IOException e) {
			System.out.println("Error sending OSC message to Server:");
			e.printStackTrace();
		}
	}

	/**
	 * Broadcast an OSC message to all devices.
	 * @param msg the message name.
	 * @param args the message arguments.
     */
	public void broadcastOSC(String msg, Object[] args) {
		try {
			oscServer.send(new OSCMessage(msg, args), broadcastAddress);
			System.out.println("Sent this message: " + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add a @{@link Listener} that will respond to incoming OSC messages from the controller. Note that this will not listen to broadcast messages from other devices, for which you should use TODO!.
	 * @param l the listener.
     */
	public void addListener(Listener l) {
		listeners.add(l);
	}

	/**
	 * Remove the given {@link Listener}.
	 * @param l the listener to remove.
     */
	public void removeListener(Listener l) {
		listeners.remove(l);
	}

	/**
	 * Clear all @{@link Listener}s.
	 */
	public void clearListeners() {
		listeners.clear();
	}

	/**
	 * Get the ID of this device, as assigned by the controller. If the controller has not yet assigned an ID to this device then the ID will be -1. IDs assigned by the controller will be non-negative integers, either from a configuration file of known devices or allocated incrementally upon request.
	 * @return the ID of this device.
     */
	public int getID() {
		return myID;
	}
	

}