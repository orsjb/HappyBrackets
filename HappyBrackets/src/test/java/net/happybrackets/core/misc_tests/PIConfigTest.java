package net.happybrackets.core.misc_tests;

import static org.junit.Assert.*;

import net.happybrackets.device.config.DeviceConfig;
import org.junit.Before;
import org.junit.Test;

public class PIConfigTest {
	protected DeviceConfig env;

	@Before
	public void setUp() throws Exception {
		env = new DeviceConfig();
	}

	@Test
	public void myHostNameTest() {
//      Retired:
//		String myHostname = env.getMyHostName();
//		assertTrue(myHostname != null);
//		assertFalse( myHostname.isEmpty() );
	}

}
