/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.device.sensors;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * Abstract base class for types of things that are sensors. Enables adding and removing of sensor listeners, and getting sensor name. See a real Sensor implementation such as {@link MiniMU} for more details.
 *
 * Created by ollie on 1/06/2016.
 */
public abstract class Sensor {

    /**
     * A {@link Hashtable} to store sensors.
     */
    private static Hashtable<Class<? extends Sensor>, Sensor> loadedSensors = new Hashtable<>();

    private final Set<SensorUpdateListener> listeners = new HashSet<>();
    private final Set<SensorValueChangedListener> valueChangedListeners = new HashSet<>();
    /**
     * Returns the sensor name, typically the make/model of the hardware sensor that this class refers to.
     * @return a {@link String} representing the sensor's name.
     */
    public abstract String getSensorName();

    /**
     * Add a @{@link SensorUpdateListener} that will listen to this @{@link Sensor}.
     * Will be deprecated in 3.0
     * @param listener the listener to add.
     * deprecated use (@link addValueChangedListener) instead
     */
    public void addListener(SensorUpdateListener listener) {
        listeners.add(listener);
    }

    /**
     * Add a @{@link SensorValueChangedListener} that will listen to this @{@link Sensor}.
     * @param listener the listener to add.
     */
    public void addValueChangedListener(SensorValueChangedListener listener) {
        valueChangedListeners.add(listener);
    }


    /**
     * Remove the given @{@link SensorUpdateListener}.
     * @param listener the listener to remove.
     */
    public void removeListener(SensorUpdateListener listener) {
        listeners.remove(listener);
    }

    /**
     * Remove the given @{@link SensorValueChangedListener}.
     * @param listener the listener to remove.
     */
    public void removeListener(SensorValueChangedListener listener) {
        valueChangedListeners.remove(listener);
    }

    /**
     * Clear all listeners listening to this @{@link Sensor}.
     */
    public void clearListeners() {
        listeners.clear();
        valueChangedListeners.clear();
    }

    /**
     * Notify all listeners that the sensor has updated
     */
    protected void notifyListeners()
    {
        for(SensorUpdateListener listener : listeners) {
            try {
                listener.sensorUpdated();
            }
            catch (Exception ex)
            {
                System.out.println("Exception in notifyListeners " +  ex.getMessage());
            }

        }

        for (SensorValueChangedListener listener: valueChangedListeners)
        {
            try
            {
                listener.sensorUpdated(this);
            }
            catch (Exception ex)
            {
                System.out.println("Exception in notifyListeners " +  ex.getMessage());
            }
        }
    }

    /**
     * Override this function on any sensors that have resolution or the likes set
     */
    public void resetToDefault(){
    }

    /**
     * Round the double value to the number of decimal places defined by rounding
     * If rounding is less than zero, we will leave value as is
     * @param val the value to round
     * @param rounding the number of decimal places to round to
     * @return the new rounded value
     */
    protected double roundValue(double val, int rounding){
        double ret = val;
        if (rounding >= 0){
            double multiplier = Math.pow(10, rounding);
            ret = val * multiplier;
            ret = Math.round(ret);
            ret =  ret / multiplier;
        }

        return ret;
    }

    /**
     * Return a sensor if it has been constructed.
     * @param sensorClass the class we are looking for
     * @return The class if it has been stored, otherwise null
     */
    public static Sensor getSensor(Class sensorClass) {
        return loadedSensors.get(sensorClass);
    }

    /**
     * Store the sensor into our loadedSensors
     * @param sensor the sensor we are loading
     */
    protected static void storeSensor(Sensor sensor){
        loadedSensors.put(sensor.getClass(), sensor);
    }
}
