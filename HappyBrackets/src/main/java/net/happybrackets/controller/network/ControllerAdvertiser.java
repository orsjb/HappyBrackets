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

package net.happybrackets.controller.network;

import de.sciss.net.*;
import net.happybrackets.core.BroadcastManager;

import net.happybrackets.core.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

public class ControllerAdvertiser {

	/**
	 * Class that contains a cached message to send to UDP to reduce garbage
	 */
	private class CachedMessage{
		DatagramPacket cachedPacket;
		OSCMessage cachedMessage;

		public CachedMessage (OSCMessage msg, DatagramPacket packet)
		{
			cachedPacket = packet;
			cachedMessage = msg;
		}

		/**
		 * Get the cached packet
		 * @return
		 */
		public DatagramPacket getCachedPacket() {
			return cachedPacket;
		}

		/**
		 * The cached OSC Message
		 * @return the msg
		 */
		public OSCMessage getCachedMessage() {
			return cachedMessage;
		}
	}

	final static Logger logger = LoggerFactory.getLogger(ControllerAdvertiser.class);

	private Thread advertisementService;

	private boolean keepAlive = true;


	private Map <Integer, CachedMessage> cachedNetworkMessage;

	DatagramSocket broadcastSocket = null;
	ByteBuffer byteBuf;

	//set up an indefinite thread to advertise the controller
	DatagramSocket finalBroadcastSocket = broadcastSocket;
	public ControllerAdvertiser(BroadcastManager broadcast_manager) {

		cachedNetworkMessage = new Hashtable<Integer, CachedMessage>();

		byteBuf	= ByteBuffer.allocateDirect(OSCChannel.DEFAULTBUFSIZE);

		try {
			broadcastSocket = new DatagramSocket();
			broadcastSocket.setBroadcast(true);
			broadcastSocket.setReuseAddress(true);

		} catch (Exception e) {
			e.printStackTrace();
		}


		//set up an indefinite thread to advertise the controller
		advertisementService = new Thread() {
			public void run() {
            BroadcastManager.OnTransmitter advertisement = new BroadcastManager.OnTransmitter() {
                @Override
                public void cb(NetworkInterface ni, OSCTransmitter transmitter) throws IOException {
					try {
						int ni_hash = ni.hashCode();

						CachedMessage cached_message = cachedNetworkMessage.get(ni_hash);

						// no point doing it if interface is not up
						if (ni.isUp()) {

							if (cached_message == null) {
								OSCMessage msg = new OSCMessage(
										"/hb/controller",
										new Object[]{
												Device.selectHostname(ni),
												transmitter.getLocalAddress().getPort()
										}
								);
								OSCPacketCodec codec = transmitter.getCodec();

								byteBuf.clear();
								codec.encode(msg, byteBuf);
								byteBuf.flip();
								byte[] buff = new byte[byteBuf.limit()];
								byteBuf.get(buff);

								// Now we are going to broadcast on network interface specific
								if (!ni.isLoopback()) {
									// Now do broadcast for that NI
									for (InterfaceAddress interface_address : ni.getInterfaceAddresses()) {
										InetAddress broadcast = interface_address.getBroadcast();
										if (broadcast == null) {
											continue;
										}
										try {
											DatagramPacket packet = new DatagramPacket(buff, buff.length, broadcast, broadcast_manager.getPort());
											cached_message = new CachedMessage(msg, packet);
											cachedNetworkMessage.put(ni_hash, cached_message);

										} catch (Exception ex) {
										}
									}
								} else // just use normal broadcast
								{
									DatagramPacket packet = new DatagramPacket(buff, buff.length, InetAddress.getByName("255.255.255.255"), broadcast_manager.getPort());
									cached_message = new CachedMessage(msg, packet);
									cachedNetworkMessage.put(ni_hash, cached_message);
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

						} // ni.isUp
						else if (cached_message != null) {
							// ni is not up remove ni as broadcast address may change
							cachedNetworkMessage.remove(ni_hash);
						}
					}
					catch (Exception ex){

					}
				}
            };


            while (keepAlive) {
				broadcast_manager.forAllTransmitters(advertisement);

                try {
                    Thread.sleep(500);
                }
                catch (InterruptedException e) {
                    logger.error("Sleep was interupted in ControllerAdvertiser thread", e);
                }
            }
			}
		};
	}

	public void start() {
		keepAlive = true;
		advertisementService.start();
	}

	public void stop() {
		keepAlive = false;
	}

	public void interrupt() {
		advertisementService.interrupt();
	}

	public boolean isAlive() {
		return advertisementService.isAlive();
	}
}
