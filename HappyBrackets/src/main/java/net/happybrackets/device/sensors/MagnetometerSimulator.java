package net.happybrackets.device.sensors;

import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.sensors.sensor_types.GyroscopeSensor;
import net.happybrackets.device.sensors.sensor_types.MagnetometerSensor;

public class MagnetometerSimulator extends Sensor implements MagnetometerSensor {
    final String CONTROL_PREFIX = "Mag-";

    DynamicControl control_x_slider;
    DynamicControl control_x_text;

    DynamicControl control_y_slider;
    DynamicControl control_y_text;

    DynamicControl control_z_slider;
    DynamicControl control_z_text;

    public MagnetometerSimulator(){
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
        setValidLoad(true);
    }



    @Override
    public String getSensorName() {
        return "MagnetometerrSimulator";
    }


    @Override
    public float getMagnetometerX() {
        return (float)control_x_text.getValue();
    }

    @Override
    public float getMagnetometerY() {
        return (float)control_y_text.getValue();
    }

    @Override
    public float getMagnetometerZ() {
        return (float)control_z_text.getValue();
    }


    @Override
    public double[] getMagnetometerData() {
        return new double[]{getMagnetometerY(), getMagnetometerX(), getMagnetometerZ()};
    }

}
