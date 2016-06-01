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
		if(args.length > 5) {
			boolean autostart = Boolean.parseBoolean(args[5]);
			if(autostart) {
				System.out.println("Detected autostart. Starting audio right away.");
				hb.startAudio();
			}
		}
	}
}
