/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.device.network;

//import com.intellij.ide.ui.EditorOptionsTopHitProvider;
import de.sciss.net.*;
import net.happybrackets.controller.network.ControllerAdvertiser;
import net.happybrackets.core.*;
import net.happybrackets.device.LogSender;
import net.happybrackets.device.config.DeviceConfig;
import net.happybrackets.device.HB;
import net.happybrackets.device.config.LocalConfigManagement;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.happybrackets.core.BroadcastManager.getBroadcast;

/**
 * This class takes care of communication between the device and the controller. You would mainly use it to send OSC messages to the controller and listen for incoming OSC messages from the controller. However, these methods are both wrapped in the {@link HB} class.
 */
public class NetworkCommunication {



	final static Logger logger = LoggerFactory.getLogger(NetworkCommunication.class);

	private int myID;							//ID assigned by the controller
	private OSCServer oscServer;				//The OSC server
	private InetSocketAddress broadcastAddress;		//The network details of the controller
	private Set<OSCListener> listeners = Collections.synchronizedSet(new HashSet<OSCListener>());
																//Listeners to incoming OSC messages
	final private HB hb;

	private final LogSender logSender;




	/**
	 * Instantiate a new {@link NetworkCommunication} object.
	 * @param _hb the {@link HB} object this object is attached to.
	 * @throws IOException thrown if there is a problem opening the {@link OSCServer}, likely due to the port already being in use.
     */
	public NetworkCommunication(HB _hb) throws IOException {
		this.hb = _hb;


		//init the OSCServer
		logger.info("Setting up OSC server");
		try {
			oscServer = OSCServer.newUsing(OSCServer.UDP, DeviceConfig.getInstance().getControlToDevicePort());
			oscServer.start();
		} catch (IOException e) {
			logger.error("Error creating OSC server!", e);
		}
		logger.info("Started OSC server");

		// Create log sender.
		logSender = new LogSender(this,  DeviceConfig.getInstance().getLogFilePath());

		//add a single master listener that forwards listening to delegates
		oscServer.addOSCListener(new OSCListener() {
			@Override
			public void messageReceived(OSCMessage msg, SocketAddress src, long time) {
				//include default listener behaviour that listens for the ID assigned to this device
				//note technically messages can be sent from anyone, so ignore messages being sent from self...
//				//TODO note the following has been removed because in fact we do want to allow messages from self
//				if(src instanceof InetSocketAddress &&
//						((InetSocketAddress)src).getHostName().contains(DeviceConfig.getInstance().getMyHostName().split("[.]")[0])) {
//					return;
//				}
//				System.out.println("Mesage received: " + msg.getName());

				InetAddress sending_address = ((InetSocketAddress) src).getAddress();

                logger.debug("Recieved message to: {} from {}", msg.getName(), src.toString());

				if(OSCVocabulary.match(msg, OSCVocabulary.Device.SET_ID)) {
					myID = (Integer)msg.getArg(0);
					logger.info("I have been given an ID by the controller: {}", myID);
					hb.setStatus("ID " + myID);
				} else if(OSCVocabulary.match(msg, OSCVocabulary.Device.GET_LOGS)) {
					boolean sendLogs = ((Integer) msg.getArg(0)) == 1;
					logger.info("I have been requested to " + (sendLogs ? "start" : "stop") + " sending logs to the controller.");
					sendLogs(sendLogs);
				} else {
					//master commands...
					if(OSCVocabulary.match(msg, OSCVocabulary.Device.SYNC)) {
						long timeToAct = 1000;
						if(msg.getArgCount() > 0) {
							timeToAct = (Integer)msg.getArg(0);
						}
						hb.syncAudioStart(timeToAct);
					} else if(OSCVocabulary.match(msg, OSCVocabulary.Device.REBOOT)) {
						HB.rebootDevice();
					} else if(OSCVocabulary.match(msg, OSCVocabulary.Device.SHUTDOWN)) {
						HB.shutdownDevice();
					} else if(OSCVocabulary.match(msg, OSCVocabulary.Device.GAIN)) {
						hb.masterGainEnv.addSegment((Float)msg.getArg(0), (Float)msg.getArg(1));
					} else if(OSCVocabulary.match(msg, OSCVocabulary.Device.RESET)) {
						hb.reset();
					} else if(OSCVocabulary.match(msg, OSCVocabulary.Device.RESET_SOUNDING)) {
						hb.resetLeaveSounding();
					} else if(OSCVocabulary.match(msg, OSCVocabulary.Device.CLEAR_SOUND)) {
						hb.clearSound();
					} else if(OSCVocabulary.match(msg, OSCVocabulary.Device.FADEOUT_RESET)) {
						hb.fadeOutReset((Float)msg.getArg(0));
					} else if(OSCVocabulary.match(msg, OSCVocabulary.Device.FADEOUT_CLEAR_SOUND)) {
						hb.fadeOutClearSound((Float)msg.getArg(0));
					} else if(OSCVocabulary.match(msg, OSCVocabulary.Device.BLEEP)) {
						hb.testBleep();
					} else if(OSCVocabulary.match(msg, OSCVocabulary.Device.STATUS)) {
						send(OSCVocabulary.Device.STATUS,
								new Object[] {
										Device.getDeviceName(),
										hb.getStatus()
								},
								sending_address);
					} else if(OSCVocabulary.match(msg, OSCVocabulary.Device.VERSION)) {
						send(OSCVocabulary.Device.VERSION,
								new Object[] {
										Device.getDeviceName(),
										BuildVersion.getMajor(),
										BuildVersion.getMinor(),
										BuildVersion.getBuild(),
										BuildVersion.getDate()
								},
								sending_address);

						System.out.println("Version sent " + BuildVersion.getVersionText());

					} else if ( OSCVocabulary.match(msg, OSCVocabulary.Device.CONFIG_WIFI) && msg.getArgCount() == 2) {
                        //TODO: add interfaces path to device config
                        boolean status = LocalConfigManagement.updateInterfaces(
                                "/etc/network/interfaces",
                                (String) msg.getArg(0),
                                (String) msg.getArg(1)
                        );
                        if (status) logger.info("Updated interfaces file");
                        else logger.error("Unable to update interfaces file");
					} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.ALIVE)) {
						//ignore
					} else {
						//all other messages getInstance forwarded to delegate listeners
						synchronized (listeners) {
							Iterator<OSCListener> i = listeners.iterator();
							while (i.hasNext()) {
								try {
									i.next().messageReceived(msg, src, time);
								} catch (Exception e) {
									logger.error("Error delegating OSC message!", e);
								}
							}
						}
					}
				}
			}
		});
		/*
		//set up the controller address
		String hostname = DeviceConfig.getInstance().getControllerHostname();
		logger.info( "Setting up controller: {}", hostname );
		controller = new InetSocketAddress(
				DeviceConfig.getInstance().getControllerAddress(),
				DeviceConfig.getInstance().getStatusFromDevicePort()
		);
		logger.debug( "Controller resolved to address: {}", controller );
		*/

		//set up an indefinite thread to ping the controller
        new Thread() {
            public void run() {
            BroadcastManager.OnTransmitter keepAlive = new BroadcastManager.OnTransmitter() {
                @Override
                public void cb(NetworkInterface ni, OSCTransmitter transmitter) throws IOException {
                	/*
					int ni_hash = ni.hashCode();
					CachedMessage cached_message = cachedNetworkMessage.get(ni_hash);

					if (ni.isUp()) {
						// Now we are going to broadcast on network interface specific
						InetAddress broadcast = getBroadcast(ni);

						try {

							if (cached_message != null) {
								if (hb.myIndex() != cached_message.getDeviceId() || !broadcast.equals(cached_message.broadcastAddress)) {
									cachedNetworkMessage.remove(ni_hash);
									cached_message = null;
								}
							}
						}catch(Exception ex){}
						if (cached_message == null) {

							OSCMessage msg = new OSCMessage(
									OSCVocabulary.Device.ALIVE,
									new Object[]{
											Device.getDeviceName(),
											Device.selectHostname(ni),
											Device.selectIP(ni),
											hb.myIndex()
									}
							);

							OSCPacketCodec codec = transmitter.getCodec();

							byteBuf.clear();
							codec.encode(msg, byteBuf);
							byteBuf.flip();
							byte[] buff = new byte[byteBuf.limit()];
							byteBuf.get(buff);


							if (broadcast != null) {
								try {
									DatagramPacket packet = new DatagramPacket(buff, buff.length, broadcast, _hb.broadcast.getPort());
									cached_message = new CachedMessage(msg, packet, hb.myIndex(), broadcast);
									cachedNetworkMessage.put(ni_hash, cached_message);

								} catch (Exception ex) {
								}
							}


						}

						transmitter.send(
								cached_message.getCachedMessage()
						);

						DatagramPacket packet = cached_message.getCachedPacket();

						// Now send a broadcast
						try {
							broadcastSocket.send(packet);
						} catch (Exception ex) {
							System.out.println(ex.getMessage());
						}
					} // ni.isUp()
					else if (cached_message != null) {
						// ni is not up remove ni as broadcast address may change
						cachedNetworkMessage.remove(ni_hash);
					}
*/
				}
            };

            while(true) {
                //hb.broadcast.forAllTransmitters(keepAlive);
                // we should send to all registered controllers
				DeviceConfig.getInstance().notifyAllControllers();

                try {
                    Thread.sleep(DeviceConfig.getInstance().getAliveInterval());
                } catch (InterruptedException e) {
                    logger.error(OSCVocabulary.Device.ALIVE + " message send interval interupted!", e);
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
	public void send(String msg, Object[] args) {
		try {
			DeviceConfig config = DeviceConfig.getInstance();

			config.getDeviceControllers().forEach((hash, controller) ->
					{
						InetSocketAddress address = controller.getAddress();

						try {
							oscServer.send(
                                    new OSCMessage(msg, args),
    						address

                            );
						} catch (IOException e) {
							logger.error("Error sending OSC message to Server!", e);
						}
					}
			);



		} catch (Exception ex) {
			logger.error("Error sending OSC message to Server!", ex);
		}
	}

	/**
	 * Send an OSC message to an Address other than the one we have configured as our controller
	 * @param msg the message name
	 * @param args the message arguments
	 * @param requester the Address of the device making request
	 */
	public void send (String msg, Object[] args, InetAddress requester)
	{
		try {
			oscServer.send(
					new OSCMessage(msg, args),
					new InetSocketAddress(
							requester.getHostAddress(),
							DeviceConfig.getInstance().getStatusFromDevicePort()
					)
			);
		} catch (IOException e) {
			logger.error("Error sending OSC message to Server!", e);
		}
	}

	/**
	 * Add a @{@link OSCListener} that will respond to incoming OSC messages from the controller. Note that this will not listen to broadcast messages from other devices, for which you should use TODO!.
	 * @param l the listener.
     */
	public void addListener(OSCListener l) {
		listeners.add(l);
	}

	/**
	 * Remove the given {@link OSCListener}.
	 * @param l the listener to remove.
     */
	public void removeListener(OSCListener l) {
		listeners.remove(l);
	}

	/**
	 * Clear all @{@link OSCListener}s.
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


	/**
	 * Start or stop sending log messages to the controller.
	 * On the first start the current log file contents will be sent.
	 * Upon subsequent starts any new log messages will be sent that were created since the last stop.
	 * Until the process is stopped, as new log messages appear they will be sent to the controller.
	 *
	 * @param sendLogs true to start, false to stop.
	 */
	public void sendLogs(boolean sendLogs) {
		logSender.setSend(sendLogs);
	}
}
