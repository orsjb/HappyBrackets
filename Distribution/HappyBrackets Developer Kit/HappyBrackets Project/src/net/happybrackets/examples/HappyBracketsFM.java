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

package net.happybrackets.examples;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 * To run this code, make sure you have installed the HappyBrackets IntelliJ plugin.
 * Ensure you have a device set up and connected. It should be visible in the devices list in the HappyBrackets plugin.
 * From the Send Composition dropdown menu in the plugin select HappyBracketsHelloWorld and click All.
 */
public class HappyBracketsFM implements HBAction {

    @Override
    public void action(HB hb) {

        //these are the parameters that control the FM synth
        Glide modFreq = new Glide(hb.ac, 666);
        Glide modDepth = new Glide(hb.ac, 100);
        Glide baseFreq = new Glide(hb.ac, 1000);
        Glide gain = new Glide(hb.ac, 0.1f);

        DynamicControl display_sensor = hb.createDynamicControl(this, ControlType.TEXT, "Sensor", "");
        DynamicControl display_freq = hb.createDynamicControl(this, ControlType.FLOAT, "Mod freq", 0.0);
        //this is the FM synth
        WavePlayer modulator = new WavePlayer(hb.ac, modFreq, Buffer.SINE);
        Function modFunction = new Function(modulator, modDepth, baseFreq) {
            @Override
            public float calculate() {
                return x[0] * x[1] + x[2];
            }
        };
        WavePlayer carrier = new WavePlayer(hb.ac, modFunction, Buffer.SINE);
        Gain g = new Gain(hb.ac, 1, gain);
        g.addInput(carrier);
        hb.sound(g);

        //this is the sensor
        LSM9DS1 mySensor = (LSM9DS1) hb.getSensor(LSM9DS1.class);
        mySensor.addListener(new SensorUpdateListener() {
            @Override
            public void sensorUpdated() {
                float x = (float)mySensor.getAccelerometerData()[0];
                float y = (float)mySensor.getAccelerometerData()[1];
                float z = (float)mySensor.getAccelerometerData()[2];
                //hb.setStatus(x + " " + y + " " + z);
                display_sensor.setValue(x + " " + y + " " + z);
                // update values
                float mod_freq = x * 1000;
                modFreq.setValue(mod_freq);
                display_freq.setValue (mod_freq);
            }
        });

    }

}
