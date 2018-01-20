package net.happybrackets.develop.DeviceMonitor;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.Sensor;
import net.happybrackets.device.sensors.SensorUpdateListener;
import net.happybrackets.device.sensors.SensorValueChangedListener;

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

        hb.reset();

        try {
            hb.createSensor(Accelerometer.class).addValueChangedListener(sensor -> {
                Accelerometer accelerometer = (Accelerometer) sensor;
                float x_val = accelerometer.getAccelerometerX();
                float y_val = accelerometer.getAccelerometerY();
                float z_val = accelerometer.getAccelerometerZ();

            });
        } catch (ClassNotFoundException e) {
            hb.setStatus("Accelerometer Fail");
        }

        hb.createDynamicControl(this, ControlType.FLOAT, "Enter Control Name",
                0, 0, 0)
                .addControlListener(control -> {
                    float control_val = (float) control.getValue();
                });
        
    }
}
