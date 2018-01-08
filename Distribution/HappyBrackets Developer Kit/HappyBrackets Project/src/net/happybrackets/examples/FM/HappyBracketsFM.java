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

package net.happybrackets.examples.FM;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.lang.invoke.MethodHandles;

/**
 * To run this code, make sure you have installed the HappyBrackets IntelliJ plugin.
 * Ensure you have a device set up and connected. It should be visible in the devices list in the HappyBrackets plugin.
 * From the Send Composition dropdown menu in the plugin select HappyBracketsHelloWorld and click All.
 */
public class HappyBracketsFM implements HBAction {

    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void action(HB hb) {

        //these are the parameters that control the FM synth
        Glide modFMFreq = new Glide(hb.ac, 1);
        Glide modFMDepth = new Glide(hb.ac, 500);
        Glide baseFmFreq = new Glide(hb.ac, 1000);
        Glide FmGain = new Glide(hb.ac, 0.5f);

        DynamicControl display_status = hb.createDynamicControl(this, ControlType.TEXT, "Status", "");

        //this is the FM synth
        WavePlayer FM_modulator = new WavePlayer(hb.ac, modFMFreq, Buffer.SINE);

        // create a function to change the carrier's frequency
        Function modFunction = new Function(FM_modulator, modFMDepth, baseFmFreq) {
            @Override
            public float calculate() {
                return x[0] * x[1] + x[2];
            }
        };
        WavePlayer FM_carrier = new WavePlayer(hb.ac, modFunction, Buffer.SINE);
        Gain g = new Gain(hb.ac, 1, FmGain);
        g.addInput(FM_carrier);
        hb.sound(g);

        hb.createDynamicControl(this, ControlType.BOOLEAN, "On / OFF", true).addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl dynamicControl) {
                FM_carrier.pause(!(Boolean) dynamicControl.getValue());
            }
        });

        //this is the sensor
        Accelerometer mySensor = (Accelerometer) hb.getSensor(Accelerometer.class);
        if (mySensor != null) {
            mySensor.addListener(new SensorUpdateListener() {
                @Override
                public void sensorUpdated() {
                    float x = (float) mySensor.getAccelerometerX();
                    float y = (float) mySensor.getAccelerometerY();
                    float z = (float) mySensor.getAccelerometerZ();
                    //hb.setStatus(x + " " + y + " " + z);
                    display_status.setValue(x + " " + y + " " + z);
                    //display_x.setValue(x);
                    //display_y.setValue(y);
                    //display_z.setValue(z);

                    // update values
                    float mod_freq = x * 1000;
                    modFMFreq.setValue(mod_freq);
                    //display_freq.setValue (mod_freq);
                }
            });
        }
    }

}
