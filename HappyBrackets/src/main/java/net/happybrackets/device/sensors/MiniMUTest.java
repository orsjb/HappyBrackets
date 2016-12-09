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

package net.happybrackets.device.sensors;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.AudioSetup;

public class MiniMUTest {

	public static void main(String[] args) {
		//audio
		AudioContext ac = AudioSetup.getAudioContext(args);
		//ac.start();
		//controllers
		final Glide freqCtrl = new Glide(ac, 500);
		final Glide gainCtrl = new Glide(ac, 0.1f);
		//set up signal chain
		WavePlayer wp = new WavePlayer(ac, freqCtrl, Buffer.SINE);
		Gain g = new Gain(ac, 1, gainCtrl);
		g.addInput(wp);
		ac.out.addInput(g);
		//getInstance listening to data

		MiniMU mm = new MiniMU();

		SensorUpdateListener myListener = new SensorUpdateListener() {

			@Override
			public void sensorUpdated() {
				double[] accel = mm.getAccelerometerData();
				String AccString = String.format("MiniMu Acc X/Y/Z = %05.2f %05.2f %05.2f", accel[0], accel[1], accel[2]);
				System.out.println(AccString);
				freqCtrl.setValue(((float)Math.abs(accel[0]) * 10f) % 10000f + 600f);
				gainCtrl.setValue(((float)Math.abs(accel[1]) * 10f) % 400f / 1600f + 0.1f);
				double[] gyro = mm.getGyroscopeData();
				String GyrString = String.format("MiniMu Gyr X/Y/Z = %05.2f %05.2f %05.2f", gyro[0], gyro[1], gyro[2]);
				System.out.println(GyrString);
				double[] mag = mm.getMagnetometerData();
				String MagString = String.format("MiniMu Mag X/Y/Z = %05.2f %05.2f %05.2f", mag[0], mag[1], mag[2]);
				System.out.println(MagString);
			}

		};
		mm.addListener(myListener);
	}

}
