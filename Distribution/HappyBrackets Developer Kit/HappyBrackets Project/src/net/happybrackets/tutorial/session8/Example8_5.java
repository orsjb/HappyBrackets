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

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.HTS221;
import net.happybrackets.device.sensors.LPS25H;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 * This example shows different kinds of data we can get from the SenseHat.
 */
public class Example8_5 implements HBAction {

    @Override
    public void action(HB hb) {
        hb.reset();
        HTS221 hts = (HTS221) hb.getSensor(HTS221.class);
        hts.addListener(new SensorUpdateListener() {
            @Override
            public void sensorUpdated() {
                // get data
                double humidity = hts.getHumidityData();
                double temperature = hts.getTemperatureData();
                System.out.println("Humidity: " + humidity);
                System.out.println("Temperature: " + temperature);
            }
        });
        LPS25H lps = (LPS25H) hb.getSensor(LPS25H.class);
        lps.addListener(new SensorUpdateListener() {
            @Override
            public void sensorUpdated() {
                // get data
                double pressure = lps.getBarometricPressureData();
                System.out.println("Pressure: " + pressure);
            }
        });

    }

}
