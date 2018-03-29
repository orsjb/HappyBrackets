package net.happybrackets.device.sensors;

import net.happybrackets.device.HB;

/**
 * Creates an gyoscope listener.
 * User needs to override abstract method sensorUpdate
 */
public abstract class GyroscopeListener {

    public GyroscopeListener (HB hb){
        /*****************************************************
         * Find an gyroscope sensor. If no sensor is found
         * you will receive a status message
         *
         * to create this code, simply type gyroscopeSensor
         *****************************************************/
        try {
            hb.findSensor(Gyroscope.class).addValueChangedListener(sensor -> {
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
     * Sensor has been updated with these values
     * @param pitch pitch of gyroscope
     * @param roll roll of gyroscope
     * @param yaw yaw of gyroscope
     */
    abstract public void sensorUpdated(float pitch, float roll, float yaw);
}
