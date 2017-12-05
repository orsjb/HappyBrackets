package net.happybrackets.develop.DeviceMonitor;

import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.sensor_types.AccelerometerListener;

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

        Accelerometer accel = (Accelerometer)hb.getSensor(Accelerometer.class) ;
        if (accel != null) {
            accel.addAccelerometerListener(new AccelerometerListener() {
                @Override
                public void sensorUpdated(double x, double y, double z) {
                    System.out.println("X " + x + ", Y " + y + ", Z " + z);
                }
            });
        }
    }
}
