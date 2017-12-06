package net.happybrackets.device.sensors;

import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.sensor_types.GyroscopeSensor;


import java.util.ArrayList;

/**
 * Default Accelerometer for HB.
 * The type of accelerometer will be detected and listeners will register to receive Accelerometer events
 */
public class Gyroscope extends Sensor implements GyroscopeSensor{
    private static Sensor defaultSensor = null;

    // these are our axis
    private double x, y, z;

    /**
     * Will detect connected Sensor and return it
     * @return
     */
    Sensor loadSensor(){

        if (defaultSensor == null)
        {

            try {
                LSM9DS1 sensor =  LSM9DS1.class.getConstructor().newInstance();
                if (sensor != null){
                    defaultSensor = sensor;
                    sensor.addListener(new SensorUpdateListener() {
                        @Override
                        public void sensorUpdated() {
                            x = sensor.getGyroscopeX();
                            y = sensor.getGyroscopeY();
                            z = sensor.getGyroscopeZ();

                            notifyListeners();
                        }
                    });

                }

            } catch (Exception e) {
            }

            if (defaultSensor == null) {
                try {
                    MiniMU sensor =  MiniMU.class.getConstructor().newInstance();
                    if (sensor != null){
                        defaultSensor = sensor;
                        sensor.addListener(new SensorUpdateListener() {
                            @Override
                            public void sensorUpdated() {
                                x = sensor.getGyroscopeX();
                                y = sensor.getGyroscopeY();
                                z = sensor.getGyroscopeZ();

                                notifyListeners();
                            }
                        });

                    }

                } catch (Exception e) {
                }
            }

            if (defaultSensor == null  && HB.isEnableSimulators()) {
                GyroscopeSimulator sensor = new GyroscopeSimulator();

                if (sensor != null) {
                    sensor.addListener(new SensorUpdateListener() {
                        @Override
                        public void sensorUpdated() {
                            x = sensor.getGyroscopeX();
                            y = sensor.getGyroscopeY();
                            z = sensor.getGyroscopeZ();

                            notifyListeners();
                        }
                    });

                    defaultSensor = sensor;
                }
            }
        }
        return  defaultSensor;
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
        return new double[]{x, y, z};
    }

    @Override
    public double getGyroscopeX() {
        return x;
    }

    @Override
    public double getGyroscopeY() {
        return y;
    }

    @Override
    public double getGyroscopeZ() {
        return z;
    }
}
