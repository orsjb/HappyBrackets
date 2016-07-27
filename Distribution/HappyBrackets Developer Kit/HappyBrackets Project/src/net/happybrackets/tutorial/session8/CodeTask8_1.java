package net.happybrackets.tutorial.session8;

import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

/** For this code task we want to look at the accelerometer and use it to 
 * trigger a sound when you turn over the accelerometer.
 * 
 */

public class CodeTask8_1 implements HBAction {



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
}
