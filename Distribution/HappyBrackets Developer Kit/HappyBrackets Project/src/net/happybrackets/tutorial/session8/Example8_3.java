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

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 * For this example we want to look at the accelerometer and use it to trigger a sound when you turn over the accelerometer.
 */
public class Example8_3 implements HBAction {

    public enum Orientation {UP, DOWN}

    Orientation currentOri = Orientation.UP;
    Orientation previousOri = Orientation.DOWN;

    @Override
    public void action(HB hb) {
        hb.reset();
        //load a set of sounds
        SampleManager.group("Guitar", "data/audio/Nylon_Guitar");
        //play a new random sound
        Sample sUp = SampleManager.fromGroup("Guitar",1);
        Sample sDown = SampleManager.fromGroup("Guitar",2);
        LSM9DS1 mySensor = (LSM9DS1) hb.getSensor(LSM9DS1.class);
        mySensor.addListener(new SensorUpdateListener() {
            @Override
            public void sensorUpdated() {
                // Get the data from Z.
                double zAxis = mySensor.getAccelerometerData()[2];
                System.out.println("zAxis: " + zAxis);
                // set previous orientation to the current orientation
                previousOri = currentOri;
                // Is it positive or negative.
                if (zAxis >= 0) {
                    currentOri = Orientation.UP;
                } else {
                    currentOri = Orientation.DOWN;
                }
                // Is it different to the current value (has it changed)
                if (currentOri != previousOri) {
                    // if so play the sound
                    if (currentOri == Orientation.UP) {
                        // this is the up sound
                        SamplePlayer sp = new SamplePlayer(hb.ac, sUp);
                        hb.sound(sp);
                    } else if (currentOri == Orientation.DOWN) {
                        // this is the down sound
                        SamplePlayer sp = new SamplePlayer(hb.ac, sDown);
                        hb.sound(sp);
                    }
                }
            }
        });
    }
}
