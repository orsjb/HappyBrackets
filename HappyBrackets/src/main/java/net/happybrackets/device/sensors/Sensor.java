package net.happybrackets.device.sensors;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ollie on 1/06/2016.
 */
public abstract class Sensor {

    //TODO correct use of generics / type parameterisation with the lister set.

    public final Set<SensorListener> listeners = new HashSet<>();

    public abstract String getSensorName();

    public void addListener(SensorListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SensorListener listener) {
        listeners.remove(listener);
    }

    public void clearListeners() {
        listeners.clear();
    }
}
