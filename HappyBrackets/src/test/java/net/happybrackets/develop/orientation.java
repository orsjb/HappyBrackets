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
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 * For this example we want to look at the accelerometer and use it to trigger a sound when you turn over the
 * accelerometer.
 */
public class orientation implements HBAction {

    public enum Orientation {UP, DOWN}

    Orientation currentOri = Orientation.UP;
    Orientation previousOri = Orientation.DOWN;



    @Override
    public void action(HB hb) {
//        hb.reset();
//        hb.testBleep();
        //load a set of sounds
        Sample d1 = SampleManager.sample("data/audio/drone1.wav");
        Sample d2 = SampleManager.sample("data/audio/drone2.wav");
        Sample d3 = SampleManager.sample("data/audio/drone3.wav");


        Envelope e = new Envelope(hb.ac, 0.001f);

        SampleManager.group("Guitar", "data/audio/Nylon_Guitar");
        //play a new random sound
        Sample s1 = SampleManager.fromGroup("Guitar", 3);

        LSM9DS1 mySensor = (LSM9DS1) hb.getSensor(LSM9DS1.class);
        mySensor.addListener(new SensorUpdateListener() {
            @Override
            public void sensorUpdated() {
                // Get the data from Z.
                double zAxis = mySensor.getAccelerometerData()[2];
                double yAxis = mySensor.getAccelerometerData()[1];
                double xAxis = mySensor.getAccelerometerData()[0];

                double spin = Math.sqrt(Math.sqrt(xAxis+yAxis)+zAxis);

                mySensor.getCompassRaw();

                SamplePlayer sp_flat = new SamplePlayer(hb.ac, d1);
                SamplePlayer sp_flat_u = new SamplePlayer(hb.ac, d2);
                SamplePlayer sp_long_edge_a = new SamplePlayer(hb.ac, d3);
                SamplePlayer sp_long_edge_b = new SamplePlayer(hb.ac, d1);
                SamplePlayer sp_short_edge_a = new SamplePlayer(hb.ac, d2);
                SamplePlayer sp_short_edge_b = new SamplePlayer(hb.ac, d3);

                sp_flat_u.getRateUGen().setValue(2.0f);
                sp_long_edge_b.getRateUGen().setValue(2.0f);
                sp_short_edge_b.getRateUGen().setValue(2.0f);

                //sp_flat
                Gain sp_flat_g = new Gain(hb.ac, 1, 1);
                sp_flat_g.addInput(sp_flat);
                //sp_flat_u
                Gain sp_flat_u_g = new Gain(hb.ac, 1, 1);
                sp_flat_u_g.addInput(sp_flat_u);
                //sp_long_edge_a
                Gain sp_long_edge_a_g = new Gain(hb.ac, 1, 1);
                sp_long_edge_a_g.addInput(sp_long_edge_a);
                //sp_long_edge_b
                Gain sp_long_edge_b_g = new Gain(hb.ac, 1, 1);
                sp_long_edge_b_g.addInput(sp_long_edge_b);
                //sp_short_edge_a
                Gain sp_short_edge_a_g = new Gain(hb.ac, 1, 1);
                sp_short_edge_a_g.addInput(sp_short_edge_a);
                //sp_short_edge_b
                Gain sp_short_edge_b_g = new Gain(hb.ac, 1, 1);
                sp_short_edge_b_g.addInput(sp_short_edge_b);

                System.out.println("Accelerometer: "+ " " + "x" + (xAxis * 100) + " " + "y" + (yAxis * 100) + " " +"z"+ (zAxis * 100));
                e.addSegment((float)spin,10);

                    double xAxis100 = xAxis * 100;
                    double yAxis100 = yAxis * 100;

                    //Long Edge
                    if (xAxis100 > 90){
                        System.out.println("sp_long_edge_a");
                        hb.sound(sp_long_edge_a);
                    }

                    if (xAxis100 < -90){
                        System.out.println("sp_long_edge_b");
                        hb.sound(sp_long_edge_b);
                    }

                    //Short Edge
                    if (yAxis100 > 90){
                        System.out.println("sp_short_edge_a");
                        hb.sound(sp_short_edge_a);
                    }

                    if (yAxis100 < -90){
                        System.out.println("sp_short_edge_b");
                        hb.sound(sp_short_edge_b);
                    }

            }
        });
    }


}
