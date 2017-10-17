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

import net.happybrackets.core.BroadcastManager;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.device.DeviceMain;
import net.happybrackets.device.HB;
import net.happybrackets.device.config.DeviceConfig;
import net.happybrackets.device.config.DeviceController;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import org.slf4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public interface ControllerDiscoverer {

	default void listenForController(DeviceConfig device_config, BroadcastManager broadcast_manager, Logger logger) {

		broadcast_manager.addBroadcastListener(new OSCListener(){
			public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
				final int CONTROLLER_HOSTNAME = 0;
				final int CONTROLLER_PORT = 1;

				if (OSCVocabulary.match(msg, OSCVocabulary.CONTROLLER.CONTROLLER) && msg.getArgCount() >= CONTROLLER_PORT) {

					InetAddress sending_address = ((InetSocketAddress) sender).getAddress();

                    String advertised_hostname = (String) msg.getArg(CONTROLLER_HOSTNAME);
					String address =  sending_address.getHostAddress();//  (String) msg.getArg(1);
					int port = (int) msg.getArg(CONTROLLER_PORT);

					int device_id = 0;


					HB device_instance = DeviceMain.getHB();

					if (device_instance != null)
					{
						device_id = device_instance.myIndex();
					}

					device_config.deviceControllerFound(advertised_hostname, address, port, device_id);

					/*
                    //System.out.println("Received controller message from " + advertisedAddress);
					if ( !controller.getAddress().equals(advertisedAddress) || !controller.getHostname().equals(advertisedHostname) ) {
                        controller.setAddress(advertisedAddress);
                        controller.setHostname(advertisedHostname);
                        logger.debug("Updated controller to {} at {}", controller.getHostname(), controller.getAddress());
                    }
                    */
				}
			}
		});

	}

}
