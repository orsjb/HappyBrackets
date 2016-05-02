package net.happybrackets.core.misc_tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import core.LoadableConfig;

public class LoadableTest {
	
	class MockClass extends LoadableConfig {
		
	}
	
	MockClass cfg = new MockClass();

	@Before
	public void setUp() throws Exception {
		cfg = LoadableConfig.load("config/misc_tests-controller-config.json", cfg);
		if (cfg == null) fail("Unable to instantiate config class!");
	}

	@Test
	public void test() {
		assertTrue(cfg.getAliveInterval() == 1500); //this should be the value in the misc_tests-controller-config.json file
		assertTrue(cfg.getCodeToPIPort() == 2225); //this should be the value from the default interface
	}

}
