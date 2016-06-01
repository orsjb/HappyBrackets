package net.happybrackets.core.misc_tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import net.happybrackets.core.LoadableConfig;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LoadableTest {
	
	class MockClass extends LoadableConfig {
		
	}
	
	MockClass cfg = new MockClass();

	@Before
	public void setUp() throws Exception {
		//add some diagnostics for current path
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current relative path is: " + s);

		cfg = LoadableConfig.load("src/test/config/test-controller-config.json", cfg);
		if (cfg == null) fail("Unable to instantiate config class!");
	}

	@Test
	public void test() {
		assertTrue(cfg.getAliveInterval() == 1500); //this should be the value in the misc_tests-controller-config.json file
		assertTrue(cfg.getCodeToDevicePort() == 2225); //this should be the value from the default interface
	}

}
