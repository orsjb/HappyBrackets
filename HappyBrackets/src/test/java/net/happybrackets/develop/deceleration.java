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

package mappings;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 * For this example we want to look at the accelerometer and use it to trigger a sound when you turn over the
 * accelerometer.
 */
public class deceleration implements HBAction {

    public enum Orientation {UP, DOWN}

    Orientation currentOri = Orientation.UP;
    Orientation previousOri = Orientation.DOWN;

    double newNumberZ = 0;
    double oldNumberZ = 0;
    double numberDifferenceZ = 0;
    double oldNumberDifferenceZ = 0;

    double newNumberY = 0;
    double oldNumberY = 0;
    double numberDifferenceY = 0;
    double oldNumberDifferenceY = 0;

    double newNumberX = 0;
    double oldNumberX = 0;
    double numberDifferenceX = 0;
    double oldNumberDifferenceX = 0;

    @Override
    public void action(HB hb) {
        hb.reset();
        hb.testBleep();
        //load a set of sounds
//        Sample d1 = SampleManager.sample("data/audio/dream_voice/1.wav");
//        Sample d2 = SampleManager.sample("data/audio/dream_voice/2.wav");
//        Sample d3 = SampleManager.sample("data/audio/dream_voice/3.wav");


        BiquadFilter bf = new BiquadFilter(hb.ac, 1, BiquadFilter.Type.LP);
        Glide freq = new Glide(hb.ac, 800);
        bf.setFrequency(freq);
        bf.setQ(2f);
        bf.setGain(2f);
        PolyLimit pl = new PolyLimit(hb.ac, 1, 8);
        bf.addInput(pl);
        hb.sound(bf);

        Envelope e = new Envelope(hb.ac, 0.01f);

        SampleManager.group("Guitar", "data/audio/Nylon_Guitar");
        //play a new random sound
        Sample s1 = SampleManager.fromGroup("Guitar", 1);
        Sample s2 = SampleManager.fromGroup("Guitar", 2);
        Sample s3 = SampleManager.fromGroup("Guitar", 3);

        LSM9DS1 mySensor = (LSM9DS1) hb.getSensor(LSM9DS1.class);
        mySensor.addListener(new SensorUpdateListener() {
            @Override
            public void sensorUpdated() {
                // Get the data from Z.
                double zAxis = mySensor.getAccelerometerData()[2];
                double yAxis = mySensor.getAccelerometerData()[1];
                double xAxis = mySensor.getAccelerometerData()[0];

                freq.setValue((float)Math.abs(xAxis) * 3500f );

                double spin = Math.sqrt(Math.sqrt(xAxis+yAxis)+zAxis);

                double[] accelerometerArray = new double[10];



                for (int i = 0; i < accelerometerArray.length; i++){
                    accelerometerArray[i] = zAxis;
                }

                SamplePlayer guitar1 = new SamplePlayer(hb.ac, s1);
                SamplePlayer guitar2 = new SamplePlayer(hb.ac, s2);
                SamplePlayer guitar3 = new SamplePlayer(hb.ac, s3);

                SamplePlayer sp_flat = new SamplePlayer(hb.ac, s1);
                SamplePlayer sp_flat_u = new SamplePlayer(hb.ac, s2);
                SamplePlayer sp_long_edge_a = new SamplePlayer(hb.ac, s3);
                SamplePlayer sp_long_edge_b = new SamplePlayer(hb.ac, s1);
                SamplePlayer sp_short_edge_a = new SamplePlayer(hb.ac, s2);
                SamplePlayer sp_short_edge_b = new SamplePlayer(hb.ac, s3);

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

                //System.out.println("Accelerometer: "+ " " + "x" + (xAxis * 100) + " " + "y" + (yAxis * 100) + " " +"z"+ (zAxis * 100));
                e.addSegment((float)spin,10);

                    double xAxis100 = xAxis * 100;
                    double yAxis100 = yAxis * 100;


                //Z Axis
                newNumberZ = zAxis;

                numberDifferenceZ = Math.abs(newNumberZ - oldNumberZ);

                oldNumberZ = newNumberZ;

                double threshold = 1;

                double thresholdZ = 1;
                if (numberDifferenceZ > threshold && oldNumberDifferenceZ < thresholdZ){
                    System.out.println("Z");

//                    hb.sound(guitar1);
                    pl.addInput(guitar1);
                }

                oldNumberDifferenceZ = numberDifferenceZ;

                //Y Axis
                newNumberY = yAxis;

                numberDifferenceY = Math.abs(newNumberY - oldNumberY);

                oldNumberY = newNumberY;

                double thresholdY = 1;
                if (numberDifferenceY > threshold && oldNumberDifferenceY < thresholdY){
                    System.out.println("Y");

//                    hb.sound(guitar2);
                    pl.addInput(guitar2);
                }
                oldNumberDifferenceY = numberDifferenceY;
                //X Axis
                newNumberX = xAxis;
                numberDifferenceX = Math.abs(newNumberX - oldNumberX);
                oldNumberX = newNumberX;
                double thresholdX = 1;
                if (numberDifferenceX > threshold && oldNumberDifferenceX < thresholdX){
                    System.out.println("X");
//                    hb.sound(guitar3);
                    pl.addInput(guitar3);
                }
                oldNumberDifferenceX = numberDifferenceX;

            }
        });
    }


}
