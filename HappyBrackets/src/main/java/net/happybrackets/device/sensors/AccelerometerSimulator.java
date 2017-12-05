package net.happybrackets.device.sensors;

import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.sensors.sensor_types.AccelerometerListener;
import net.happybrackets.device.sensors.sensor_types.AccelerometerSensor;

import java.util.ArrayList;
import java.util.List;

public class AccelerometerSimulator extends Sensor implements AccelerometerSensor {
    final String CONTROL_PREFIX = "Accel-";

    DynamicControl control_x_slider;
    DynamicControl control_x_text;

    DynamicControl control_y_slider;
    DynamicControl control_y_text;

    DynamicControl control_z_slider;
    DynamicControl control_z_text;

    public AccelerometerSimulator(){
        DynamicControl.DynamicControlListener listener = new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                // we will ignore the control and send all three at once
                for(SensorUpdateListener listener : listeners) {
                    listener.sensorUpdated();
                }
            }
        };

        control_x_slider = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "x", 0.0, -1, 1).setControlScope(ControlScope.CLASS).addControlListener(listener);
        control_x_text = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "x", 0.0).setControlScope(ControlScope.CLASS).addControlListener(listener);

        control_y_slider = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "y", 0.0, -1, 1).setControlScope(ControlScope.CLASS).addControlListener(listener);
        control_y_text = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "y", 0.0).setControlScope(ControlScope.CLASS).addControlListener(listener);

        control_z_slider = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "z", 0.0, -1, 1).setControlScope(ControlScope.CLASS).addControlListener(listener);
        control_z_text = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "z", 0.0).setControlScope(ControlScope.CLASS).addControlListener(listener);

    }



    @Override
    public String getSensorName() {
        return "AccelerometerSimulator";
    }

    @Override
    public double[] getAccelerometerData() {
        return new double[]{getAccelerometerY(), getAccelerometerX(), getAccelerometerZ()};
    }

    @Override
    public double getAccelerometerX() {
        return (float)control_x_text.getValue();
    }

    @Override
    public double getAccelerometerY() {
        return (float)control_y_text.getValue();
    }

    @Override
    public double getAccelerometerZ() {
        return (float)control_z_text.getValue();
    }
}
