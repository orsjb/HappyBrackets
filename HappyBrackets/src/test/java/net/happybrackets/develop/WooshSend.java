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

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 * For this example we want to look at the accelerometer and use it to trigger a sound when you turn over the
 * accelerometer.
 */
@SuppressWarnings("deprecated")
public class WooshSend implements HBAction {

    public enum Orientation {UP, DOWN}

    Orientation currentOri = Orientation.UP;
    Orientation previousOri = Orientation.DOWN;

    @Override
    public void action(HB hb) {

        hb.reset();
        hb.testBleep();

        DynamicControl gyro_x = hb.createDynamicControl(this, ControlType.FLOAT, "Gyro-x", 0.0);
        DynamicControl gyro_y = hb.createDynamicControl(this, ControlType.FLOAT, "Gyro-y", 0.0);
        DynamicControl gyro_z = hb.createDynamicControl(this, ControlType.FLOAT, "Gyro-z", 0.0);

        gyro_x.setControlScope(ControlScope.GLOBAL);
        gyro_y.setControlScope(ControlScope.GLOBAL);
        gyro_z.setControlScope(ControlScope.GLOBAL);

        DynamicControl min_gyro_x = hb.createDynamicControl(this, ControlType.FLOAT, "min-Gyro-x", 0.0);
        DynamicControl min_gyro_y = hb.createDynamicControl(this, ControlType.FLOAT, "min-Gyro-y", 0.0);
        DynamicControl min_gyro_z = hb.createDynamicControl(this, ControlType.FLOAT, "min-Gyro-z", 0.0);

        DynamicControl max_gyro_x = hb.createDynamicControl(this, ControlType.FLOAT, "max-Gyro-x", 0.0);
        DynamicControl max_gyro_y = hb.createDynamicControl(this, ControlType.FLOAT, "max-Gyro-y", 0.0);
        DynamicControl max_gyro_z = hb.createDynamicControl(this, ControlType.FLOAT, "max-Gyro-z", 0.0);


        Glide modFreq = new Glide(hb.ac, 20);
        WavePlayer mod = new WavePlayer(hb.ac, modFreq, Buffer.SAW);
        Glide baseFreq = new Glide(hb.ac, 300);
        Function f = new Function(mod, baseFreq) {
            @Override
            public float calculate() {
                return x[1] + 15 * x[0];
            }
        };

        WavePlayer wp = new WavePlayer(hb.ac, f, Buffer.SAW);
        Glide env = new Glide(hb.ac, 0);
        Gain g = new Gain(hb.ac, 1, env);
        g.addInput(wp);


        BiquadFilter bf = new BiquadFilter(hb.ac, 1, BiquadFilter.Type.LP);
        Glide freq = new Glide(hb.ac, 800);
        bf.setFrequency(freq);
        bf.setQ(2f);
        bf.setGain(2f);
        PolyLimit pl = new PolyLimit(hb.ac, 1, 8);
        bf.addInput(pl);

        pl.addInput(g);

        BiquadFilter hpf = new BiquadFilter(hb.ac, 1, BiquadFilter.HP);
        hpf.setFrequency(100);
        hpf.addInput(g);

        Reverb rb = new Reverb(hb.ac);
        rb.setDamping(0.9f);
        rb.addInput(bf);

        hpf.addInput(bf);
        hpf.addInput(rb);
        hb.sound(hpf);


        //load a set of sounds
        SampleManager.group("Guitar", "data/audio/Nylon_Guitar");
        //play a new random sound
        Sample s1 = SampleManager.fromGroup("Guitar", 3);
        Sample s2 = SampleManager.fromGroup("Guitar", 5);
        Sample s3 = SampleManager.fromGroup("Guitar", 1);

        LSM9DS1 mySensor = (LSM9DS1) hb.getSensor(LSM9DS1.class);
        mySensor.addListener(new SensorUpdateListener() {


            double prevMag = 0;
            double val = 0;

            boolean init = false;

            double min_x, min_y, min_z, max_x, max_y, max_z;


            @Override
            public void sensorUpdated() {
                // Get the data from Z.
                double zAxis = mySensor.getGyroscopeData()[2];
                double yAxis = mySensor.getGyroscopeData()[1];
                double xAxis = mySensor.getGyroscopeData()[0];


                if (!init)
                {
                    min_x = xAxis;
                    max_x = xAxis;
                    min_y = yAxis;
                    max_x = yAxis;
                    min_z = zAxis;
                    max_z = zAxis;
                    init = true;
                }

                gyro_x.setValue((float) xAxis);
                gyro_y.setValue((float)yAxis);
                gyro_z.setValue((float)zAxis);

                if (xAxis > max_x){
                    max_x = xAxis;
                    max_gyro_x.setValue((float)max_x);
                }

                if (yAxis > max_y){
                    max_y = yAxis;
                    max_gyro_y.setValue((float)max_y);
                }

                if (zAxis > max_z){
                    max_z = zAxis;
                    max_gyro_z.setValue((float)max_z);
                }

                // now show mini
                if (xAxis < min_x){
                    min_x = xAxis;
                    min_gyro_x.setValue((float)min_x);
                }

                if (yAxis < min_y){
                    min_y = yAxis;
                    min_gyro_y.setValue((float)min_y);
                }

                if (zAxis < min_z){
                    min_z = zAxis;
                    min_gyro_z.setValue((float)min_z);
                }

                modFreq.setValue((float)Math.abs(zAxis*zAxis) * 3f);
                baseFreq.setValue((float)(xAxis*xAxis) * 400f + 20);
                System.out.println("z " + zAxis);

                double mag = Math.sqrt(xAxis*xAxis + yAxis*yAxis + zAxis*zAxis);
                mag /= 20f;
//                mag = mag*mag;

                if(prevMag < mag) {
                    val += (mag - val) * 0.7f;
                } else {
                    val += (mag - val) * 0.0001f;
//                    val += (mag - val) * 0.5f;
                }
//                val = val*val;

                env.setValue((float)val*10f);
                freq.setValue((float)val * 1000);

//                System.out.println("Mag: " + mag);

                prevMag = mag;


            }
        });
    }
}
