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

//SG 2016-04-04
//We should move from a static configuration definition to a dynamic definition.
//By moving to a dynamic method we can load settings on launch from external files
// and we can run init level operations as part of the object instantiation.
//Key bonuses are the PIs being able to look for the host controller instead of the current hard coded approach.


import net.happybrackets.core.Device;

public interface EnvironmentConfig {

	//hosts and ports for network messages
//	default public String getMyHostName() 					{ return Device.getInstance().myHostname;}
//  default public String getMyAddress()					{ return Device.getInstance().myIP; }
	default public String getMulticastAddr()				{ return "::FFFF:225.2.2.5"; }		//multicast address used for both synch and broadcast messages
	default public int getBroadcastPort() 					{ return 2222; }					//broadcast port (not currently OSC)
	default public int getStatusFromDevicePort()			{ return 2223; }					//OSC status messages from device to controller
	default public int getClockSynchPort()					{ return 2224; }					//synch messages over multicast
	default public int getCodeToDevicePort()				{ return 2225; }					//Java bytecode from controller to device
	default public int getControlToDevicePort()				{ return 2226; }					//OSC messages from controller to device
	default public int getControllerDiscoveryPort()			{ return 2227; }
	default public int getControllerHTTPPort()				{ return 2228; }					//http requests from device to controller
	//how often the PI sends an alive message to the server
	default public int getAliveInterval() 					{ return 1000; }
	//places
	default public String getWorkingDir()					{ return "."; }
	default public String getAudioDir()						{ return getWorkingDir() + "/audio"; }
	default public String getConfigDir() 			    	{ return getWorkingDir() + "/config"; }
	default public String getKnownDevicesFile()				{ return getConfigDir() + "/known_devices"; }
}
