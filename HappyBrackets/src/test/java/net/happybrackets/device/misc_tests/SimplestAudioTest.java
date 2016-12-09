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

package net.happybrackets.device.misc_tests;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;
import net.beadsproject.beads.ugens.Noise;
import net.happybrackets.core.AudioSetup;

public class SimplestAudioTest {
	public static void main(String[] args) {
		
		System.out.println("Mixer info: ");
		JavaSoundAudioIO.printMixerInfo();
		System.out.println("------------");

		AudioContext ac = AudioSetup.getAudioContext(args);
		Noise n = new Noise(ac);
		ac.out.setGain(0.5f);
		ac.out.addInput(n);
		ac.start();
		
	}
}
