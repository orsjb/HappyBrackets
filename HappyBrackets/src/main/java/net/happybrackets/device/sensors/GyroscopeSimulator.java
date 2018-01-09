package net.happybrackets.device.sensors;

import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.sensors.sensor_types.AccelerometerSensor;
import net.happybrackets.device.sensors.sensor_types.GyroscopeSensor;

public class GyroscopeSimulator extends Sensor implements GyroscopeSensor {
    final String CONTROL_PREFIX = "Gyro-";

    DynamicControl control_x_slider;
    DynamicControl control_x_text;

    DynamicControl control_y_slider;
    DynamicControl control_y_text;

    DynamicControl control_z_slider;
    DynamicControl control_z_text;

    public GyroscopeSimulator(){
        DynamicControl.DynamicControlListener listener = new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                // we will ignore the control and send all three at once
                notifyListeners();
            }
        };

        control_x_slider = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "x", 0.0, -1, 1).setControlScope(ControlScope.SKETCH);
        control_x_text = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "x", 0.0).setControlScope(ControlScope.SKETCH).addControlListener(listener);

        control_y_slider = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "y", 0.0, -1, 1).setControlScope(ControlScope.SKETCH);
        control_y_text = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "y", 0.0).setControlScope(ControlScope.SKETCH).addControlListener(listener);

        control_z_slider = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "z", 0.0, -1, 1).setControlScope(ControlScope.SKETCH);
        control_z_text = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "z", 0.0).setControlScope(ControlScope.SKETCH).addControlListener(listener);

        storeSensor(this);
    }



    @Override
    public String getSensorName() {
        return "GyroscopeSimulator";
    }

    @Override
    public double[] getGyroscopeData() {
        return new double[]{getGyroscopeY(), getGyroscopeX(), getGyroscopeZ()};
    }

    @Override
    public double getGyroscopeX() {
        return (float)control_x_text.getValue();
    }

    @Override
    public double getGyroscopeY() {
        return (float)control_y_text.getValue();
    }

    @Override
    public double getGyroscopeZ() {
        return (float)control_z_text.getValue();
    }



}
