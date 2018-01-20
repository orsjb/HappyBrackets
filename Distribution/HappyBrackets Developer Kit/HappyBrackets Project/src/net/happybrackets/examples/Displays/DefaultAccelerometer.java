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
public class DefaultAccelerometer implements HBAction {

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

        DynamicControl x_axis = hb.createDynamicControl(ControlType.FLOAT, "AccelX");


        try {
            hb.createSensor(Accelerometer.class).addListener(new SensorUpdateListener() {
                @Override
                public void sensorUpdated() {
                    //x_axis.setValue(mySensor.getAccelerometerX());
                }
            });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}

