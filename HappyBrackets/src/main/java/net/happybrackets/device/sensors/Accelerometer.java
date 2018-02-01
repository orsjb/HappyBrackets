package net.happybrackets.device.sensors;

import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.sensor_types.AccelerometerSensor;

/**
 * Default Accelerometer for HB.
 * The type of accelerometer will be detected and listeners will register to receive Accelerometer events
 */
public class Accelerometer extends Sensor implements AccelerometerSensor {
    private static Sensor defaultSensor = null;

    // these are our axis
    private double x, y, z;

    //The value we will round our sensor value to. -1 is not rounding
    private int xRounding = -1, yRounding = -1, zRounding = -1;


    /**
     * Remove all rounding
     */
    public void resetToDefault(){
        xRounding = -1;
        yRounding = -1;
        zRounding = -1;
    }


    /**
     * Will detect connected Sensor and return it
     * @return
     */
    @SuppressWarnings("deprecation")
    private Sensor loadSensor(){

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
                        System.out.println("Valid Load of LSM95DS1");
                        defaultSensor = sensor;
                        LSM9DS1 finalSensor = sensor;
                        sensor.addNonResettableListener(new SensorUpdateListener() {
                            @Override
                            public void sensorUpdated() {
                                boolean send_notify = setX(finalSensor.getAccelerometerX());
                                send_notify |= setY(finalSensor.getAccelerometerY());
                                send_notify |= setZ(finalSensor.getAccelerometerZ());

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
                            sensor.addNonResettableListener(new SensorUpdateListener() {
                                @Override
                                public void sensorUpdated() {
                                    boolean send_notify = setX(finalSensor.getAccelerometerX());
                                    send_notify |= setY(finalSensor.getAccelerometerY());
                                    send_notify |= setZ(finalSensor.getAccelerometerZ());

                                    if (send_notify) {
                                        notifyListeners();
                                    }
                                }
                            });
                        }
                    }

                } catch (Exception e) {
                    System.out.println("loading MiniMU not found");
                }
            }

            if (defaultSensor == null && HB.isEnableSimulators()) {
                AccelerometerSimulator sensor = new AccelerometerSimulator();

                if (sensor != null) {
                    sensor.addNonResettableListener(new SensorUpdateListener() {
                        @Override
                        public void sensorUpdated() {
                            boolean send_notify = setX(sensor.getAccelerometerX());
                            send_notify |= setY(sensor.getAccelerometerY());
                            send_notify |= setZ(sensor.getAccelerometerZ());

                            if (send_notify) {
                                notifyListeners();
                            }
                        }
                    });

                    defaultSensor = sensor;
                }
            }
        }

        if (defaultSensor != null) {
            storeSensor(this);
        }

        setValidLoad (defaultSensor != null);
        return  defaultSensor;
    }

    /**
     * Set the new axis value based on resolution.
     * If the new value causes the class value to change, we will return true
     * so we can know that we need to send an update
     * @param new_val the new value to test or set
     * @return true if we are overwriting th3 value
     */
    private boolean setX(double new_val){
        boolean ret = false;

        new_val = roundValue(new_val, xRounding);
        if (x != new_val) {
            x = new_val;
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
    private boolean setZ(double new_val){
        boolean ret = false;

        new_val = roundValue(new_val, zRounding);
        if (z != new_val) {
            z = new_val;
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
    private boolean setY(double new_val){
        boolean ret = false;

        new_val = roundValue(new_val, yRounding);
        if (y != new_val) {
            y = new_val;
            ret = true;
        }
        return  ret;
    }

    /**
     * Set the resolution for all three axis to the number of decimal places
     * set by resolution. A value of -1 will remove rounding
     * @param resolution the number of decimal places to round to. -1 will be no rounding
     * @return this object
     */
    public Accelerometer setRounding(int resolution){
        xRounding = resolution;
        yRounding = resolution;
        zRounding = resolution;
        return this;
    }

    /**
     * Set the resolution for X axis to the number of decimal places
     * set by resolution. A value of -1 will remove rounding
     * @param resolution the number of decimal places to round to. -1 will be no rounding
     */
    public void setXRounding(int resolution){
        xRounding = resolution;
    }

        /**
     * Set the resolution for Y axis to the number of decimal places
     * set by resolution. A value of -1 will remove rounding
     * @param resolution the number of decimal places to round to. -1 will be no rounding
     */
    public void setYRounding(int resolution){
        yRounding = resolution;
    }

        /**
     * Set the resolution for Z axis to the number of decimal places
     * set by resolution. A value of -1 will remove rounding
     * @param resolution the number of decimal places to round to. -1 will be no rounding
     */
    public void setZRounding(int resolution){
        zRounding = resolution;
    }


    /**
     * Loads the default connected sensor
     */
    public Accelerometer(){

        try {
            loadSensor();
            setValidLoad(defaultSensor != null);
        }
        catch (Exception ex){

        }
    }




    @Override
    public String getSensorName() {
        return "Accelerometer";
    }


    @Override
    public double[] getAccelerometerData() {
        return new double[]{(float)x, (float)y, (float)z};
    }

    @Override
    public float getAccelerometerX() {
        return (float)x;
    }

    @Override
    public float getAccelerometerY() {
        return (float)y;
    }

    @Override
    public float getAccelerometerZ() {
        return (float)z;
    }
}
