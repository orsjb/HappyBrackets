package net.happybrackets.device;

import net.happybrackets.device.config.DeviceConfig;
import net.happybrackets.core.AudioSetup;

/**
 * Entry point for PI code.
 * 
 * //@param args
 */
public class DeviceMain {
	
	public static void main(String[] args) throws Exception {
        //manage configuration files;
		String configFile = "config/device-config.json";
        System.out.println("Reading config file: " + configFile);
		DeviceConfig config = DeviceConfig.load(configFile);
		HB hb = new HB(AudioSetup.getAudioContext(args));
		//deal with autostart and parse arguments
		boolean autostart = true;
		for(String s : args) {
			if(s.startsWith("start=")) {
				autostart = Boolean.parseBoolean(s.split("[=]")[1]);
			} else if(!s.contains("=")) {
				hb.attemptHBActionFromClassName(s);
			}
		}
		if(autostart) {
			System.out.println("Detected autostart. Starting audio right away.");
			hb.startAudio();
		}

	}
}
