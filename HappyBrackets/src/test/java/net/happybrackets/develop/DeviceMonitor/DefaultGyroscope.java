package net.happybrackets.develop.DeviceMonitor;

import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.Gyroscope;
import net.happybrackets.device.sensors.SensorUpdateListener;

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

        Gyroscope sensor = (Gyroscope)hb.getSensor(Gyroscope.class) ;
        if (sensor != null) {
            sensor.setRounding(3);
            sensor.addListener(new SensorUpdateListener() {
                @Override
                public void sensorUpdated() {
                    System.out.println(sensor.getGyroscopeX());
                    System.out.println(sensor.getGyroscopeY());
                    System.out.println(sensor.getGyroscopeZ());
                }
            });
        }
    }
}
