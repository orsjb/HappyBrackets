package net.happybrackets.develop.DeviceMonitor;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.*;

import java.lang.invoke.MethodHandles;

public class DefaultAccelerometer implements HBAction{

    final String ACCEL_PREFIX = "Accel-";
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

        // display text and sliders
        DynamicControl control_x = hb.createDynamicControl(this, ControlType.FLOAT, ACCEL_PREFIX + "x", 0.0).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, ACCEL_PREFIX + "x", 0.0, -1, 1).setControlScope(ControlScope.SKETCH);

        DynamicControl control_y = hb.createDynamicControl(this, ControlType.FLOAT, ACCEL_PREFIX + "y", 0.0).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, ACCEL_PREFIX + "y", 0.0, -1, 1).setControlScope(ControlScope.SKETCH);

        DynamicControl control_z = hb.createDynamicControl(this, ControlType.FLOAT, ACCEL_PREFIX + "z", 0.0).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, ACCEL_PREFIX + "z", 0.0, -1, 1).setControlScope(ControlScope.SKETCH);


        /*****************************************************
         * Find an accelerometer sensor. If no sensor is found
         * you will receive a status message
         * accelerometer values typically range from -1 to + 1
         * to create this code, simply type accelerometerSensor
         *****************************************************/
        new AccelerometerListener(hb) {
            @Override
            public void sensorUpdated(float x_val, float y_val, float z_val) {
                /******** Write your code below this line ********/
                float scaled_x = Sensor.scaleValue(-1, 1, 200, 1000, x_val);
                System.out.println("" + scaled_x);
                /******** Write your code above this line ********/

            }
        };
        /*** End accelerometerSensor code ***/



    }
}
