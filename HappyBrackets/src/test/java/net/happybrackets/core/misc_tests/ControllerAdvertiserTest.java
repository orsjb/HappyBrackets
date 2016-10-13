package net.happybrackets.core.misc_tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.happybrackets.core.BroadcastManager;

import net.happybrackets.controller.network.ControllerAdvertiser;
import net.happybrackets.controller.config.ControllerConfig;

public class ControllerAdvertiserTest {
	protected ControllerConfig 			env;
	protected ControllerAdvertiser 	advertiser;
	protected BroadcastManager			broadcastManager;

	@Before
	public void setUp() throws Exception {
		env 				= new ControllerConfig();
		env 				= env.load("src/test/config/test-controller-config.json", env);
		advertiser 			= new ControllerAdvertiser(broadcastManager);
		advertiser.start();
	}

	@After
	public void tearDown() throws Exception {
		advertiser.interrupt();
	}

	@Test
	public void test() {
		assert( advertiser.isAlive() );
	}

}
