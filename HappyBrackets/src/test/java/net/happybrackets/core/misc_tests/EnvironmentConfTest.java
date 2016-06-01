package net.happybrackets.core.misc_tests;

import net.happybrackets.core.EnvironmentConfig;
import junit.framework.TestCase;

public class EnvironmentConfTest extends TestCase {
	protected EnvironmentConfig env;
	
	protected void setUp() {
		env = new EnvironmentConfig(){
			
		};
	}
	
	public void testMyHostname() {
		String myHostname = env.getMyHostName();
		assertTrue(myHostname != null);
		assertFalse( myHostname.isEmpty() );
	}
}
