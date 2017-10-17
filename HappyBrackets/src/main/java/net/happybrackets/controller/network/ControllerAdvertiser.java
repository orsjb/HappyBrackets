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
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.core.config.LoadableConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static net.happybrackets.core.BroadcastManager.getBroadcast;

public class ControllerAdvertiser {

	/**
	 * Class that contains a cached message to send to UDP to reduce garbage
	 */
	private class CachedMessage{
		DatagramPacket cachedPacket;
		OSCMessage cachedMessage;
		InetAddress broadcastAddress;

		public CachedMessage (OSCMessage msg, DatagramPacket packet, InetAddress broadcast_address)
		{
			cachedPacket = packet;
			cachedMessage = msg;
			broadcastAddress = broadcast_address;
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
	int replyPort; // leave it undefined so we can see a warning if it does not get assigned

	private Map <Integer, CachedMessage> cachedNetworkMessage;


	CachedMessage cachedMessage = null;

	DatagramSocket broadcastSocket = null;
	ByteBuffer byteBuf;


	/**
	 * Load a set of Broadcast messages in the standard broadcast message fails
	 */
	void loadNetworkBroadcastAdverticements() {
		cachedNetworkMessage.clear();
		OSCMessage msg = new OSCMessage(
				OSCVocabulary.CONTROLLER.CONTROLLER,
				new Object[]{
						Device.getDeviceName(),
						replyPort
				}
		);
		OSCPacketCodec codec = new OSCPacketCodec();

		try {
			byteBuf.clear();
			codec.encode(msg, byteBuf);
			byteBuf.flip();
			byte[] buff = new byte[byteBuf.limit()];
			byteBuf.get(buff);


			List<NetworkInterface> interfaces = Device.viableInterfaces();

			interfaces.forEach(ni -> {

				InetAddress broadcast = BroadcastManager.getBroadcast(ni);


				if (broadcast != null) {
                    try {
                        // Now we are going to broadcast on network interface specific
                        DatagramPacket packet = new DatagramPacket(buff, buff.length, broadcast, replyPort);
                        CachedMessage message = new CachedMessage(msg, packet, broadcast);
                        cachedNetworkMessage.put(ni.hashCode(), message);
                    } catch (Exception ex) {
                        logger.error("Unable to create cached message", ex);
                    }

                }

			});

		} catch (Exception ex) {
			logger.error("Unable to create cached message", ex);
		}

	}


	/**
	 * Create a controller advertiser that also tells the device what port to send reply to
	 * @param reply_port The port we want the device to respond to
	 */
	public ControllerAdvertiser(int reply_port) {

		replyPort = reply_port;
		int port = LoadableConfig.getInstance().getBroadcastPort();
		cachedNetworkMessage = new Hashtable<Integer, CachedMessage>();

		byteBuf	= ByteBuffer.allocateDirect(OSCChannel.DEFAULTBUFSIZE);

		try {
			broadcastSocket = new DatagramSocket();
			broadcastSocket.setBroadcast(true);
			broadcastSocket.setReuseAddress(true);

		} catch (Exception e) {
			e.printStackTrace();
		}

		OSCMessage msg = new OSCMessage(
				OSCVocabulary.CONTROLLER.CONTROLLER,
				new Object[]{
						Device.getDeviceName(),
						replyPort
				}
		);
		OSCPacketCodec codec = new OSCPacketCodec();

		try {
			byteBuf.clear();
			codec.encode(msg, byteBuf);
			byteBuf.flip();
			byte[] buff = new byte[byteBuf.limit()];
			byteBuf.get(buff);
			InetAddress broadcast = InetAddress.getByName("255.255.255.255");

			// Now we are going to broadcast on network interface specific
			DatagramPacket packet = new DatagramPacket(buff, buff.length, broadcast, port);
			cachedMessage = new CachedMessage(msg, packet, broadcast);
			loadNetworkBroadcastAdverticements();
		}
		catch (Exception ex){
			logger.error("Unable to create cached message", ex);
		}

		//set up an indefinite thread to advertise the controller
		advertisementService = new Thread() {
			public void run() {

            while (keepAlive) {
				DatagramPacket packet = cachedMessage.getCachedPacket();

				// Now send a broadcast
				try {
					broadcastSocket.send(packet);
				} catch (Exception ex) {
					System.out.println(ex.getMessage());

					try{
						cachedNetworkMessage.forEach(new BiConsumer<Integer, CachedMessage>() {
							@Override
							public void accept(Integer integer, CachedMessage cachedMessage) {
								DatagramPacket packet = cachedMessage.cachedPacket;
								try {
									broadcastSocket.send(packet);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						});
					}
					catch (Exception ex_all)
					{

					}
				}
                try {
                    Thread.sleep(1000);
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
