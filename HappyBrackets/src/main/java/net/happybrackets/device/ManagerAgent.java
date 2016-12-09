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

package net.happybrackets.device;

import net.happybrackets.device.config.DeviceConfig;
import net.happybrackets.core.config.LoadableConfig;

public class ManagerAgent {

	public static void main(String[] args) {
		DeviceConfig env = new DeviceConfig();
		env = LoadableConfig.load("config/device-config.json", env);
//		System.out.println("Device Manager Agent Started as: " + env.getMyHostName());
		System.out.println("Listening for Controller...");
		System.out.println("Found controller on host: " + env.getControllerHostname());
		System.out.println("Requesting configuration settings from controller...");
		System.out.println("Device Manager Agent Exiting");
	}

}
