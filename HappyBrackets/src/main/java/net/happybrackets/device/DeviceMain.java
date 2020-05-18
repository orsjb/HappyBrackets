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


import net.happybrackets.core.AudioSetup;
import net.happybrackets.core.Device;
import net.happybrackets.device.config.DeviceConfig;
import net.happybrackets.device.sensors.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * Entry point for PI code.
 *
 * //@param args
 */
public class DeviceMain {
    final static Logger logger = LoggerFactory.getLogger(DeviceMain.class);

	static public final String EXAMPLES_FOLDER = "examples";


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

		final String CONFIG_PATH = "config/";
		final String DEFAULT_CONFIG_FILE = "device-config.json";

		String configFile = CONFIG_PATH  + DEFAULT_CONFIG_FILE;

		MuteControl muteControl = null;

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
					HB.setEnableSimulators(true);
					Sensor.setSimulatedOnly(true);
					HB.deviceType =  HB.DeviceType.SIMULATOR;
				}
			}
			else if (s.startsWith("config=")) {
				String config_value = s.split("[=]")[1];
				configFile = CONFIG_PATH  + config_value;
			}
			else if (s.startsWith("mute=")){

				try{
					String config_value = s.split("[=]")[1];
					int mute_gpio = Integer.parseInt(config_value);
					System.out.println("Using GPIO " + mute_gpio + " for Mute control");
					muteControl = new MuteControl(mute_gpio);

				}
				catch (Exception ex){}
			}

		}

		logger.debug("Access mode is " + mode);

		HB.DeviceType deviceType =  HB.detectDeviceType();

		System.out.println("Detected " + deviceType.name());

		eraseExampleClasses();

		//manage configuration files;
		System.out.println("Setting config file to " +  configFile);
        logger.debug("Loading config file: {}", configFile);
		DeviceConfig config = DeviceConfig.load(configFile);
		HB hb = new HB(AudioSetup.getAudioContext(args), mode);
		hb.setMuteControl(muteControl);
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

		String startup_filename = StartupClasses.getStartupFilename(Device.getDeviceName());

		System.out.println("Look for " + startup_filename);
		// see if we find startup file based on hostname
		File f = new File(startup_filename);
		if(!f.exists()) {
			// try the default name then
			System.out.println("Device specific startup file not found");
			startup_filename = StartupClasses.getDefaultStartupFilename();
			System.out.println("Look for " + startup_filename);
		}


		f = new File(startup_filename);
		if(f.exists()) {
			System.out.println("Found " + startup_filename);
			// Lets try some startup names
			List<String> auto_classes =  StartupClasses.getStartupClassnames(startup_filename);
			for (String classname: auto_classes) {
				hb.attemptHBActionFromClassName(classname);
			}

		}

		if(autostart) {
			logger.debug("Detected autostart. Starting audio right away.");
			hb.startAudio();
		}

	}

	/**
	 * erase the example classes from the startup directory
	 */
	private static void eraseExampleClasses() {
		try{
			String path = StartupClasses.STARTUP_FOLDER + EXAMPLES_FOLDER;
			File file = new File(path);
			if (file.exists()){
				if (file.isDirectory()){

					System.out.println("Delete " + path);
					Files.walk(file.toPath())
							.sorted(Comparator.reverseOrder())
							.map(Path::toFile)
							.forEach(File::delete);
				}
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
	}
}
