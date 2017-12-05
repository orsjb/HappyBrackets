package net.happybrackets.device.sensors.sensor_types;

public interface MagnetometerListener{
    void sensorUpdated(double x, double y, double z);
}
