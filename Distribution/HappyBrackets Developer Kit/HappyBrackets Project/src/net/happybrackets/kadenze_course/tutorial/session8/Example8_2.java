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

package net.happybrackets.kadenze_course.tutorial.session8;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 * In this example, we use the z-axis of the accelerometer data to manipulate the frequency of a WavePlayer sine tone.
 */
public class Example8_2 implements HBAction {

    @Override
    public void action(HB hb) {

        // reset HB
        hb.reset();

        // Set up a sensor value as a Glide
        Glide sensorVal = new Glide(hb.ac);

        // Setup the Accelerometer sensor connection
        LSM9DS1 lsm = (LSM9DS1) hb.getSensor(LSM9DS1.class);


        // Set up a Function to translate accelerometer data into a frequency range that makes sense.
        Function freq = new Function(sensorVal){
            @Override
            public float calculate(){
                return ((x[0] +1f) * 0.5f) * 660f + 220f;
            }
        };

        // Setup the Sine Generator, with the 'freq' Function determining the frequency
        WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SINE);

        // Gain control
        Gain g = new Gain(hb.ac, 1, 0.1f);

        // Hook up the UGens
        g.addInput(wp);
        hb.ac.out.addInput(g);

        // set up the SensorUpdateListener to update the ZMapper
        lsm.addListener(new SensorUpdateListener() {
            @Override
            public void sensorUpdated() {

                // get the accelerometer data
                // (x axis is [0], y axis is [1] so z axis is [2])
                float zAxis = (float) lsm.getAccelerometerData()[2];

                // change the frequency
                sensorVal.setValue(zAxis);
                System.out.println(zAxis + " " + freq.getValue());

            }
        });
    }
}
