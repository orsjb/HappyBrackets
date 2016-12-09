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

package net.happybrackets.tutorial.session8;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.beadsproject.beads.ugens.ZMap;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 * In this examlpe, we use the z-axis of the accelerometer data to manipulate the frequency of a WavePlayer.
 */
public class Example8_2 implements HBAction {

    @Override
    public void action(HB hb) {
        // reset HB
        hb.reset();
        //load a set of sounds
        Envelope freq = new Envelope(hb.ac, 0.1f);
        // Set up the ZMap
        ZMap zm = new ZMap(hb.ac);
        zm.setRanges(-2.0f, 2.0f, 220f, 880f);
        zm.setValue(0.0f);
        freq.addInput(zm);
        // New Accelerometer
        LSM9DS1 lsm = (LSM9DS1) hb.getSensor(LSM9DS1.class);
        // Sine Generator
        WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SINE);
        // Gain control
        Gain g = new Gain(hb.ac, 1, 0.1f);
        // Hook up the UGens
        g.addInput(wp);
        hb.ac.out.addInput(g);
        // set up the SensorUpdateListener
        lsm.addListener(new SensorUpdateListener() {
            @Override
            public void sensorUpdated() {
                // get the accelerometer data
                float zAxis = (float) lsm.getAccelerometerData()[2];
                // change the frequency
                zm.setValue(zAxis);
            }
        });
    }
}
