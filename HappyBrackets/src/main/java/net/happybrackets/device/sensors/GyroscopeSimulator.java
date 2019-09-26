package net.happybrackets.device.sensors;

import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.sensors.sensor_types.GyroscopeSensor;

public class GyroscopeSimulator extends Sensor implements GyroscopeSensor {
    final String CONTROL_PREFIX = "Gyro-";

    DynamicControl control_x;


    DynamicControl control_y;


    DynamicControl control_z;


    public GyroscopeSimulator(){
        reloadSimulation();
        storeSensor(this);
        setValidLoad(true);
    }



    public void reloadSimulation(){
        DynamicControl.DynamicControlListener listener = control -> {
            // we will ignore the control and send all three at once
            notifyListeners();
        };


        control_y = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "Pitch", 0.0, -1, 1).setControlScope(ControlScope.UNIQUE).addControlListener(listener).setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY).setSensorSimulationController();


        control_x = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "Roll", 0.0, -1, 1).setControlScope(ControlScope.UNIQUE).addControlListener(listener).setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY).setSensorSimulationController();


        control_z = new DynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "Yaw", 0.0, -1, 1).setControlScope(ControlScope.UNIQUE).addControlListener(listener).setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY).setSensorSimulationController();


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
    public float getGyroscopeX() {
        return (float)((double)control_x.getValue());
    }

    @Override
    public float getGyroscopeY() {
        return (float)((double)control_y.getValue());
    }

    @Override
    public float getGyroscopeZ() {
        return (float)((double)control_z.getValue());
    }

    @Override
    public float getPitch() {
        return getGyroscopeY();
    }

    @Override
    public float getRoll() {
        return getGyroscopeX();
    }

    @Override
    public float getYaw() {
        return getGyroscopeZ();
    }


}
