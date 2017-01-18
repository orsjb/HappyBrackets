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

import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 * In this example, we look at grabbing accelerometer data from the SenseHat.
 *
 * 1. We reset hb
 * 2. We instantiate an instance of the sensor class.
 * 3. We attach a listener to this class, and
 * 4. ...in the listener we override sensorUpdated, to run the code we want to run.
 *
 * For this code task we want to look at the accelerometer and use it to
 * trigger a sound when you turn over the accelerometer.
 *
 * You'll have to run this with a pi ssh session open to see the text output.
 *
 *
 */

public class Example8_1 implements HBAction {

    double[] dataBuffer = new double[12];
    int j = 0;

    @Override
    public void action(HB hb) {

        // Reset HB
        hb.reset();

        // Create the connection to the Accelerometer
        LSM9DS1 lsm = (LSM9DS1) hb.getSensor(LSM9DS1.class);

        // The listener is added to the Accelerometer connection, and we overload sensorUpdated with our code.
        lsm.addListener(new SensorUpdateListener() {

            @Override
            public void sensorUpdated() {

                // 3 Values (x, y, z) will go into accelData
                double[] accelData = lsm.getAccelerometerData();

                // Here they are printed out to the screen.
                String data = accelData[0] + " " + accelData[1] + " " + accelData[2];
                System.out.println(data);


                // To show the z value next to a smoothed z value, uncomment the below.
//                dataBuffer[j] =  accelData[2]; // Add the z value to the buffer
//                j = (j + 1) % 12; // increment, skipping back to zero when you get to 12
//
//                // Here the two values are printed out to the screen.
//                String dataMean = accelData[2] + " " + mean(dataBuffer);
//                System.out.println(dataMean);

            }
        });
    }

    public double mean(double[] dataBuffer) {

        double meanValue = 0; // initialise meanValue at zero

        // step through entire buffer, whatever length it is.
        for (int index = 0; index < dataBuffer.length; index++) {

            // Add the element in the buffer to the 'meanValue' total - it will get to be quite large
            meanValue += dataBuffer[index];
        }

        meanValue /= dataBuffer.length;  // Divide the meanValue total by the number of values in the buffer

        return meanValue; // Return the meanValue.

    }
}
