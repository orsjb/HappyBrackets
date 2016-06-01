package net.happybrackets.device.network;

import net.happybrackets.core.Device;
import net.happybrackets.device.config.DeviceConfig;
import net.happybrackets.core.Synchronizer;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;
import net.happybrackets.device.dynamic.HB;

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

	int myID;									//ID assigned by the controller
	private OSCServer oscServer;				//The OSC server
	private InetSocketAddress controller, oscPortDetails;		//The network details of the controller
	private Set<Listener> listeners = Collections.synchronizedSet(new HashSet<Listener>()); 	
																//Listeners to incoming OSC messages
	final private HB hb;
	
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
				//include default listener behaviour that listens for the ID assigned to this PI
				//note technically messages can be sent from anyone, so ignore messages being sent from self...
				//TODO questionable approach, is this escape needed?
				if(src instanceof InetSocketAddress && 
						((InetSocketAddress)src).getHostName().contains(Device.myHostname.split("[.]")[0])) {
					return;
				}
				if(msg.getName().equals("/PI/set_id")) {
					myID = (Integer)msg.getArg(0);
					System.out.println("I have been given an ID by the controller: " + myID);
					hb.setStatus("ID " + myID);
				} else {
					//master commands...
					if(msg.getName().equals("/PI/sync")) {
						long timeToAct = Long.parseLong((String)msg.getArg(0));
						System.out.println(msg.getArg(0).getClass() + " " + msg.getArg(0));
						hb.sync(timeToAct);
					} else if(msg.getName().equals("/PI/reboot")) {
						HB.rebootDevice();
					} else if(msg.getName().equals("/PI/shutdown")) {
						HB.shutdownDevice();
					} else if(msg.getName().equals("/PI/gain")) {
						hb.masterGainEnv.addSegment((Float)msg.getArg(0), (Float)msg.getArg(1));
					} else if(msg.getName().equals("/PI/reset")) {
						hb.reset();
					} else if(msg.getName().equals("/PI/reset_sounding")) {
						hb.resetLeaveSounding();
					} else if(msg.getName().equals("/PI/clearsound")) {
						hb.clearSound();
					} else if(msg.getName().equals("/PI/fadeout_reset")) {
						hb.fadeOutReset((Float)msg.getArg(0));
					} else if(msg.getName().equals("/PI/fadeout_clearsound")) {
						hb.fadeOutClearSound((Float)msg.getArg(0));
					} else if(msg.getName().equals("/PI/bleep")) {
						hb.testBleep();
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
				DeviceConfig.getInstance().getControllerHostname(),
				DeviceConfig.getInstance().getStatusFromDevicePort()
		);
		System.out.println( "Controller resolved to address: " + controller );
		//set up the controller address
		oscPortDetails = new InetSocketAddress(DeviceConfig.getInstance().getControllerHostname(), DeviceConfig.getInstance().getBroadcastOSCPort());
		//set up an indefinite thread to ping the controller
		new Thread() {
			public void run() {
				while(true) {
					sendToController("/PI/alive", new Object[] {DeviceConfig.getInstance().getMyHostName(), Synchronizer.time(), hb.getStatus()});
					try {
						Thread.sleep(DeviceConfig.getInstance().getAliveInterval());
					} catch (InterruptedException e) {
						System.out.println("/PI/alive message did not getInstance through to controller.");
					}
				}
 				
			}
		}.start();
	}

	public void sendToController(String msg, Object[] args) {
		try {
			oscServer.send(new OSCMessage(msg, args), controller);
		} catch (IOException e) {
			System.out.println("Error sending OSC message to Server:");
			e.printStackTrace();
		}
	}

	public void broadcastOSC(String msg, Object[] args) {
		try {
			oscServer.send(new OSCMessage(msg, args), oscPortDetails);
			System.out.println("Sent this message: " + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addListener(Listener l) {
		listeners.add(l);
	}
	
	public void removeListener(Listener l) {
		listeners.remove(l);
	}

	public void clearListeners() {
		listeners.clear();
	}
	
	public int getID() {
		return myID;
	}
	

}