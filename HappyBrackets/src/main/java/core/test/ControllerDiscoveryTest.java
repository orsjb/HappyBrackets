package core.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import core.ControllerAdvertiser;
import core.ControllerConfig;
import core.PIConfig;

public class ControllerDiscoveryTest {
	protected PIConfig piEnv;
	protected ControllerConfig controllerEnv;
	protected ControllerAdvertiser advertiser;

	@Before
	public void setUp() throws Exception {
		piEnv = new PIConfig();
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
