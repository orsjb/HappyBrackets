package net.happybrackets.device.sensors;

import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.sensors.sensor_types.MagnetometerSensor;

public class MagnetometerSimulator extends Sensor implements MagnetometerSensor {
    final String CONTROL_PREFIX = "Mag-";

    DynamicControl control_x;


    DynamicControl control_y;


    DynamicControl control_z;


    public MagnetometerSimulator(){
        reloadSimulation();
        storeSensor(this);
        setValidLoad(true);
    }

    public void reloadSimulation(){
        DynamicControl.DynamicControlListener listener = new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                // we will ignore the control and send all three at once
                notifyListeners();
            }
        };

        control_x = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "x", 0.0, -1, 1).setControlScope(ControlScope.UNIQUE).addControlListener(listener).setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);


        control_y = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "y", 0.0, -1, 1).setControlScope(ControlScope.UNIQUE).addControlListener(listener).setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);


        control_z = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "z", 0.0, -1, 1).setControlScope(ControlScope.UNIQUE).addControlListener(listener).setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);


    }

    @Override
    public String getSensorName() {
        return "MagnetometerrSimulator";
    }


    @Override
    public float getMagnetometerX() {
        return (float)((double)control_x.getValue());
    }

    @Override
    public float getMagnetometerY() {
        return (float)((double)control_y.getValue());
    }

    @Override
    public float getMagnetometerZ() {
        return (float)((double)control_z.getValue());
    }


    @Override
    public double[] getMagnetometerData() {
        return new double[]{getMagnetometerY(), getMagnetometerX(), getMagnetometerZ()};
    }

}
