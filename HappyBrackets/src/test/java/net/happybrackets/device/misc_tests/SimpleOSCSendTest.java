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

package net.happybrackets.device.misc_tests;

import java.io.IOException;
import java.net.InetSocketAddress;

import net.happybrackets.device.config.DeviceConfig;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

public class SimpleOSCSendTest {
	protected static final DeviceConfig config = new DeviceConfig();

	public static void main(String[] args) throws IOException {
		OSCServer s = OSCServer.newUsing(OSCServer.UDP);
		InetSocketAddress addr = new InetSocketAddress("pisound-009e959c510a.local", config.getControlToDevicePort());
		
		s.send(new OSCMessage("/PI/"), null);
	}

}
