package net.happybrackets.examples.Displays;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.Gyroscope;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.lang.invoke.MethodHandles;

/**
 * Display the values of the sensor in the GUI
 */
public class DisplayGyroscope implements HBAction {

    boolean initialisedMaxMin = false;

    final float MAX_GYRO = 1;
    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void action(HB hb) {

        Gyroscope mySensor = (Gyroscope) hb.getSensor(Gyroscope.class);

        DynamicControl control_pitch = hb.createDynamicControl(ControlType.FLOAT, "Pitch").setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(ControlType.FLOAT, "Pitch", 0, -1 * MAX_GYRO, MAX_GYRO).setControlScope(ControlScope.SKETCH);
        DynamicControl control_y  = hb.createDynamicControl(ControlType.FLOAT, "Gyro Y", 0, -1 * MAX_GYRO, MAX_GYRO);

        DynamicControl control_roll = hb.createDynamicControl(ControlType.FLOAT, "Roll").setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(ControlType.FLOAT, "Roll", 0, -1* MAX_GYRO, MAX_GYRO).setControlScope(ControlScope.SKETCH);
        DynamicControl control_x  = hb.createDynamicControl(ControlType.FLOAT, "Gyro X", 0, -1 * MAX_GYRO, MAX_GYRO);

        DynamicControl control_yaw = hb.createDynamicControl(ControlType.FLOAT, "Yaw").setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(ControlType.FLOAT, "Yaw", 0, -1* MAX_GYRO, MAX_GYRO).setControlScope(ControlScope.SKETCH);
        DynamicControl control_z  = hb.createDynamicControl(ControlType.FLOAT, "Gyro Z", 0, -1 * MAX_GYRO, MAX_GYRO);

        DynamicControl min_control_pitch = hb.createDynamicControl(ControlType.FLOAT, "min-pitch");
        DynamicControl min_control_roll = hb.createDynamicControl(ControlType.FLOAT, "min-roll");
        DynamicControl min_control_yaw = hb.createDynamicControl(ControlType.FLOAT, "min-yaw");

        DynamicControl max_control_pitch = hb.createDynamicControl(ControlType.FLOAT, "max-pitch");
        DynamicControl max_control_roll = hb.createDynamicControl(ControlType.FLOAT, "max-roll");
        DynamicControl max_control_yaw = hb.createDynamicControl(ControlType.FLOAT, "max-yaw");


        // Create a reset button
        hb.createDynamicControl(this, ControlType.TRIGGER, "Reset", 0).addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl dynamicControl) {
                // set initialised to false
                initialisedMaxMin = false;
            }
        });

        // Add a resolution
        DynamicControl resolution = hb.createDynamicControl(ControlType.INT, "Gyro Resolution", -1, -1, 8).addControlListener(new DynamicControl.DynamicControlListener() {
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


                double min_pitch, min_roll, min_yaw, max_pitch, max_roll, max_yaw;


                @Override
                public void sensorUpdated() {

                    // Get the data from Z.
                    double yaw = mySensor.getYaw();
                    double roll = mySensor.getRoll();
                    double pitch = mySensor.getPitch();


                    if (!initialisedMaxMin) {
                        min_pitch = pitch;
                        max_pitch = pitch;
                        min_roll = roll;
                        max_roll = roll;
                        min_yaw = yaw;
                        max_yaw = yaw;
                        initialisedMaxMin = true;
                    }

                    control_pitch.setValue((float) pitch);
                    control_roll.setValue((float) roll);
                    control_yaw.setValue((float) yaw);

                    control_y.setValue(mySensor.getGyroscopeY());
                    control_x.setValue(mySensor.getGyroscopeX());
                    control_z.setValue(mySensor.getGyroscopeZ());

                    if (pitch > max_pitch) {
                        max_pitch = pitch;

                    }

                    if (roll > max_roll) {
                        max_roll = roll;

                    }

                    if (yaw > max_yaw) {
                        max_yaw = yaw;

                    }

                    // now show min
                    if (pitch < min_pitch) {
                        min_pitch = pitch;

                    }

                    if (roll < min_roll) {
                        min_roll = roll;
                    }

                    if (yaw < min_yaw) {
                        min_yaw = yaw;
                    }

                    max_control_pitch.setValue((float) max_pitch);
                    max_control_roll.setValue((float) max_roll);
                    max_control_yaw.setValue((float) max_yaw);

                    min_control_pitch.setValue((float) min_pitch);
                    min_control_roll.setValue((float) min_roll);
                    min_control_yaw.setValue((float) min_yaw);
                }

            });
        }
    }
}

