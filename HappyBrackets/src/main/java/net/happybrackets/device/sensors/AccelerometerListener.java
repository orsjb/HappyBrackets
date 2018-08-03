package net.happybrackets.device.sensors;

import net.happybrackets.device.HB;

/**
 * Creates an accelerometer listener.
 * User needs to override abstract method sensorUpdate
 */
public abstract class AccelerometerListener {

    private Accelerometer accelerometer = null;

    public AccelerometerListener(HB hb)
    {
        /*****************************************************
         * Find an accelerometer sensor. If no sensor is found
         * you will receive a status message
         * accelerometer values typically range from -1 to + 1
         * to create this code, simply type accelerometerSensor
         *****************************************************/
        try {
            accelerometer = (Accelerometer) hb.findSensor(Accelerometer.class);

            accelerometer.addValueChangedListener(sensor -> {
                Accelerometer accelerometer = (Accelerometer) sensor;
                float x_val = accelerometer.getAccelerometerX();
                float y_val = accelerometer.getAccelerometerY();
                float z_val = accelerometer.getAccelerometerZ();

                /******** Write your code below this line ********/
                sensorUpdate(x_val, y_val, z_val);

                /******** Write your code above this line ********/

            });

        } catch (SensorNotFoundException e) {
            System.out.println("Unable to create Accelerometer");
            hb.setStatus("Unable to create Accelerometer");
        }
        /*** End accelerometerSensor code ***/
    }

    /**
     * Set the resolution for all three axis to the number of decimal places
     * set by resolution. A value of -1 will remove rounding
     * @param resolution the number of decimal places to round to. -1 will be no rounding
     */
    public void setRounding(int resolution) {
        if (accelerometer != null) {
            accelerometer.setRounding(resolution);
        }
    }
    /**
     * Sensor has been updated
     * @param x_val x value of accelerometer
     * @param y_val y value of accelerometer
     * @param z_val z value of accelerometer
     */
    abstract public void  sensorUpdate (float x_val, float y_val, float z_val);
}
