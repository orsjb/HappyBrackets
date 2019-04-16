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
import net.happybrackets.core.*;
import net.happybrackets.core.config.DefaultConfig;
import net.happybrackets.core.control.ControlMap;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.LogSender;
import net.happybrackets.device.config.DeviceConfig;
import net.happybrackets.device.HB;
import net.happybrackets.device.config.LocalConfigManagement;

import java.io.IOException;
import java.net.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class takes care of communication between the device and the controller. You would mainly use it to send OSC messages to the controller and listen for incoming OSC messages from the controller. However, these methods are both wrapped in the {@link HB} class.
 */
public class NetworkCommunication {



	final static Logger logger = LoggerFactory.getLogger(NetworkCommunication.class);

	private OSCServer oscServer;				//The OSC server
	private InetAddress broadcastAddress;		//Global Broadcast address
	private Set<OSCListener> listeners = Collections.synchronizedSet(new HashSet<OSCListener>());

	/**
	 * Class for receiving non java files from network
	 */
	public FileReceiver fileReceiver = null;															//Listeners to incoming OSC messages

	// Define a set of Sockets that will receive dynamic Control events. These will only be controllers who have done a get request
	private Set<SocketAddress> dynamicControlControllerListeners =  Collections.synchronizedSet(new HashSet<SocketAddress>());

	final private HB hb;

	private OSCServer controllerOscServer;
	private final LogSender logSender;
	DatagramSocket advertiseTxSocket = null;


	/**
	 * send SC Message to each controller that has registered
	 * @param msg the Message to send to the controller
	 */
	private void sendDynamicControlEventToControllers(OSCMessage msg){
		try
		{
			synchronized (dynamicControlControllerListeners) {
				ArrayList<SocketAddress> failed_addresses = new ArrayList<SocketAddress>();
				Iterator<SocketAddress> i = dynamicControlControllerListeners.iterator();
				while (i.hasNext()) {
					SocketAddress socket =  i.next();
					if (!send(msg, socket)){
						// this was a fail. Lets remove the socket
						failed_addresses.add(socket);
					}

				}

				// we are out of our iterator. Now remove our failed sockets
				for (SocketAddress socket: failed_addresses) {
					dynamicControlControllerListeners.remove(socket);

				}
			}
		}
		catch(Exception ex){}
	}

