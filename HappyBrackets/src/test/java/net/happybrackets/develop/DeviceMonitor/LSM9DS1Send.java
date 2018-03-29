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
import net.happybrackets.device.sensors.SensorNotFoundException;

/**
 * For this example we want to look at the accelerometer and use it to trigger a sound when you turn over the
 * accelerometer.
 */
public class LSM9DS1Send implements HBAction {


    final int MIN_SENSOR_WAIT = 10; // we will not send messages greater than one per 10ms

    long lastSendTime;

    final String ACCEL_PREFIX = "Accel-";
    final String GYRO_PREFIX = "Gyro -";
    final String MAG_PREFIX = "Mag-";
    
    @Override
    public void action(HB hb) {

        hb.reset();
        hb.testBleep();

        // display text and sliders
        DynamicControl control_x = hb.createDynamicControl(this, ControlType.FLOAT, ACCEL_PREFIX + "x", 0.0).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, ACCEL_PREFIX + "x", 0.0, -1, 1).setControlScope(ControlScope.SKETCH);

        DynamicControl control_y = hb.createDynamicControl(this, ControlType.FLOAT, ACCEL_PREFIX + "y", 0.0).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, ACCEL_PREFIX + "y", 0.0, -1, 1).setControlScope(ControlScope.SKETCH);

        DynamicControl control_z = hb.createDynamicControl(this, ControlType.FLOAT, ACCEL_PREFIX + "z", 0.0).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, ACCEL_PREFIX + "z", 0.0, -1, 1).setControlScope(ControlScope.SKETCH);


        // display text and sliders
        DynamicControl gyro_roll = hb.createDynamicControl(this, ControlType.FLOAT, GYRO_PREFIX + "roll", 0.0).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, GYRO_PREFIX + "roll", 0.0, -1, 1).setControlScope(ControlScope.SKETCH);

        DynamicControl gyro_pitch = hb.createDynamicControl(this, ControlType.FLOAT, GYRO_PREFIX + "pitch", 0.0).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, GYRO_PREFIX + "pitch", 0.0, -1, 1).setControlScope(ControlScope.SKETCH);

        DynamicControl gyro_yaw = hb.createDynamicControl(this, ControlType.FLOAT, GYRO_PREFIX + "yaw", 0.0).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, GYRO_PREFIX + "yaw", 0.0, -1, 1).setControlScope(ControlScope.SKETCH);


        // magnetometer
        DynamicControl mag_x = hb.createDynamicControl(this, ControlType.FLOAT, MAG_PREFIX + "x", 0.0).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, MAG_PREFIX + "x", 0.0, -1000, 1000).setControlScope(ControlScope.SKETCH);

        DynamicControl mag_y = hb.createDynamicControl(this, ControlType.FLOAT, MAG_PREFIX + "y", 0.0).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, MAG_PREFIX + "y", 0.0, -1000, 1000).setControlScope(ControlScope.SKETCH);

        DynamicControl mag_z = hb.createDynamicControl(this, ControlType.FLOAT, MAG_PREFIX + "z", 0.0).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, MAG_PREFIX + "z", 0.0, -1000, 1000).setControlScope(ControlScope.SKETCH);

        
        try {
            hb.findSensor(LSM9DS1.class).addValueChangedListener(sensor -> {
                LSM9DS1 lsm9DS1 = (LSM9DS1) sensor;
                // Enter Your Code here
                float x_val = lsm9DS1.getAccelerometerX();
                float y_val = lsm9DS1.getAccelerometerY();
                float z_val = lsm9DS1.getAccelerometerZ();

                control_x.setValue(x_val);
                control_y.setValue(y_val);
                control_z.setValue( z_val);

                //Stop entering your code here
                gyro_yaw.setValue(lsm9DS1.getYaw());
                gyro_pitch.setValue(lsm9DS1.getPitch());
                gyro_roll.setValue(lsm9DS1.getRoll());

                mag_x.setValue(lsm9DS1.getMagnetometerX());
                mag_y.setValue(lsm9DS1.getMagnetometerY());
                mag_z.setValue(lsm9DS1.getMagnetometerZ());
            });
        } catch (SensorNotFoundException e) {
            hb.setStatus("Accelerometer Fail");
        }

    }
}
