package net.happybrackets.core.misc_tests;

import net.happybrackets.controller.config.ControllerConfig;
import junit.framework.TestCase;

public class ControllerConfigTest extends TestCase {
	protected ControllerConfig env;

	protected void setUp() throws Exception {
		super.setUp();
		env = new ControllerConfig();
	}
	
	public void testMyHostname() {
//		Retired:
//		String myHostname = env.getMyHostName();
//		assertTrue(myHostname != null);
//		assertFalse( myHostname.isEmpty() );
	}

}
