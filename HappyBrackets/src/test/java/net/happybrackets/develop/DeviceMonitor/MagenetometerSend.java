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

package net.happybrackets.develop.DeviceMonitor;

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
public class MagenetometerSend implements HBAction {


    final int MIN_SENSOR_WAIT = 10; // we will not send messages greater than one per 10ms

    long lastSendTime;

    final String CONTROL_PREFIX = "Mag-";
    @Override
    public void action(HB hb) {

        hb.reset();
        hb.testBleep();

        lastSendTime = System.currentTimeMillis();

        DynamicControl control_x = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "x", 0.0);
        DynamicControl control_y = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "y", 0.0);
        DynamicControl control_z = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "z", 0.0);


        DynamicControl min_control_x = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "min-x", 0.0);
        DynamicControl min_control_y = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "min-y", 0.0);
        DynamicControl min_control_z = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "min-z", 0.0);

        DynamicControl max_control_x = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "max-x", 0.0);
        DynamicControl max_control_y = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "max-y", 0.0);
        DynamicControl max_control_z = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "max-z", 0.0);


        LSM9DS1 mySensor = (LSM9DS1) hb.getSensor(LSM9DS1.class);
        mySensor.addListener(new SensorUpdateListener() {

            boolean init = false;

            double min_x, min_y, min_z, max_x, max_y, max_z;


            @Override
            public void sensorUpdated() {

                // We are going to see if we have expired

                long expired = System.currentTimeMillis() - lastSendTime;

                if (expired > MIN_SENSOR_WAIT) {
                    lastSendTime = System.currentTimeMillis();

                    // Get the data from Z.
                    double zAxis = mySensor.getMagnetometerZ();
                    double yAxis = mySensor.getMagnetometerY();
                    double xAxis = mySensor.getMagnetometerZ();


                    if (!init) {
                        min_x = xAxis;
                        max_x = xAxis;
                        min_y = yAxis;
                        max_x = yAxis;
                        min_z = zAxis;
                        max_z = zAxis;
                        init = true;
                    }

                    control_x.setValue((float) xAxis);
                    control_y.setValue((float) yAxis);
                    control_z.setValue((float) zAxis);

                    if (xAxis > max_x) {
                        max_x = xAxis;
                        max_control_x.setValue((float) max_x);
                    }

                    if (yAxis > max_y) {
                        max_y = yAxis;
                        max_control_y.setValue((float) max_y);
                    }

                    if (zAxis > max_z) {
                        max_z = zAxis;
                        max_control_z.setValue((float) max_z);
                    }

                    // now show mini
                    if (xAxis < min_x) {
                        min_x = xAxis;
                        min_control_x.setValue((float) min_x);
                    }

                    if (yAxis < min_y) {
                        min_y = yAxis;
                        min_control_y.setValue((float) min_y);
                    }

                    if (zAxis < min_z) {
                        min_z = zAxis;
                        min_control_z.setValue((float) min_z);
                    }

                }

            }
        });
    }
}
