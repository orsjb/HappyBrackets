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

	final static int BANNER_WIDTH = 60;
	final static int BORDER_WIDTH = 3;
	final static String BORDER_CHAR = "*";
	final static String BLANK_CHAR = " ";

	static HB HBInstance;
	/**
	 * Get a line of text for a banner with the text in the centrem with a border and padded
	 * @param print_text The text we want displayed
	 * @param padding_char The char to pad the print_text with
	 * @return the print text centred between the border and padded so string is banner_width
	 */
	static String getBannerLine (String print_text,  String padding_char)
	{
		int str_len = print_text.length();
		// our text needs to fill this space
		int required_length = BANNER_WIDTH - BORDER_WIDTH * 2;
		int spaces_required = required_length - str_len;

		int leading_spaces = spaces_required / 2;
		// do this calulation to account for an odd amount of padding
		int trailing_spaces = spaces_required - leading_spaces;


		String ret = StringUtils.repeat(BORDER_CHAR, BORDER_WIDTH) +
				StringUtils.repeat(padding_char, leading_spaces) + print_text + StringUtils.repeat(padding_char, trailing_spaces)
				+ StringUtils.repeat(BORDER_CHAR, BORDER_WIDTH);

		/*
		String ret = repeatText(BORDER_CHAR, BORDER_WIDTH) +
				repeatText(padding_char, leading_spaces) + print_text + repeatText(padding_char, trailing_spaces)
				+ repeatText(BORDER_CHAR, BORDER_WIDTH);
*/

		return ret;


	}

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

	static void printBanner()
	{
		final int TOP_BORDER = 3;
		
		for (int i = 0; i < TOP_BORDER; i++)
		{
			System.out.println(getBannerLine(BORDER_CHAR,  BORDER_CHAR));
		}

		String version_text = "HappyBrackets Version " + BuildVersion.getMajor()+ "." + BuildVersion.getMinor() + "."
				+ BuildVersion.getBuild() + "." + BuildVersion.getDate();
		System.out.println(getBannerLine(version_text, BLANK_CHAR));


		for (int i = 0; i < TOP_BORDER; i++)
		{
			System.out.println(getBannerLine(BORDER_CHAR, BORDER_CHAR));
		}




	}
	public static void main(String[] args) throws Exception {

    	printBanner();
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
				break;
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
