package net.happybrackets.examples.Displays;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.lang.invoke.MethodHandles;

/**
 * Display the values of the sensor in the GUI
 */
public class DisplayAccelerometer implements HBAction {

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

        DynamicControl control_x = hb.createDynamicControl(ControlType.FLOAT, "x");
        DynamicControl control_y = hb.createDynamicControl(ControlType.FLOAT, "y");
        DynamicControl control_z = hb.createDynamicControl(ControlType.FLOAT, "z");


        DynamicControl min_control_x = hb.createDynamicControl(ControlType.FLOAT, "min-x");
        DynamicControl min_control_y = hb.createDynamicControl(ControlType.FLOAT, "min-y");
        DynamicControl min_control_z = hb.createDynamicControl(ControlType.FLOAT, "min-z");

        DynamicControl max_control_x = hb.createDynamicControl(ControlType.FLOAT, "max-x");
        DynamicControl max_control_y = hb.createDynamicControl(ControlType.FLOAT, "max-y");
        DynamicControl max_control_z = hb.createDynamicControl(ControlType.FLOAT, "max-z");


        // Create a reset button
        hb.createDynamicControl(this, ControlType.TRIGGER, "Reset", 0).addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl dynamicControl) {
                // set initialised to false
                initialisedMaxMin = false;
            }
        });

        LSM9DS1 mySensor = (LSM9DS1) hb.getSensor(LSM9DS1.class);
        if (mySensor != null) {
            mySensor.addListener(new SensorUpdateListener() {


                double min_x, min_y, min_z, max_x, max_y, max_z;


                @Override
                public void sensorUpdated() {

                    // Get the data from Z.
                    double zAxis = mySensor.getAccelerometerZ();
                    double yAxis = mySensor.getAccelerometerY();
                    double xAxis = mySensor.getAccelerometerX();


                    if (!initialisedMaxMin) {
                        min_x = xAxis;
                        max_x = xAxis;
                        min_y = yAxis;
                        max_x = yAxis;
                        min_z = zAxis;
                        max_z = zAxis;
                        initialisedMaxMin = true;
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

            });
        }
    }
}

