package net.happybrackets.device.sensors;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ollie on 1/06/2016.
 */
public abstract class Sensor {

    //TODO correct use of generics / type parameterisation with the listener set.

    protected final Set<SensorUpdateListener> listeners = new HashSet<>();

    /**
     * Returns the sensor name, typically the make/model of the hardware sensor that this class refers to.
     * @return a {@link String} representing the sensor's name.
     */
    public abstract String getSensorName();

    /**
     * Add a @{@link SensorUpdateListener} that will listen to this @{@link Sensor}.
     * @param listener the listener to add.
     */
    public void addListener(SensorUpdateListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove the given @{@link SensorUpdateListener}.
     * @param listener the listener to remove.
     */
    public void removeListener(SensorUpdateListener listener) {
        listeners.remove(listener);
    }

    /**
     * Clear all listeners listening to this @{@link Sensor}.
     */
    public void clearListeners() {
        listeners.clear();
    }
}
