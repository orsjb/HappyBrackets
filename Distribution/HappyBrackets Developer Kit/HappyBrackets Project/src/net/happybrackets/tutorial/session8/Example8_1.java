package net.happybrackets.tutorial.session8;

import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 * In this example, we look at grabbing accelerometer data from the SenseHat.
 * If you have a different sensor, see the sensor objects available in the HappyBrackets library (replacing the LSM9DS1 class).
 *
 * For this code task we want to look at the accelerometer and use it to
 * trigger a sound when you turn over the accelerometer.
 * 
 */

public class Example8_1 implements HBAction {

    @Override
    public void action(HB hb) {
        hb.reset();
        LSM9DS1 lsm = (LSM9DS1)hb.getSensor(LSM9DS1.class);
        lsm.addListener(new SensorUpdateListener() {
            @Override
            public void sensorUpdated() {
                double[] accelData = lsm.getAccelerometerData();
                String data = accelData[0] + " " + accelData[1] + " " + accelData[2];
                System.out.println(data);
            }
        });
    }

    public double mean(double[] dataBuffer){
        double meanValue = 0;
        for (int index = 0; index < dataBuffer.length; index++) {
            meanValue += dataBuffer[index];
        }
        meanValue /= dataBuffer.length;
        return meanValue;

    }
}
