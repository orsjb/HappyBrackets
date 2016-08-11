package net.happybrackets.core.misc_tests;

import net.happybrackets.controller.network.ControllerAdvertiser;
import net.happybrackets.controller.config.ControllerConfig;
import net.happybrackets.device.config.DeviceConfig;
import net.happybrackets.core.BroadcastManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
		broadcastManager = new BroadcastManager(controllerEnv);

		advertiser = new ControllerAdvertiser(broadcastManager, controllerEnv.getMyHostName());
		advertiser.start();

		deviceEnv.listenForController(broadcastManager);
	}

	@After
	public void tearDown() throws Exception {
		advertiser.stop();
	}

	@Test
	public void testGetControllerHostname() {
		//give some time to catch the name of the controller
		try {
			Thread.sleep(1500);
		}
		catch (InterruptedException e) {
			System.err.println("Sleep was interupted during ControllerDiscoveryTest.");
			e.printStackTrace();
		}

		assertTrue( advertiser.isAlive() );
		assertNotNull( deviceEnv.getControllerHostname() );
		assertEquals( controllerEnv.getMyHostName(), deviceEnv.getControllerHostname() );
		System.out.println("Found controller: " + deviceEnv.getControllerHostname());
	}

}
