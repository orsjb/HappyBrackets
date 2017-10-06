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

public class ControllerAdvertiser {

	final static Logger logger = LoggerFactory.getLogger(ControllerAdvertiser.class);

	private Thread advertisementService;

	private boolean keepAlive = true;

	DatagramSocket broadcastSocket = null;
	ByteBuffer byteBuf;

	//set up an indefinite thread to advertise the controller
	DatagramSocket finalBroadcastSocket = broadcastSocket;
	public ControllerAdvertiser(BroadcastManager broadcast_manager) {


		byteBuf	= ByteBuffer.allocateDirect(OSCChannel.DEFAULTBUFSIZE);

		try {
			broadcastSocket = new DatagramSocket();
			broadcastSocket.setBroadcast(true);
			broadcastSocket.setReuseAddress(true);
			broadcastSocket.connect(InetAddress.getByName("255.255.255.255"), broadcast_manager.getPort());

		} catch (Exception e) {
			e.printStackTrace();
		}


		//set up an indefinite thread to advertise the controller
		advertisementService = new Thread() {
			public void run() {
            BroadcastManager.OnTransmitter advertisement = new BroadcastManager.OnTransmitter() {
                @Override
                public void cb(NetworkInterface ni, OSCTransmitter transmitter) throws IOException {
					// no point doing it if interface is not up
                	if (ni.isUp()) {

						OSCPacket msg = new OSCMessage(
								"/hb/controller",
								new Object[]{
										Device.selectHostname(ni),
										Device.selectIP(ni)
								}
						);

						OSCPacketCodec codec = transmitter.getCodec();

						byteBuf.clear();
						codec.encode(msg, byteBuf);
						byteBuf.flip();

						transmitter.send(
								msg
						);

						byte[] buf = new byte[byteBuf.limit()];
						byteBuf.get(buf);

						// THis is probably supurfluous here as we are sening over network
						try {
							DatagramPacket packet = new DatagramPacket(buf, byteBuf.limit());
							broadcastSocket.send(packet);
						} catch (Exception ex) {
							System.out.println(ex.getMessage());
						}

						// Now we are going to broadcast on network interface specific
						if (!ni.isLoopback()) {
							// Now do broadcast for that NI
							for (InterfaceAddress interface_address : ni.getInterfaceAddresses()) {
								InetAddress broadcast = interface_address.getBroadcast();
								if (broadcast == null) {
									continue;
								}
								try {
									DatagramPacket packet = new DatagramPacket(buf, byteBuf.limit(), broadcast, broadcast_manager.getPort());
									broadcastSocket.send(packet);
								} catch (Exception ex) {
								}
							}
						}
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
