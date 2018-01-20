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
import net.happybrackets.device.sensors.Sensor;
import net.happybrackets.device.sensors.SensorUpdateListener;
import net.happybrackets.device.sensors.SensorValueChangedListener;

/**
 * For this example we want to look at the accelerometer and use it to trigger a sound when you turn over the
 * accelerometer.
 */
public class GyroscopeSend implements HBAction {


    final int MIN_SENSOR_WAIT = 10; // we will not send messages greater than one per 10ms

    long lastSendTime;

    final String CONTROL_PREFIX = "Gyro-";
    @Override
    public void action(HB hb) {

        hb.reset();
        hb.testBleep();

        lastSendTime = System.currentTimeMillis();

        DynamicControl control_pitch = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "Pitch", 0.0);
        DynamicControl control_roll = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "Roll", 0.0);
        DynamicControl control_yaw = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "Yaw", 0.0);

        control_pitch.setControlScope(ControlScope.GLOBAL);
        control_roll.setControlScope(ControlScope.GLOBAL);
        control_yaw.setControlScope(ControlScope.GLOBAL);

        DynamicControl min_control_pitch = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "min-pitch", 0.0);
        DynamicControl min_control_roll = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "min-roll", 0.0);
        DynamicControl min_control_yaw = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "min-yaw", 0.0);

        DynamicControl max_control_pitch = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "max-pitch", 0.0);
        DynamicControl max_control_roll = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "max-roll", 0.0);
        DynamicControl max_control_yaw = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "max-yaw", 0.0);


        LSM9DS1 mySensor = (LSM9DS1) hb.getSensor(LSM9DS1.class);
        mySensor.addValueChangedListener(new SensorValueChangedListener() {

            boolean init = false;

            double min_pitch, min_roll, min_yaw, max_pitch, max_roll, max_yaw;


            @Override
            public void sensorUpdated(Sensor sensor) {

                // We are going to see if we have expired

                LSM9DS1 lsm9DS1 = (LSM9DS1)sensor;
                long expired = System.currentTimeMillis() - lastSendTime;

                if (expired > MIN_SENSOR_WAIT) {
                    lastSendTime = System.currentTimeMillis();

                    // Get the data from Z.
                    double yaw = lsm9DS1.getYaw();
                    double roll = lsm9DS1.getRoll();
                    double pitch = lsm9DS1.getPitch();


                    if (!init) {
                        min_pitch = pitch;
                        max_pitch = pitch;
                        min_roll = roll;
                        max_roll = roll;
                        min_yaw = yaw;
                        max_yaw = yaw;
                        init = true;
                    }

                    control_pitch.setValue((float) pitch);
                    control_roll.setValue((float) roll);
                    control_yaw.setValue((float) yaw);

                    if (pitch > max_pitch) {
                        max_pitch = pitch;

                    }

                    if (roll > max_roll) {
                        max_roll = roll;

                    }

                    if (yaw > max_yaw) {
                        max_yaw = yaw;

                    }

                    // now show min
                    if (pitch < min_pitch) {
                        min_pitch = pitch;

                    }

                    if (roll < min_roll) {
                        min_roll = roll;
                    }

                    if (yaw < min_yaw) {
                        min_yaw = yaw;
                    }

                    max_control_pitch.setValue((float) max_pitch);
                    max_control_roll.setValue((float) max_roll);
                    max_control_yaw.setValue((float) max_yaw);

                    min_control_pitch.setValue((float) min_pitch);
                    min_control_roll.setValue((float) min_roll);
                    min_control_yaw.setValue((float) min_yaw);
                }

            }
        });
    }
}
