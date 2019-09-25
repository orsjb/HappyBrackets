package net.happybrackets.device.sensors;

import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.sensors.sensor_types.AccelerometerSensor;

public class AccelerometerSimulator extends Sensor implements AccelerometerSensor {
    final String CONTROL_PREFIX = "Accel-";

    DynamicControl control_x;


    DynamicControl control_y;


    DynamicControl control_z;
    

    public AccelerometerSimulator(){

        reloadSimulation();

        storeSensor(this);
        setValidLoad(true);
    }



    public void reloadSimulation(){
        DynamicControl.DynamicControlListener listener = control -> {
            // we will ignore the control and send all three at once
            notifyListeners();
        };

        control_x = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "x", 0.0, -1, 1).setControlScope(ControlScope.UNIQUE).addControlListener(listener).setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);

        //control_x_text = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "x", 0.0).setControlScope(ControlScope.SKETCH).addControlListener(listener);

        control_y = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "y", 0.0, -1, 1).setControlScope(ControlScope.UNIQUE).addControlListener(listener).setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);;
        //control_y_text = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "y", 0.0).setControlScope(ControlScope.SKETCH).addControlListener(listener);

        control_z = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "z", 0.0, -1, 1).setControlScope(ControlScope.UNIQUE).addControlListener(listener).setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);
        //control_z_text = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "z", 0.0).setControlScope(ControlScope.SKETCH).addControlListener(listener);
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
    public float getAccelerometerX() {
        return (float) ((double) control_x.getValue());
    }

    @Override
    public float getAccelerometerY() {
        return (float) ((double)control_y.getValue());
    }

    @Override
    public float getAccelerometerZ() {
        return (float) ((double)control_z.getValue());
    }
}
