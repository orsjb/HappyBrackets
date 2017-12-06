package net.happybrackets.develop.DeviceMonitor;

import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Gyroscope;
import net.happybrackets.device.sensors.Magnetometer;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.lang.invoke.MethodHandles;

public class DefaultMagnetometer implements HBAction{

    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void action(HB hb) {

        Magnetometer sensor = (Magnetometer)hb.getSensor(Magnetometer.class) ;
        if (sensor != null) {
            sensor.addListener(new SensorUpdateListener() {
                @Override
                public void sensorUpdated() {
                    System.out.println(sensor.getMagnetometerX());
                    System.out.println(sensor.getMagnetometerY());
                    System.out.println(sensor.getMagnetometerZ());
                }
            });
        }
    }
}
