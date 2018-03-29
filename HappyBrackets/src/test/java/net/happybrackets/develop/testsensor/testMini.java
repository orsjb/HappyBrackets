package net.happybrackets.develop.testsensor;

import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.SensorNotFoundException;

import java.lang.invoke.MethodHandles;

public class testMini implements HBAction {
    @Override
    public void action(HB hb) {

        hb.setStatus("LOAD MINI");
        /*****************************************************
         * Find an accelerometer sensor. If no sensor is found
         * you will receive a status message
         * accelerometer values typically range from -1 to + 1
         * to create this code, simply type accelerometerSensor
         *****************************************************/
        try {
            hb.findSensor(Accelerometer.class).addValueChangedListener(sensor -> {
                Accelerometer accelerometer = (Accelerometer) sensor;
                float x_val = accelerometer.getAccelerometerX();
                float y_val = accelerometer.getAccelerometerY();
                float z_val = accelerometer.getAccelerometerZ();

                /******** Write your code below this line ********/
                hb.setStatus("X" + x_val);

                /******** Write your code above this line ********/

            });

        } catch (SensorNotFoundException e) {
            hb.setStatus("Unable to create Accelerometer");
        }
        /*** End accelerometerSensor code ***/
    }

    /**
     * This function is used when running sketch in IntelliJ IDE for debugging or testing
     *
     * @param args standard args required
     */
    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
