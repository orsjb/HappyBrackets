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

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.IOAudioFormat;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AudioSetup {

	final static Logger logger = LoggerFactory.getLogger(AudioSetup.class);

	public static AudioContext getAudioContext(String[] args) {			//args are bufSize (512), sample rate (11000), input channels (0), output channels (1)

		int bufSize = 2048;
		int sampleRate = 44100;
		int bits = 16;
		int inchans = 0;
		int outchans = 1;
		int mixer = -1;
		try {
			//parse args
			for(String arg : args) {
				String[] elements = arg.split("[=]");
				if(elements.length > 1) {
					if(elements[0].equals("buf")) bufSize = Integer.parseInt(elements[1]);
					else if(elements[0].equals("sr")) sampleRate = Integer.parseInt(elements[1]);
					else if(elements[0].equals("bits")) bits = Integer.parseInt(elements[1]);
					else if(elements[0].equals("ins")) inchans = Integer.parseInt(elements[1]);
					else if(elements[0].equals("outs")) outchans = Integer.parseInt(elements[1]);
					else if(elements[0].equals("device")) mixer = Integer.parseInt(elements[1]);
				}
			}
		} catch(Exception e) {
			logger.warn("Correct args have not been supplied to AudioSetup.getAudioContext()");
		}
		logger.info("Creating AudioContext with args: bufSize=" + bufSize + ", sampleRate=" + sampleRate + ", bits=" + bits + ", ins=" + inchans + ", outs=" + outchans + ", mixer=" + mixer);
		JavaSoundAudioIO jsaio = new JavaSoundAudioIO(bufSize);
		if(mixer != -1) {
			jsaio.selectMixer(mixer);
		}
		AudioContext ac = new AudioContext(jsaio, bufSize, new IOAudioFormat(sampleRate, bits, inchans, outchans));
		return ac;

	}
}
