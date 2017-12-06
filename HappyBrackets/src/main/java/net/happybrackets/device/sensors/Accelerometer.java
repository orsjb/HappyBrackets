package net.happybrackets.device.sensors;

import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.sensor_types.AccelerometerSensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Default Accelerometer for HB.
 * The type of accelerometer will be detected and listeners will register to receive Accelerometer events
 */
public class Accelerometer extends Sensor implements AccelerometerSensor {
    private static Sensor defaultSensor = null;

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
                            x = sensor.getAccelerometerX();
                            y = sensor.getAccelerometerY();
                            z = sensor.getAccelerometerZ();

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
                                x = sensor.getAccelerometerX();
                                y = sensor.getAccelerometerY();
                                z = sensor.getAccelerometerZ();

                                notifyListeners();
                            }
                        });

                    }

                } catch (Exception e) {
                }
            }

            if (defaultSensor == null && HB.isEnableSimulators()) {
                AccelerometerSimulator sensor = new AccelerometerSimulator();

                if (sensor != null) {
                    sensor.addListener(new SensorUpdateListener() {
                        @Override
                        public void sensorUpdated() {
                            x = sensor.getAccelerometerX();
                            y = sensor.getAccelerometerY();
                            z = sensor.getAccelerometerZ();

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
    public Accelerometer(){

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
    public double[] getAccelerometerData() {
        return new double[]{x, y, z};
    }

    @Override
    public double getAccelerometerX() {
        return x;
    }

    @Override
    public double getAccelerometerY() {
        return y;
    }

    @Override
    public double getAccelerometerZ() {
        return z;
    }
}
