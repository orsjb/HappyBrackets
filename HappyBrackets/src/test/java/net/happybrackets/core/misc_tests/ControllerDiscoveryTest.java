package net.happybrackets.core.misc_tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import core.ControllerAdvertiser;
import core.ControllerConfig;
import core.DeviceConfig;

public class ControllerDiscoveryTest {
	protected DeviceConfig piEnv;
	protected ControllerConfig controllerEnv;
	protected ControllerAdvertiser advertiser;

	@Before
	public void setUp() throws Exception {
		piEnv = new DeviceConfig();
		controllerEnv = new ControllerConfig();
		advertiser = new ControllerAdvertiser(controllerEnv);
		advertiser.start();
	}

	@After
	public void tearDown() throws Exception {
		advertiser.interrupt();
	}

	@Test
	public void testGetControllerHostname() {
		assert( piEnv.getControllerHostname() != null );
		assert( piEnv.getControllerHostname().equals(controllerEnv.getMyHostName()) );
		System.out.println("Found host " + piEnv.getControllerHostname());
	}

}
