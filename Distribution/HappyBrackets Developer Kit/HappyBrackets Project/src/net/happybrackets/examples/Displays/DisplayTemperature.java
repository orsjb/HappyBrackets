package net.happybrackets.examples.Displays;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.HTS221;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.lang.invoke.MethodHandles;

/**
 * Display the values of the sensor in the GUI
 */
public class DisplayTemperature implements HBAction {

    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void action(HB hb) {


        HTS221 mySensor = (HTS221) hb.getSensor(HTS221.class);

        DynamicControl control_temp = hb.createDynamicControl(ControlType.FLOAT, "Temperature");

        if (mySensor != null) {
            mySensor.addListener(new SensorUpdateListener() {


                @Override
                public void sensorUpdated() {

                    control_temp.setValue(mySensor.getTemperatureData());
                }

            });
        }
    }
}

