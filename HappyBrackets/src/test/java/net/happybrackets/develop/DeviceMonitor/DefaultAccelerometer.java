package net.happybrackets.develop.DeviceMonitor;

import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.lang.invoke.MethodHandles;

public class DefaultAccelerometer implements HBAction{

    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void action(HB hb) {

        Accelerometer sensor = (Accelerometer)hb.getSensor(Accelerometer.class) ;
        if (sensor != null) {
            sensor.setRounding(3);
            sensor.addListener(new SensorUpdateListener() {
                @Override
                public void sensorUpdated() {
                    System.out.println(sensor.getAccelerometerX());
                    System.out.println(sensor.getAccelerometerY());
                    System.out.println(sensor.getAccelerometerZ());
                }
            });
        }
    }
}
