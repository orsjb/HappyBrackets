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

import de.sciss.net.OSCMessage;
import net.happybrackets.core.logging.Logging;
import net.happybrackets.device.DeviceMain;
import net.happybrackets.device.LogSender;
import net.happybrackets.device.network.UDPCachedMessage;

import java.io.IOException;

public class DeviceStatus {

	private enum MESSAGE_PARAMETERS{
		DEVICE_NAME,
		STATUS_TEXT,
		LOG_ENABLED,
		DEBUG_LEVEL
	}

	String statusText = "";
	boolean loggingEnabled = false;
	int debugLevel = 0;
	String deviceName = "";


	private static DeviceStatus gDeviceStatus = null;

	/**
	 * We use a private constructor with no parameters so we can generate on device
	 */
	private DeviceStatus(){
		deviceName = Device.getDeviceName();
	}


	private UDPCachedMessage cachedStatusMessage = null;


	/**
	 * Create a status Message
	 * @return the Cached Message Created
	 */
	public UDPCachedMessage getCachedStatusMessage()
	{
		try {
			if (cachedStatusMessage == null) {

				cachedStatusMessage = new UDPCachedMessage(getOSCMessage());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return  cachedStatusMessage;
	}

	public String getStatusText() {
		return statusText;
	}

	public void setStatusText(String status_text) {

		// If the message will be changed at all, we will invalidate it and force it to be rebuilt next time
		if (!status_text.equals(statusText))
		{
			cachedStatusMessage = null;
		}
		statusText = status_text;
	}

	public boolean isLoggingEnabled() {
		return loggingEnabled;
	}

	public void setLoggingEnabled(boolean logging_enabled) {
		if (loggingEnabled != logging_enabled)
		{
			cachedStatusMessage = null;
		}

		loggingEnabled = logging_enabled;
	}

	public int getDebugLevel() {
		return debugLevel;
	}

	public void setDebugLevel(int debug_level) {
		if (debugLevel != debug_level)
		{
			cachedStatusMessage = null;
		}
		debugLevel = debug_level;
	}


	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String device_name)
	{

		// If the message will be changed at all, we will invalidate it and force it to be rebuilt next time
		if (!device_name.equals(deviceName))
		{
			cachedStatusMessage = null;
		}
		deviceName = device_name;
	}


	/**
	 * Return the static DeviceStatus for device
	 * @return The Device status text
	 */
	static synchronized public DeviceStatus getInstance(){
		if (gDeviceStatus == null)
		{
			gDeviceStatus = new DeviceStatus();
		}
		return gDeviceStatus;
	}

	public OSCMessage getOSCMessage() {

		OSCMessage msg = new OSCMessage(OSCVocabulary.Device.STATUS,
				new Object[]{
						deviceName,
						statusText,
						loggingEnabled? 1:0,
						debugLevel
				});


		return msg;
	}

	/**
	 * Contructor based on OSC Message. Enabled encoding and decoding of Message within Same module
	 * @param msg OSC Message with parameters in arguments
	 */
	public  DeviceStatus(OSCMessage msg) {
		try {
			deviceName = (String) msg.getArg(MESSAGE_PARAMETERS.DEVICE_NAME.ordinal());
			statusText = (String) msg.getArg(MESSAGE_PARAMETERS.STATUS_TEXT.ordinal());
			loggingEnabled = ((int)msg.getArg(MESSAGE_PARAMETERS.LOG_ENABLED.ordinal())) == 0 ? false: true;
			debugLevel = (int)msg.getArg(MESSAGE_PARAMETERS.DEBUG_LEVEL.ordinal());

		}
		catch (Exception ex){}

	}
	
}
