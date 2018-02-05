package net.happybrackets.examples.Displays;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
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

        hb.reset();
        Accelerometer mySensor = (Accelerometer) hb.getSensor(Accelerometer.class);

        DynamicControl control_x = hb.createControlBuddyPair(this, ControlType.FLOAT, "accel x", 0, -1, 1);

        DynamicControl control_y = hb.createControlBuddyPair(this, ControlType.FLOAT, "accel y", 0, -1, 1);

        DynamicControl control_z = hb.createControlBuddyPair(this, ControlType.FLOAT, "accel z", 0, -1, 1);

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


        DynamicControl resolution = hb.createDynamicControl(ControlType.INT, "Accel Resolution", -1, -1, 8).addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl dynamicControl) {
                if (mySensor != null)
                {
                    int resolution = (int)dynamicControl.getValue();
                    mySensor.setRounding(resolution);
                }
            }
        });

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
                        max_y = yAxis;
                        min_z = zAxis;
                        max_z = zAxis;
                        initialisedMaxMin = true;
                    }

                    control_x.setValue((float) xAxis);
                    control_y.setValue((float) yAxis);
                    control_z.setValue((float) zAxis);

                    if (xAxis > max_x) {
                        max_x = xAxis;

                    }

                    if (yAxis > max_y) {
                        max_y = yAxis;

                    }

                    if (zAxis > max_z) {
                        max_z = zAxis;

                    }

                    // now show mini
                    if (xAxis < min_x) {
                        min_x = xAxis;

                    }

                    if (yAxis < min_y) {
                        min_y = yAxis;

                    }

                    if (zAxis < min_z) {
                        min_z = zAxis;

                    }

                    max_control_y.setValue((float) max_y);
                    max_control_x.setValue((float) max_x);
                    max_control_z.setValue((float) max_z);
                    min_control_x.setValue((float) min_x);
                    min_control_y.setValue((float) min_y);
                    min_control_z.setValue((float) min_z);

                }

            });
        }
    }
}

