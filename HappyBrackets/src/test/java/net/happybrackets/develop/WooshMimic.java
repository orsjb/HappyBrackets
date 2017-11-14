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

/**
 * For this example we want to look at the accelerometer and use it to trigger a sound when you turn over the
 * accelerometer.
 */
public class WooshMimic implements HBAction {

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

        // We are just going to use one listener and any change to any of the globals will read value
        DynamicControl.DynamicControlListener gyro_control_listener = new DynamicControl.DynamicControlListener(){
            double prevMag = 0;
            double val = 0;


            @Override
            public void update(DynamicControl control) {
                double zAxis = (float) gyro_z.getValue();
                double yAxis = (float) gyro_y.getValue();
                double xAxis = (float) gyro_x.getValue();

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
        };

        gyro_x.addControlListener(gyro_control_listener);
        gyro_y.addControlListener(gyro_control_listener);
        gyro_z.addControlListener(gyro_control_listener);
    }
}
