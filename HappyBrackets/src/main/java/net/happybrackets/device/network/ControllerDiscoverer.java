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
import net.happybrackets.device.config.DeviceController;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import org.slf4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public interface ControllerDiscoverer {

	default void listenForController(DeviceController controller, BroadcastManager broadcastManager, Logger logger) {
		broadcastManager.addBroadcastListener(new OSCListener(){
			public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
				if (OSCVocabulary.match(msg, "/hb/controller") && msg.getArgCount() > 0) {

					InetAddress sending_address = ((InetSocketAddress) sender).getAddress();

                    String advertisedAddress =  sending_address.getHostAddress();//  (String) msg.getArg(1);
                    String advertisedHostname = (String) msg.getArg(0);

                    //System.out.println("Received controller message from " + advertisedAddress);
					if ( !controller.getAddress().equals(advertisedAddress) || !controller.getHostname().equals(advertisedHostname) ) {
                        controller.setAddress(advertisedAddress);
                        controller.setHostname(advertisedHostname);
                        logger.debug("Updated controller to {} at {}", controller.getHostname(), controller.getAddress());
                    }
				}
			}
		});

	}

}
