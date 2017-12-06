package net.happybrackets.device.sensors;

import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.sensor_types.MagnetometerSensor;


import java.util.ArrayList;
import java.util.List;

/**
 * Default Accelerometer for HB.
 * The type of accelerometer will be detected and listeners will register to receive Accelerometer events
 */
public class Magnetometer extends Sensor implements MagnetometerSensor
{
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
                            x = sensor.getMagnetometerX();
                            y = sensor.getMagnetometerY();
                            z = sensor.getMagnetometerZ();

                            notifyListeners();
                        }
                    });

                }

            } catch (Exception e) {
            }

            if (defaultSensor == null) {
                try {
                    MiniMU sensor =  MiniMU.class.getConstructor().newInstance();
                    if (sensor != null) {
                        defaultSensor = sensor;
                        sensor.addListener(new SensorUpdateListener() {
                            @Override

                            public void sensorUpdated() {
                                x = sensor.getMagnetometerX();
                                y = sensor.getMagnetometerY();
                                z = sensor.getMagnetometerZ();

                                notifyListeners();
                            }

                        });

                    }

                } catch (Exception e) {
                }
            }

            if (defaultSensor == null  && HB.isEnableSimulators()) {
                MagnetometerSimulator sensor = new MagnetometerSimulator();

                if (sensor != null) {
                    sensor.addListener(new SensorUpdateListener() {
                        @Override
                            public void sensorUpdated() {
                                x = sensor.getMagnetometerX();
                                y = sensor.getMagnetometerY();
                                z = sensor.getMagnetometerZ();

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
    public Magnetometer(){

        try {
            loadSensor();
        }
        catch (Exception ex){

        }
    }



    @Override
    public String getSensorName() {
        return "Accelerometer";
    }

    @Override
    public double[] getMagnetometerData() {
        return new double[]{x, y, z};
    }

    @Override
    public double getMagnetometerX() {
        return x;
    }

    @Override
    public double getMagnetometerY() {
        return y;
    }

    @Override
    public double getMagnetometerZ() {
        return z;
    }
}
