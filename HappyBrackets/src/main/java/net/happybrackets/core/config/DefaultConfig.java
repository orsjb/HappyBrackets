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

package net.happybrackets.core.config;

/**
 * Define the default configuration in case a config has not been loaded
 */
public class DefaultConfig {

	//hosts and ports for network messages
	public static final String MULTICAST_ADDRESS = "::FFFF:225.2.2.5"; //multicast address used for both synch and broadcast messages
	public static final int    BROADCAST_PORT =  2222; 				//broadcast port (not currently OSC)
	public static final int    STATUS_FROM_DEVICE_PORT = 2223; 		//OSC status messages from device to controller
	public static final int    CODE_TO_DEVICE_PORT = 2225; 			//Java bytecode from controller to device
	public static final int    CLOCK_SYNC_PORT = 2224; 			//Java bytecode from controller to device
	public static final int    CONTROL_TO_DEVICE_PORT = 2226; 			//OSC messages from controller to device
	public static final int    CONTROLLER_DISCOVERY_PORT = 2227;
	public static final int    CONTROLLER_HTTP_PORT = 2228; 			//http requests from device to controller
	//how often the PI sends an alive message to the server
	public static final int    ALIVE_INTERVAL = 1000;
	//places
	public static final String WORKING_DIRECTORY =  ".";
	public static final String AUDIO_DIRECTORY = "/audio";
	public static final String CONFIG_DIRECTORY = "/config";
	public static final String KNOWN_DEVICES_FILE = "/known_devices";
	public static final String ENCRYPTION_KEY = "CHANGE ME!";
}
