package net.happybrackets.examples.Displays;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.lang.invoke.MethodHandles;

/**
 * Display the values of the sensor in the GUI
 */
public class DebugAccelerometer implements HBAction {

    boolean initialisedMaxMin = false;

    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void action(HB hb) {

        Accelerometer mySensor = (Accelerometer)hb.getSensor(Accelerometer.class);

        if (mySensor != null) {
            mySensor.setRounding(5);

            mySensor.addListener(new SensorUpdateListener() {
                @Override
                public void sensorUpdated() {
                    System.out.println(mySensor.getAccelerometerX());
                    System.out.println(mySensor.getAccelerometerY());
                    System.out.println(mySensor.getAccelerometerZ());
                }
            });
        }
    }
}

