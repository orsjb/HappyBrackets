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

import de.sciss.net.OSCMessage;
import de.sciss.net.OSCTransmitter;
import net.happybrackets.core.BroadcastManager;

import net.happybrackets.core.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.NetworkInterface;

public class ControllerAdvertiser {

	final static Logger logger = LoggerFactory.getLogger(ControllerAdvertiser.class);

	private Thread advertisementService;
	private boolean keepAlive = true;

	public ControllerAdvertiser(BroadcastManager broadcastManager) {
		//set up an indefinite thread to advertise the controller
		advertisementService = new Thread() {
			public void run() {
            BroadcastManager.OnTransmitter advertisement = new BroadcastManager.OnTransmitter() {
                @Override
                public void cb(NetworkInterface ni, OSCTransmitter transmitter) throws IOException {
                    transmitter.send(
                            new OSCMessage(
                                    "/hb/controller",
                                    new Object[] {
                                            Device.selectHostname(ni),
                                            Device.selectIP(ni)
                                    }
                            )
                    );
                }
            };

            while (keepAlive) {
                broadcastManager.forAllTransmitters(advertisement);

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
