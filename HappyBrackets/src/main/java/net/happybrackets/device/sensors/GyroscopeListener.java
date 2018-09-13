package net.happybrackets.device.sensors;

import net.happybrackets.device.HB;

/**
 * Creates an gyoscope listener.
 * User needs to override abstract method sensorUpdate
 */
public abstract class GyroscopeListener {

    private Gyroscope gyroscope = null;

    public GyroscopeListener (HB hb){
        /*****************************************************
         * Find an gyroscope sensor. If no sensor is found
         * you will receive a status message
         *
         * to create this code, simply type gyroscopeSensor
         *****************************************************/
        try {
            gyroscope = (Gyroscope) hb.findSensor(Gyroscope.class);

            gyroscope.addValueChangedListener(sensor -> {
                Gyroscope gyroscope = (Gyroscope) sensor;
                float pitch = gyroscope.getPitch();
                float roll = gyroscope.getRoll();
                float yaw = gyroscope.getYaw();

                /******** Write your code below this line ********/

                sensorUpdated(pitch, roll, yaw);

                /******** Write your code above this line ********/

            });

        } catch (SensorNotFoundException e) {
            System.out.println("Unable to find gyroscope");
            hb.setStatus("Unable to find gyroscope");
        }
        /*** End gyroscopeSensor code ***/
    }

    /**
     * Set the resolution for all three axis to the number of decimal places
     * set by resolution. A value of -1 will remove rounding
     * @param resolution the number of decimal places to round to. -1 will be no rounding
     */
    public void setRounding(int resolution){
        if (gyroscope != null){
            gyroscope.setRounding(resolution);
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
     * Sensor has been updated with these values
     * @param pitch pitch of gyroscope
     * @param roll roll of gyroscope
     * @param yaw yaw of gyroscope
     */
    abstract public void sensorUpdated(float pitch, float roll, float yaw);
}
