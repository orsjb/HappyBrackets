package net.happybrackets.device.sensors;

import net.beadsproject.beads.data.DataBead;

/**
 * Created by ollie on 1/06/2016.
 */
public interface SensorListener {
    DataBead sensor = null;
    DataBead data = null;

    static DataBead getData() {
        return data;
    }

    static DataBead getSensor() {
        return sensor;
    }

    static void setData(DataBead dataIn) {
    }

    static void setSensor(DataBead sensorIn) {
    }
}
