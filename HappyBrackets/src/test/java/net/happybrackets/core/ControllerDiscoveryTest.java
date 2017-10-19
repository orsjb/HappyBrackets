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

package net.happybrackets.core;

import net.happybrackets.controller.network.ControllerAdvertiser;
import net.happybrackets.controller.config.ControllerConfig;
import net.happybrackets.core.Device;
import net.happybrackets.device.config.DeviceConfig;
import net.happybrackets.core.BroadcastManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.NetworkInterface;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class ControllerDiscoveryTest {
	protected DeviceConfig 					deviceEnv;
	protected ControllerConfig 			controllerEnv;
	protected ControllerAdvertiser 	advertiser;
	protected BroadcastManager			broadcastManager;

	@Before
	public void setUp() throws Exception {
		deviceEnv = new DeviceConfig();
		deviceEnv = deviceEnv.load("src/test/config/test-device-config.json", deviceEnv);

		controllerEnv = new ControllerConfig();
		controllerEnv = controllerEnv.load("src/test/config/test-controller-config.json", controllerEnv);

		//we need a broadcast manager for testing but we can only run one at a time
		broadcastManager = new BroadcastManager(controllerEnv.getMulticastAddr(), controllerEnv.getBroadcastPort());
		broadcastManager.startRefreshThread();

		advertiser = new ControllerAdvertiser(deviceEnv.getMulticastAddr(), deviceEnv.getBroadcastPort(), deviceEnv.getBroadcastPort());
		advertiser.start();

		deviceEnv.listenForController(broadcastManager);
	}

	@After
	public void tearDown() throws Exception {
		advertiser.stop();
	}

	@Test
	public void testGetControllerHostname() {
        ArrayList<String> controllerHostName = new ArrayList<>();

        String our_hostname = Device.getDeviceName();

        try {
            Device.viableInterfaces().forEach( ni -> controllerHostName.add(Device.selectHostname(ni)) );
        } catch (Exception e) {
            System.err.println("Error retrieving host name!");
            e.printStackTrace();
        }

        int max_retries = 5;
		//give some time to catch the name of the controller
		try {
        	for (int i = 0; i < max_retries; i++) {
				Thread.sleep(500);
				String controller_hostname = deviceEnv.getControllerHostname();
				if (!controller_hostname.isEmpty())
				{
					break;
				}
			}
		}
		catch (InterruptedException e) {
			System.err.println("Sleep was interupted during ControllerDiscoveryTest.");
			e.printStackTrace();
		}

		assertTrue( advertiser.isAlive() );
		assertNotNull( deviceEnv.getControllerHostname() );
        //stop advertising so we do not change mid test
        advertiser.stop();
        //check that one of our host names matches

		String controller_hostname = deviceEnv.getControllerHostname();


        assertTrue(
                controllerHostName.stream().anyMatch(name -> controller_hostname.equals(name)) || our_hostname.equals(controller_hostname)
        );

		System.out.println("Found controller: " + deviceEnv.getControllerHostname());

	}

}
