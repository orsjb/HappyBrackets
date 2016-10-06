package net.happybrackets.device;

import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import net.happybrackets.device.config.DeviceConfig;
import net.happybrackets.core.AudioSetup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for PI code.
 *
 * //@param args
 */
public class DeviceMain {
    final static Logger logger = LoggerFactory.getLogger(DeviceMain.class);

	public static void main(String[] args) throws Exception {

        //manage configuration files;
		String configFile = "config/device-config.json";
        logger.debug("Loading config file: {}", configFile);
		DeviceConfig config = DeviceConfig.load(configFile);
		HB hb = new HB(AudioSetup.getAudioContext(args));
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
