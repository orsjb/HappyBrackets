package net.happybrackets.develop.DeviceMonitor;

import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Magnetometer;
import net.happybrackets.device.sensors.SensorNotFoundException;

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

        try {
            Magnetometer sensor = (Magnetometer) hb.findSensor(Magnetometer.class);
            if (sensor != null) {
                sensor.setRounding(3);
                sensor.addValueChangedListener(sensor1 -> {
                    System.out.println(sensor.getMagnetometerX());
                    System.out.println(sensor.getMagnetometerY());
                    System.out.println(sensor.getMagnetometerZ());

                });

            }
        }
        catch (SensorNotFoundException e) {
            e.printStackTrace();

        }
    }
}
