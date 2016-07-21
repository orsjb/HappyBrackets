package net.happybrackets.core.misc_tests;

import net.happybrackets.controller.network.ControllerAdvertiser;
import net.happybrackets.controller.config.ControllerConfig;
import net.happybrackets.device.config.DeviceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ControllerDiscoveryTest {
	protected DeviceConfig deviceEnv;
	protected ControllerConfig controllerEnv;
	protected ControllerAdvertiser advertiser;

	@Before
	public void setUp() throws Exception {
		deviceEnv = new DeviceConfig();
		deviceEnv = deviceEnv.load("src/test/config/test-device-config.json", deviceEnv);

		controllerEnv = new ControllerConfig();
		controllerEnv = controllerEnv.load("src/test/config/test-controller-config.json", controllerEnv);

		advertiser = new ControllerAdvertiser(controllerEnv);
		advertiser.start();
	}

	@After
	public void tearDown() throws Exception {
		advertiser.interrupt();
	}

	@Test
	public void testGetControllerHostname() {
		assertTrue( advertiser.isAlive() );
		assertNotNull( deviceEnv.getControllerHostname() );
		assertEquals( controllerEnv.getMyHostName(), deviceEnv.getControllerHostname() );
		System.out.println("Found controller: " + deviceEnv.getControllerHostname());
	}

}
