/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.core;

import net.happybrackets.core.config.LoadableConfig;
import net.happybrackets.device.config.DeviceConfig;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.fail;

public class LoadableDeviceConfigTest {

	
	DeviceConfig cfg = new DeviceConfig();

	//@Before
	public void setUp() throws Exception {
		//add some diagnostics for current path
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current relative path is: " + s);

		cfg = LoadableConfig.load("src/test/config/test-device-config.json", cfg);
		if (cfg == null) fail("Unable to instantiate config class!");
	}

	@Test
	public void load() {
		//assertTrue(cfg.getAliveInterval() == 15000); //this should be the value in the misc_tests-controller-config.json file
		//assertTrue(cfg.getCodeToDevicePort() == 23250); //this should be the value from the default interface
		System.out.println("Test Needs to be implemented");
	}

}
