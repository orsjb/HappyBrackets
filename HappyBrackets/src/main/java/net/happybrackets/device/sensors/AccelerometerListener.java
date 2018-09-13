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
     * Scale the values based on a maximum and minimum sensor value of 1 / -1
     * The value can be greater than scaled_min and scaled_max if sensor_value is greater than abs(1)
     * ((scaled_max - scaled_min) * (sensor_value - sensor_min)) / (sensor_max - sensor_min) + scaled_min;
     * @param scaled_min The value we want our standard minimum sensor value to become
     * @param scaled_max  The value we want our standard maximum sensor value to become
     * @param sensor_value The actual value of our sensor
     * @return the value scaled
     */
    public float scaleValue(double scaled_min, double scaled_max, double sensor_value){
        return Sensor.scaleValue(-1, 1, scaled_min, scaled_max, sensor_value);
    }

    /**
     * Sensor has been updated
     * @param x_val x value of accelerometer
     * @param y_val y value of accelerometer
     * @param z_val z value of accelerometer
     */
    abstract public void  sensorUpdate (float x_val, float y_val, float z_val);
}
