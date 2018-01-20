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

package net.happybrackets.develop;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 * For this example we want to look at the accelerometer and use it to trigger a sound when you turn over the
 * accelerometer.
 */
@SuppressWarnings("deprecated")
public class Example8_3 implements HBAction {

    public enum Orientation {UP, DOWN}

    Orientation currentOri = Orientation.UP;
    Orientation previousOri = Orientation.DOWN;

    @Override
    public void action(HB hb) {
        hb.reset();
        hb.testBleep();
        //load a set of sounds
        SampleManager.group("Guitar", "data/audio/Nylon_Guitar");
        //play a new random sound
        Sample s1 = SampleManager.fromGroup("Guitar", 3);
        Sample s2 = SampleManager.fromGroup("Guitar", 5);
        Sample s3 = SampleManager.fromGroup("Guitar", 1);

        LSM9DS1 mySensor = (LSM9DS1) hb.getSensor(LSM9DS1.class);
        mySensor.addListener(new SensorUpdateListener() {
            @Override
            public void sensorUpdated() {
                // Get the data from Z.
                double zAxis = mySensor.getGyroscopeData()[2];
                double yAxis = mySensor.getGyroscopeData()[1];
                double xAxis = mySensor.getGyroscopeData()[0];

                double spin = Math.sqrt(Math.sqrt(xAxis+yAxis)+zAxis);
//                double spin = Math.sqrt(xAxis*xAxis + yAxis*yAxis + zAxis*zAxis);

                System.out.println("Spin: " + spin);

                SamplePlayer sp1 = new SamplePlayer(hb.ac, s1);
                SamplePlayer sp2 = new SamplePlayer(hb.ac, s2);
                SamplePlayer sp3 = new SamplePlayer(hb.ac, s3);

                if (spin > 1) {
                    // if so play the sound
                    if (spin > 2) {
                        hb.sound(sp1);
                    }

                    if (spin > 2.5) {
                        hb.sound(sp1);
                        hb.sound(sp2);
                    }
                    if (spin > 3.0) {
                        hb.sound(sp2);
                        hb.sound(sp3);
                    }
                    if (spin > 3.5) {
                        hb.sound(sp3);
                    }

                }

            }
        });
    }
}
