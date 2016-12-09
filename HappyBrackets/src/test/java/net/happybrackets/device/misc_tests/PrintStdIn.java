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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.happybrackets.core.AudioSetup;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;


public class PrintStdIn{
	public static void main (String args[]){

		AudioContext ac = AudioSetup.getAudioContext(args);
		ac.start();

		Glide g = new Glide(ac, 500);
		WavePlayer wp = new WavePlayer(ac, g, Buffer.SINE);

		ac.out.addInput(wp);
		ac.out.setGain(0.1f);

		try{
			BufferedReader br = new BufferedReader(new InputStreamReader (System.in));
			String input;
			while ((input=br.readLine())!=null){
				System.out.println("Java: " + input);
				String inVal1 = input.split("\\s+")[1];
				System.out.println(inVal1);
				float val = Float.parseFloat(inVal1);
				g.setValue(val *  2000f);
			}
		}catch(IOException io){
		io.printStackTrace();
		}
	}
}
