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
import net.happybrackets.device.config.DeviceController;
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

	final int MAX_TEST_TIME = 5000;
	Object testWaitControl = new Object();

	boolean controllerFound = false;

	@Before
	public void setUp() throws Exception {
		deviceEnv = new DeviceConfig();
		deviceEnv = deviceEnv.load("src/test/config/test-device-config.json", deviceEnv);


		controllerEnv = new ControllerConfig();
		controllerEnv = controllerEnv.load("src/test/config/test-controller-config.json", controllerEnv);

		//we need a broadcast manager for testing but we can only run one at a time
		broadcastManager = new BroadcastManager(controllerEnv.getMulticastAddr(), controllerEnv.getBroadcastPort());
		broadcastManager.setThreadSleepTime(2000);

		deviceEnv.listenForController(broadcastManager);

		broadcastManager.startRefreshThread();

		advertiser = new ControllerAdvertiser(deviceEnv.getMulticastAddr(), deviceEnv.getBroadcastPort(), deviceEnv.getBroadcastPort());
		advertiser.setSendLocalHost(true);
		advertiser.start();


	}

	@After
	public void tearDown() throws Exception {
		advertiser.stop();
	}

	@Test
	public void testGetControllerHostname() {
        ArrayList<String> controllerHostName = new ArrayList<>();

        String our_hostname = Device.getDeviceName();

        deviceEnv.addDeviceDiscoveredListener(new DeviceConfig.DeviceControllerDiscoveredListener() {
			@Override
			public void controllerDiscovered(DeviceController deviceController) {
				if (deviceController.getHostname().equalsIgnoreCase(our_hostname)) {
					System.out.println("Device found");
					controllerFound = true;
					synchronized (testWaitControl){
						testWaitControl.notify();
					}
				}
			}
		});

		assertTrue( advertiser.isAlive() );

		synchronized (testWaitControl){
			try {
				testWaitControl.wait(MAX_TEST_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// if we received a controller found event, this will be true
		assertNotNull(controllerFound );
        //stop advertising so we do not change mid test
        advertiser.stop();


	}

}
