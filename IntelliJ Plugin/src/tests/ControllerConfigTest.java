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

package tests;

//import net.happybrackets.controller.config.ControllerConfig;

import junit.framework.TestCase;
import net.happybrackets.intellij_plugin.controller.config.ControllerConfig;

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
