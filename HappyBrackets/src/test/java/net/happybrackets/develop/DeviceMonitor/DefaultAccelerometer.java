package net.happybrackets.develop.DeviceMonitor;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.Sensor;
import net.happybrackets.device.sensors.SensorNotFoundException;

import java.lang.invoke.MethodHandles;

public class DefaultAccelerometer implements HBAction{

    final String ACCEL_PREFIX = "Accel-";
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

        // display text and sliders
        DynamicControl control_x = hb.createDynamicControl(this, ControlType.FLOAT, ACCEL_PREFIX + "x", 0.0).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, ACCEL_PREFIX + "x", 0.0, -1, 1).setControlScope(ControlScope.SKETCH);

        DynamicControl control_y = hb.createDynamicControl(this, ControlType.FLOAT, ACCEL_PREFIX + "y", 0.0).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, ACCEL_PREFIX + "y", 0.0, -1, 1).setControlScope(ControlScope.SKETCH);

        DynamicControl control_z = hb.createDynamicControl(this, ControlType.FLOAT, ACCEL_PREFIX + "z", 0.0).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, ACCEL_PREFIX + "z", 0.0, -1, 1).setControlScope(ControlScope.SKETCH);



        //hb.setEnableSimulators(false);
        try {
            hb.findSensor(Accelerometer.class).addValueChangedListener(sensor -> {
                Accelerometer accelerometer = (Accelerometer) sensor;
                // Get the data from Z.
                double zAxis = accelerometer.getAccelerometerZ();
                double yAxis = accelerometer.getAccelerometerY();
                double xAxis = accelerometer.getAccelerometerX();


                control_x.setValue((float) xAxis);
                control_y.setValue((float) yAxis);
                control_z.setValue((float) zAxis);

                float scaled_x = Sensor.scaleValue(-1, 1, 200, 1000, xAxis);
                System.out.println("" + scaled_x);

            });
        } catch (SensorNotFoundException e) {
            hb.setStatus(e.getMessage());
        }


    }
}
