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

import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import net.happybrackets.core.BuildVersion;
import net.happybrackets.device.config.DeviceConfig;
import net.happybrackets.core.AudioSetup;

import net.happybrackets.device.sensors.Sensor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * Entry point for PI code.
 *
 * //@param args
 */
public class DeviceMain {
    final static Logger logger = LoggerFactory.getLogger(DeviceMain.class);



	static HB HBInstance;

	/**
	 * Return the global Intstance of HB
	 * @return Static HB
	 */
	public static final HB getHB()
	{
		return HBInstance;
	}

	static String repeatText(String s, int count)
	{
		String ret = "";
		for (int i = 0; i < count; i++)
		{
			ret += s;
		}

		return ret;
	}


	public static void main(String[] args) throws Exception {

		TextOutput.printBanner();
		// Determine access mode.
		HB.AccessMode mode = HB.AccessMode.OPEN;
		for (String s : args) {
			if (s.startsWith("access=")) {
				try {
					mode = HB.AccessMode.valueOf(s.split("[=]")[1].toUpperCase());
				}
				catch (Exception e) {
					logger.error("Error setting access mode from command line, check spelling. Defaulting to OPEN.");
				}
				//break;
			}
			else if (s.startsWith("simulate=")){
				String simulate_value = s.split("[=]")[1];
				if(simulate_value.equalsIgnoreCase("true")){
					Sensor.setSimulatedOnly(true);
				}
			}
		}

		logger.debug("Access mode is " + mode);

		//manage configuration files;
		String configFile = "config/device-config.json";
        logger.debug("Loading config file: {}", configFile);
		DeviceConfig config = DeviceConfig.load(configFile);
		HB hb = new HB(AudioSetup.getAudioContext(args), mode);
		HBInstance = hb;
		//deal with autostart and parse arguments
		boolean autostart = true;
		for(String s : args) {
			if(s.startsWith("start=")) {
				autostart = !s.split("[=]")[1].toLowerCase().startsWith("f");
			} else if(!s.contains("=")) {
				hb.attemptHBActionFromClassName(s);
			}
		}
		if(autostart) {
			logger.debug("Detected autostart. Starting audio right away.");
			hb.startAudio();
		}

	}
}
