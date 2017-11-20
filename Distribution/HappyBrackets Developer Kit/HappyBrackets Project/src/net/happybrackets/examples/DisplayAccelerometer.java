package net.happybrackets.examples;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

public class DisplayAccelerometer implements HBAction {


    final int MIN_SENSOR_WAIT = 10; // we will not send messages greater than one per 10ms

    long lastSendTime;
    boolean initialisedMaxMin = false;


    final String CONTROL_PREFIX = "Accel-";
    @Override
    public void action(HB hb) {

        lastSendTime = System.currentTimeMillis();

        DynamicControl control_x = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "x", 0.0);
        DynamicControl control_y = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "y", 0.0);
        DynamicControl control_z = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "z", 0.0);


        DynamicControl min_control_x = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "min-x", 0.0);
        DynamicControl min_control_y = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "min-y", 0.0);
        DynamicControl min_control_z = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "min-z", 0.0);

        DynamicControl max_control_x = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "max-x", 0.0);
        DynamicControl max_control_y = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "max-y", 0.0);
        DynamicControl max_control_z = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "max-z", 0.0);


        // Create a reset button
        hb.createDynamicControl(this, ControlType.TRIGGER, "Reset", 0).addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl dynamicControl) {
                // set initialised to false
                initialisedMaxMin = false;
            }
        });

        LSM9DS1 mySensor = (LSM9DS1) hb.getSensor(LSM9DS1.class);
        mySensor.addListener(new SensorUpdateListener() {


            double min_x, min_y, min_z, max_x, max_y, max_z;


            @Override
            public void sensorUpdated() {

                // We are going to see if we have expired

                long expired = System.currentTimeMillis() - lastSendTime;

                // we are putting this delay in so we do not overload the network with messages
                if (expired > MIN_SENSOR_WAIT) {
                    lastSendTime = System.currentTimeMillis();

                    // Get the data from Z.
                    double zAxis = mySensor.getAccelerometerData()[2];
                    double yAxis = mySensor.getAccelerometerData()[1];
                    double xAxis = mySensor.getAccelerometerData()[0];


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

            }
        });
    }
}

