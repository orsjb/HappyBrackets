package net.happybrackets.device.sensors;

import net.happybrackets.device.sensors.sensor_types.AccelerometerListener;
import net.happybrackets.device.sensors.sensor_types.GyroscopeListener;
import net.happybrackets.device.sensors.sensor_types.MagnetometerListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Default Accelerometer for HB.
 * The type of accelerometer will be detected and listeners will register to receive Accelerometer events
 */
public class Magnetometer extends Sensor {
    private static Sensor defaultSensor = null;


    static List<MagnetometerListener> listeners = new ArrayList<>();
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
                            synchronized (listeners) {
                                for (MagnetometerListener listener : listeners) {
                                    listener.sensorUpdated(sensor.getMagnetometerX(), sensor.getMagnetometerY(), sensor.getMagnetometerZ());
                                }
                            }
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
                                synchronized (listeners) {
                                    for (MagnetometerListener listener : listeners) {
                                        listener.sensorUpdated(sensor.getMagnetometerX(), sensor.getMagnetometerY(), sensor.getMagnetometerZ());
                                    }
                                }
                            }
                        });

                    }

                } catch (Exception e) {
                }
            }

            if (defaultSensor == null) {
                MagnetometerSimulator sensor = new MagnetometerSimulator();

                if (sensor != null) {
                    sensor.addListener(new SensorUpdateListener() {
                        @Override
                        public void sensorUpdated() {
                            synchronized (listeners) {
                                for (MagnetometerListener listener : listeners) {
                                    listener.sensorUpdated(sensor.getMagnetometerX(), sensor.getMagnetometerY(), sensor.getMagnetometerZ());
                                }                           }
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


    /**
     * Adds a listener for accelerometer
     * @param listener the listener
     */
    public void addAccelerometerListener(MagnetometerListener listener){
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public String getSensorName() {
        return "Accelerometer";
    }

    /**
     * Erases all the listeners
     */
    public void clearListeners(){
        synchronized (listeners) {
            listeners.clear();
        }
    }
}
