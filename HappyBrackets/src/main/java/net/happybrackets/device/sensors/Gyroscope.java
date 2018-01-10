package net.happybrackets.device.sensors;

import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.sensor_types.GyroscopeSensor;

/**
 * Default Accelerometer for HB.
 * The type of accelerometer will be detected and listeners will register to receive Accelerometer events
 */
public class Gyroscope extends Sensor implements GyroscopeSensor{
    private static Sensor defaultSensor = null;

    // these are our axis
    private double pitch, roll, yaw;

    //The value we will round our sensor value to. -1 is not rounding
    private int pitchRounding = -1, rollRounding = -1, yawRounding = -1;

    /**
     * Remove all rounding
     */
    public void resetToDefault(){
        pitchRounding = -1;
        rollRounding = -1;
        yawRounding = -1;
    }

    /**
     * Will detect connected Sensor and return it
     * @return
     */
    Sensor loadSensor(){

        if (defaultSensor == null)
        {
            System.out.println("Try Load LSM95DS1");
            try {
                LSM9DS1 sensor = (LSM9DS1) getSensor(LSM9DS1.class);

                if (sensor == null) {
                    sensor = LSM9DS1.class.getConstructor().newInstance();
                }

                if (sensor != null){
                    if (sensor.isValidLoad()) {
                        defaultSensor = sensor;
                        LSM9DS1 finalSensor = sensor;
                        sensor.addListener(new SensorUpdateListener() {
                            @Override
                            public void sensorUpdated() {
                                boolean send_notify = setPitch(finalSensor.getPitch());
                                send_notify |= setRoll(finalSensor.getRoll());
                                send_notify |= setYaw(finalSensor.getYaw());

                                if (send_notify) {
                                    notifyListeners();
                                }
                            }
                        });
                    }
                }

            } catch (Exception e) {
                System.out.println("LSM9DS1 not found");
            }

            if (defaultSensor == null) {
                System.out.println("Try Load MiniMU");

                try {
                    MiniMU sensor = (MiniMU) getSensor(MiniMU.class);
                    if (sensor == null) {
                        sensor = MiniMU.class.getConstructor().newInstance();
                    }

                    if (sensor != null){
                        if (sensor.isValidLoad()) {
                            defaultSensor = sensor;
                            MiniMU finalSensor = sensor;
                            sensor.addListener(new SensorUpdateListener() {
                                @Override
                                public void sensorUpdated() {
                                    boolean send_notify = setPitch(finalSensor.getPitch());
                                    send_notify |= setRoll(finalSensor.getRoll());
                                    send_notify |= setYaw(finalSensor.getRoll());

                                    if (send_notify) {
                                        notifyListeners();
                                    }
                                }
                            });
                        }
                    }

                } catch (Exception e) {
                    System.out.println("MiniMU not found");
                }
            }

            if (defaultSensor == null  && HB.isEnableSimulators()) {
                GyroscopeSimulator sensor = new GyroscopeSimulator();

                if (sensor != null) {
                    sensor.addListener(new SensorUpdateListener() {
                        @Override
                        public void sensorUpdated() {
                            boolean send_notify = setPitch(sensor.getPitch());
                            send_notify |= setRoll(sensor.getRoll());
                            send_notify |= setYaw(sensor.getYaw());

                            if (send_notify) {
                                notifyListeners();
                            }
                        }
                    });

                    defaultSensor = sensor;
                }
            }
        }
        // Store into our static List
        storeSensor(this);
        return  defaultSensor;
    }

    /**
     * Set the new axis value based on resolution.
     * If the new value causes the class value to change, we will return true
     * so we can know that we need to send an update
     * @param new_val the new value to test or set
     * @return true if we are overwriting th3 value
     */
    private boolean setPitch(double new_val){
        boolean ret = false;

        new_val = roundValue(new_val, pitchRounding);
        if (pitch != new_val) {
            pitch = new_val;
            ret = true;
        }
        return  ret;
    }

    /**
     * Set the new axis value based on resolution.
     * If the new value causes the class value to change, we will return true
     * so we can know that we need to send an update
     * @param new_val the new value to test or set
     * @return true if we are overwriting th3 value
     */
    private boolean setYaw(double new_val){
        boolean ret = false;

        new_val = roundValue(new_val, yawRounding);
        if (yaw != new_val) {
            yaw = new_val;
            ret = true;
        }
        return  ret;
    }

    /**
     * Set the new axis value based on resolution.
     * If the new value causes the class value to change, we will return true
     * so we can know that we need to send an update
     * @param new_val the new value to test or set
     * @return true if we are overwriting th3 value
     */
    private boolean setRoll(double new_val){
        boolean ret = false;

        new_val = roundValue(new_val, rollRounding);
        if (roll != new_val) {
            roll = new_val;
            ret = true;
        }
        return  ret;
    }

    /**
     * Set the resolution for all three axis to the number of decimal places
     * set by resolution. A value of -1 will remove rounding
     * @param resolution the number of decimal places to round to. -1 will be no rounding
     */
    public void setRounding(int resolution){
        pitchRounding = resolution;
        rollRounding = resolution;
        yawRounding = resolution;
    }

    /**
     * Set the resolution for X axis to the number of decimal places
     * set by resolution. A value of -1 will remove rounding
     * @param resolution the number of decimal places to round to. -1 will be no rounding
     */
    public void setXRounding(int resolution){
        pitchRounding = resolution;
    }

    /**
     * Set the resolution for Y axis to the number of decimal places
     * set by resolution. A value of -1 will remove rounding
     * @param resolution the number of decimal places to round to. -1 will be no rounding
     */
    public void setYRounding(int resolution){
        rollRounding = resolution;
    }

    /**
     * Set the resolution for Z axis to the number of decimal places
     * set by resolution. A value of -1 will remove rounding
     * @param resolution the number of decimal places to round to. -1 will be no rounding
     */
    public void setZRounding(int resolution){
        yawRounding = resolution;
    }


    /**
     * Loads the default connected sensor
     */
    public Gyroscope(){

        try {
            loadSensor();
        }
        catch (Exception ex){

        }
    }


    @Override
    public String getSensorName() {
        return "Gyroscope";
    }



    @Override
    public double[] getGyroscopeData() {
        return new double[]{pitch, roll, yaw};
    }


    @Override
    public double getPitch() {
        return pitch;
    }

    @Override
    public double getRoll() {
        return roll;
    }

    @Override
    public double getYaw() {
        return yaw;
    }

}
