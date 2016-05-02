package core.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import core.PIConfig;

public class PIConfigTest {
	protected PIConfig env;
	
	@Before
	public void setUp() throws Exception {
		env = new PIConfig();
	}

	@Test
	public void myHostNameTest() {
		String myHostname = env.getMyHostName();
		assertTrue(myHostname != null);
		assertFalse( myHostname.isEmpty() );
	}

}
