package net.happybrackets.core.misc_tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.happybrackets.controller.network.ControllerAdvertiser;
import net.happybrackets.controller.config.ControllerConfig;

public class ControllerAdvertiserTest {
	protected ControllerConfig env;
	protected ControllerAdvertiser advertiser;
	
	@Before
	public void setUp() throws Exception {
		env = new ControllerConfig();
		advertiser = new ControllerAdvertiser(env);
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
