package net.happybrackets.develop.DeviceMonitor;

import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Gyroscope;
import net.happybrackets.device.sensors.SensorNotFoundException;

import java.lang.invoke.MethodHandles;

public class DefaultGyroscope implements HBAction{

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
            Gyroscope sensor = (Gyroscope)hb.findSensor(Gyroscope.class);
            sensor.setRounding(3);
            sensor.addListener(() -> {
                System.out.println(sensor.getPitch());
                System.out.println(sensor.getRoll());
                System.out.println(sensor.getYaw());
            });
        } catch (SensorNotFoundException e) {
            e.printStackTrace();
        }
    }
}