	/**
	 * Instantiate a new {@link NetworkCommunication} object.
	 * @param _hb the {@link HB} object this object is attached to.
	 * @throws IOException thrown if there is a problem opening the {@link OSCServer}, likely due to the port already being in use.
     */
	public NetworkCommunication(HB _hb) throws IOException {
		this.hb = _hb;

		hb.setUseEncryption(DeviceStatus.getInstance().isClassEncryption());

		broadcastAddress = BroadcastManager.getBroadcast(null);

		try {
			advertiseTxSocket = new DatagramSocket();
			advertiseTxSocket.setBroadcast(true);
		}
		catch (Exception ex){}

		ControlMap.getInstance().addDynamicControlAdvertiseListener(new ControlMap.dynamicControlAdvertiseListener() {
			@Override
			public void dynamicControlEvent(OSCMessage msg) {
				// Send all Dynamic Control Messages to the respective controllers
				sendDynamicControlEventToControllers(msg);
			}
		});


		ControlMap.getInstance().addGlobalDynamicControlAdvertiseListener(new ControlMap.dynamicControlAdvertiseListener() {
			@Override
			public void dynamicControlEvent(OSCMessage msg) {
				// Send all Dynamic Control Messages to the Broadcast Address so all will receive them
				try {
					if (DeviceConfig.getInstance() != null) {
						UDPCachedMessage cached_message = new UDPCachedMessage(msg);
						// We need to send message On broadcast channel on the standard listening port. We are sending to device, not controller
						// this is why we are using control to device port
						if (advertiseTxSocket != null) {
							int device_port = DeviceConfig.getInstance().getControlToDevicePort();
							DatagramPacket packet = cached_message.getCachedPacket();
							packet.setAddress(broadcastAddress);
							packet.setPort(device_port);
							advertiseTxSocket.send(packet);
						}

						// Now send the message to all controllers
						sendDynamicControlEventToControllers(msg);
						//DeviceConfig.getInstance().sendMessageToAllControllers(cached_message.getCachedPacket());
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});


		_hb.addStatusChangedListener(new HB.StatusChangedListener() {
			@Override
			public void statusChanged(String new_status) {

				if (DeviceConfig.getInstance() != null) {
					DeviceStatus.getInstance().setStatusText(new_status);
					DeviceConfig.getInstance().sendMessageToAllControllers(DeviceStatus.getInstance().getCachedStatusMessage().cachedPacket);
				}
			}

			@Override
			public void classLoadedMessage(Class<? extends HBAction> incomingClass) {
				if (DeviceConfig.getInstance() != null) {
					String simple_class_name = incomingClass.getSimpleName();
					String full_class_name = incomingClass.getName();

					OSCMessage oscMessage = OSCMessageBuilder.createOscMessage(OSCVocabulary.Device.CLASS_LOADED, simple_class_name, full_class_name);

					UDPCachedMessage message = null;
					try {
						message = new UDPCachedMessage(oscMessage);
						DeviceConfig.getInstance().sendMessageToAllControllers(message.getCachedPacket());
					} catch (IOException e) {
						e.printStackTrace();
					}


				}
			}
		});


		//init the OSCServer
		logger.info("Setting up OSC server");
		try {
			if (DeviceConfig.getInstance() == null) {
				oscServer = OSCServer.newUsing(OSCServer.UDP, DefaultConfig.CONTROL_TO_DEVICE_PORT);
			}
			else {
				oscServer = OSCServer.newUsing(OSCServer.UDP, DeviceConfig.getInstance().getControlToDevicePort());
			}
			oscServer.start();
		} catch (IOException e) {
			logger.error("Error creating OSC server!", e);
		}
		logger.info("Started OSC server");

		controllerOscServer =  OSCServer.newUsing(OSCServer.TCP, 0);

		int oscPort =  controllerOscServer.getLocalAddress().getPort();

		hb.setTCPServerPort(oscPort);
		fileReceiver = new FileReceiver(controllerOscServer);
		System.out.println("Local controllerOscServer Port " + oscPort);

		if (DeviceConfig.getInstance() != null) {
			// Create log sender.
			logSender = new LogSender(this, DeviceConfig.getInstance().getLogFilePath());
		}
		else {
			logSender = null;
		}
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
//				System.out.println("Message received: " + msg.getName());

				// this is our default target ports
				int target_port = DefaultConfig.STATUS_FROM_DEVICE_PORT;

				if (DeviceConfig.getInstance() != null)
				{
					DeviceConfig.getInstance().getStatusFromDevicePort();
				}
				try {
					InetAddress sending_address = ((InetSocketAddress) src).getAddress();

					logger.debug("Recieved message to: {} from {}", msg.getName(), src.toString());

					if (OSCVocabulary.match(msg, OSCVocabulary.Device.SET_ID)) {
						int new_id = (Integer) msg.getArg(0);
						hb.setMyIndex(new_id);
						logger.info("I have been given an ID by the controller: {}", new_id);
						hb.setStatus("ID " + new_id);


					} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.SET_NAME)) {
						String name = (String) msg.getArg(0);
						hb.setFriendlyName(name);
						logger.info("I have been given a friendly name by the controller: {}", name);
					}
					else if (OSCVocabulary.match(msg, OSCVocabulary.Device.GET_LOGS)) {
						boolean enabled = ((Integer) msg.getArg(0)) == 1;
						logger.info("I have been requested to " + (enabled ? "start" : "stop") + " sending logs to the controller.");
						sendLogs(enabled);
					} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.SET_ENCRYPTION)) {
						boolean enabled = ((Integer) msg.getArg(0)) == 1;
						DeviceStatus.getInstance().setClassEncryption(enabled);
						hb.setUseEncryption(enabled);

					} else {
						//master commands...
						if (OSCVocabulary.match(msg, OSCVocabulary.Device.SYNC)) {
							int intervalForSynchAction = 1000;
							if (msg.getArgCount() > 0) {
								intervalForSynchAction = (Integer) msg.getArg(0);
							}
							hb.syncAudioStart(intervalForSynchAction);
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.REBOOT)) {
							HB.rebootDevice();
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.SHUTDOWN)) {
							HB.shutdownDevice();
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.GAIN)) {
							hb.masterGainEnv.addSegment((Float) msg.getArg(0), (Float) msg.getArg(1));
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.RESET)) {
							boolean has_classes = hb.hasClassesLoaded();
							hb.reset();

							// If we have pressed reset previously, we will also mute audio
							if (!has_classes) {
								hb.muteAudio(true);
							}
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.RESET_SOUNDING)) {
							hb.resetLeaveSounding();
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.CLEAR_SOUND)) {
							hb.clearSound();
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.FADEOUT_RESET)) {
							hb.fadeOutReset((Float) msg.getArg(0));
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.FADEOUT_CLEAR_SOUND)) {
							hb.fadeOutClearSound((Float) msg.getArg(0));
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.BLEEP)) {
							hb.testBleep();
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.STATUS)) {
							if (msg.getArgCount() > 0) {
								target_port = (Integer) msg.getArg(0);
							}

							InetSocketAddress target_address  =  new InetSocketAddress(sending_address.getHostAddress(), target_port);

							send(DeviceStatus.getInstance().getOSCMessage(),
									target_address);

						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.VERSION)) {
							if (msg.getArgCount() > 0) {
								target_port = (Integer) msg.getArg(0);
							}
							InetSocketAddress target_address  =  new InetSocketAddress(sending_address.getHostAddress(), target_port);

							send(OSCVocabulary.Device.VERSION,
									new Object[]{
											Device.getDeviceName(),
											BuildVersion.getMajor(),
											BuildVersion.getMinor(),
											BuildVersion.getBuild(),
											BuildVersion.getCompile()
									},
									target_address);

							System.out.println("Version sent " + BuildVersion.getVersionText() + " to port " + target_port) ;

						}
						else if (OSCVocabulary.match(msg, OSCVocabulary.Device.FRIENDLY_NAME)) {
							if (msg.getArgCount() > 0) {
								target_port = (Integer) msg.getArg(0);
							}
							InetSocketAddress target_address  =  new InetSocketAddress(sending_address.getHostAddress(), target_port);

							send(OSCVocabulary.Device.FRIENDLY_NAME,
									new Object[]{
											Device.getDeviceName(),
											hb.friendlyName()
									},
									target_address);

							System.out.println("Name sent " + BuildVersion.getVersionText() + " to port " + target_port) ;

						}


						else if (OSCVocabulary.match(msg, OSCVocabulary.DynamicControlMessage.GET)) {
							if (msg.getArgCount() > 0) {
								target_port = (Integer) msg.getArg(0);
							}

							InetSocketAddress target_address = new InetSocketAddress(sending_address.getHostAddress(), target_port);


							ControlMap control_map = ControlMap.getInstance();

							List<DynamicControl> controls = control_map.GetSortedControls();

							for (DynamicControl control : controls) {
								if (control != null) {
									OSCMessage send_msg = control.buildCreateMessage();
									send(send_msg, target_address);
								}
							}




						}
						else if (OSCVocabulary.match(msg, OSCVocabulary.DynamicControlMessage.UPDATE)){
							DynamicControl.processUpdateMessage(msg);
						}
						else if (OSCVocabulary.match(msg, OSCVocabulary.DynamicControlMessage.GLOBAL)){
							DynamicControl.processGlobalMessage(msg);
						}
						else if (OSCVocabulary.match(msg, OSCVocabulary.Device.CONFIG_WIFI) && msg.getArgCount() == 2) {
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
				} catch (Exception ex)
				{
					logger.error("Error processing OSC message!", ex);
				}

			}
		});

		controllerOscServer.start();



		//add a single master listener that forwards listening to delegates
		controllerOscServer.addOSCListener(new OSCListener() {
			@Override
			public void messageReceived(OSCMessage msg, SocketAddress src, long time) {
				//include default listener behaviour that listens for the ID assigned to this device

				try {

					if (OSCVocabulary.match(msg, OSCVocabulary.Device.SET_ID)) {
						int new_id = (Integer) msg.getArg(0);
						hb.setMyIndex(new_id);
						logger.info("I have been given an ID by the controller: {}", new_id);
						hb.setStatus("ID " + new_id);


					} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.SET_NAME)) {
						String name = (String) msg.getArg(0);
						hb.setFriendlyName(name);
						logger.info("I have been given a friendly name by the controller: {}", name);
					}
					else if (OSCVocabulary.match(msg, OSCVocabulary.Device.GET_LOGS)) {
						boolean enabled = ((Integer) msg.getArg(0)) == 1;
						logger.info("I have been requested to " + (enabled ? "start" : "stop") + " sending logs to the controller.");
						sendLogs(enabled);
					} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.SET_ENCRYPTION)) {
						boolean enabled = ((Integer) msg.getArg(0)) == 1;
						DeviceStatus.getInstance().setClassEncryption(enabled);
						hb.setUseEncryption(enabled);

					} else {
						//master commands...
						if (OSCVocabulary.match(msg, OSCVocabulary.Device.SYNC)) {
							int intervalForSynchAction = 1000;
							if (msg.getArgCount() > 0) {
								intervalForSynchAction = (Integer) msg.getArg(0);
							}
							hb.syncAudioStart(intervalForSynchAction);
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.REBOOT)) {
							HB.rebootDevice();
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.SHUTDOWN)) {
							HB.shutdownDevice();
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.GAIN)) {
							hb.masterGainEnv.addSegment((Float) msg.getArg(0), (Float) msg.getArg(1));
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.RESET)) {
							boolean has_classes = hb.hasClassesLoaded();
							hb.reset();

							// If we have pressed reset previously, we will also mute audio
							if (!has_classes) {
								hb.muteAudio(true);
							}
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.RESET_SOUNDING)) {
							hb.resetLeaveSounding();
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.CLEAR_SOUND)) {
							hb.clearSound();
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.FADEOUT_RESET)) {
							hb.fadeOutReset((Float) msg.getArg(0));
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.FADEOUT_CLEAR_SOUND)) {
							hb.fadeOutClearSound((Float) msg.getArg(0));
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.BLEEP)) {
							hb.testBleep();
						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.STATUS)) {

							send(DeviceStatus.getInstance().getOSCMessage(),
									src);

						} else if (OSCVocabulary.match(msg, OSCVocabulary.Device.VERSION)) {

							send(OSCVocabulary.Device.VERSION,
									new Object[]{
											Device.getDeviceName(),
											BuildVersion.getMajor(),
											BuildVersion.getMinor(),
											BuildVersion.getBuild(),
											BuildVersion.getCompile()
									},
									src);

							System.out.println("Version sent " + BuildVersion.getVersionText() + " to tcp " ) ;

						}
						else if (OSCVocabulary.match(msg, OSCVocabulary.Device.FRIENDLY_NAME)) {

							send(OSCVocabulary.Device.FRIENDLY_NAME,
									new Object[]{
											Device.getDeviceName(),
											hb.friendlyName()
									},
									src);

							System.out.println("Name sent " + BuildVersion.getVersionText() + " to tcp ") ;

						}


						else if (OSCVocabulary.match(msg, OSCVocabulary.DynamicControlMessage.GET)) {

							ControlMap control_map = ControlMap.getInstance();

							List<DynamicControl> controls = control_map.GetSortedControls();

							for (DynamicControl control : controls) {
								if (control != null) {
									OSCMessage send_msg = control.buildCreateMessage();
									send(send_msg, src);
								}
							}
							synchronized (dynamicControlControllerListeners) {
								dynamicControlControllerListeners.add(src);
							}

						}
						else if (OSCVocabulary.match(msg, OSCVocabulary.DynamicControlMessage.UPDATE)){
							DynamicControl.processUpdateMessage(msg);
						}
						else if (OSCVocabulary.match(msg, OSCVocabulary.DynamicControlMessage.GLOBAL)){
							DynamicControl.processGlobalMessage(msg);
						}
						else if (OSCVocabulary.match(msg, OSCVocabulary.Device.CONFIG_WIFI) && msg.getArgCount() == 2) {
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
				} catch (Exception ex)
				{
					logger.error("Error processing OSC message!", ex);
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

            	int default_alive_time = DefaultConfig.ALIVE_INTERVAL;

            	if (DeviceConfig.getInstance() != null) {
					//hb.broadcast.forAllTransmitters(keepAlive);
					// we should send to all registered controllers
					DeviceConfig.getInstance().notifyAllControllers();
					default_alive_time = DeviceConfig.getInstance().getAliveInterval();

					// if we set our alive time to zero, we will not send alive messages back but will act dumb
					if (default_alive_time == 0){
						System.out.println("Disable Keep Alive poll back to controller");
						break;
					}
				}
                try {
                    Thread.sleep(default_alive_time);
                } catch (InterruptedException e) {
                    logger.error(OSCVocabulary.Device.ALIVE + " message send interval interupted!", e);
                }
            }
			}
		}.start();
	}


	/**
	 * Send an OSC message to the controller. This assumes that you have implemented code on the controller side to respond to this message.
	 * @param msg_name the message name.
	 * @param args the message arguments.
     */
	public void send(String msg_name, Object[] args) {
		try {
			DeviceConfig config = DeviceConfig.getInstance();
			if (config != null) {
				OSCMessage msg = new OSCMessage(msg_name, args);

				UDPCachedMessage cached_message = new UDPCachedMessage(msg);
				DeviceConfig.getInstance().sendMessageToAllControllers(cached_message.getCachedPacket());
			}
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
	public void send (String msg, Object[] args, InetSocketAddress requester)
	{
		send(
				new OSCMessage(msg, args),
				requester
		);
	}
	/**
	 * Send an OSC message to an Address other than the one we have configured as our controller
	 * @param msg the message name
	 * @param args the message arguments
	 * @param requester the Address of the device making request
	 */
	public void send (String msg, Object[] args, SocketAddress requester)
	{
		send(
				new OSCMessage(msg, args),
				requester
		);
	}

	/**
	 * Send a Built OSC Message to server
	 * @param msg OSC Message
	 * @param target, where we need to send message
	 */
	public void send (OSCMessage msg, InetSocketAddress target)
	{
		try {
			oscServer.send(msg,
					target
			);
		} catch (IOException e) {
			logger.error("Error sending OSC message to Server!", e);
		}

	}

	/**
	 * Send a Built OSC Message to server
	 * @param msg OSC Message
	 * @param target, where we need to send message
	 * @return true if able to send and no exception thrown
	 */
	public boolean send (OSCMessage msg, SocketAddress target)
	{
		boolean ret = true;
		try {
			controllerOscServer.send(msg,
					target
			);
		} catch (IOException e) {
			ret = false;
			logger.error("Error sending OSC message to Server!", e);
		}

		return ret;
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
		return hb.myIndex();
	}


	/**
	 * Start or stop sending log messages to the controller.
	 * On the first start the current log file contents will be sent.
	 * Upon subsequent starts any new log messages will be sent that were created since the last stop.
	 * Until the process is stopped, as new log messages appear they will be sent to the controller.
	 *
	 * @param send_logs true to start, false to stop.
	 */
	public void sendLogs(boolean send_logs) {
		if (logSender != null) {
			logSender.setSend(send_logs);
			DeviceStatus.getInstance().setLoggingEnabled(send_logs);
		}
	}
}
