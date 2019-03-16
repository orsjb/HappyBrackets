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
     * Add a validLoad flag. Every sensor will need to set it when it determines that the load is valid
     */
    protected boolean validLoad = false;

    protected static boolean simulatedOnly = false;


    /**
     * A {@link Hashtable} to store sensors.
     */
    private static Hashtable<Class<? extends Sensor>, Sensor> loadedSensors = new Hashtable<>();

    private final Set<SensorUpdateListener> listeners = new HashSet<>();
    private final Set<SensorValueChangedListener> valueChangedListeners = new HashSet<>();

    private final Set<SensorUpdateListener> nonResetableListeners = new HashSet<>();
    private final Set<SensorValueChangedListener> nonResetablevValueChangedListeners = new HashSet<>();

    /**
     * Flag to indicate this is just a simulator
     * @return true if only a simulator
     */
    public static boolean isSimulatedOnly() {
        return simulatedOnly;
    }

    /**
     * Return a scaled value for a sensor based on known maximum and minimum values
     * A continuous function that satisfies this the following
     * sensor_value(sensor_min) = scaled_min
     * sensor_value(sensor_max) = scaled_max
     * @param sensor_min the minimum value sensor would normally return
     * @param sensor_max the maximum value our sensor would normally return
     * @param scaled_min the value we want returned as our minimum for sensor minimum value
     * @param scaled_max the value we want returned as our maximum for our sensor maximum value
     * @param sensor_value the actual value of the sensor
     * @return our scaled value
     */
    public static float scaleValue (double sensor_min, double sensor_max, double scaled_min, double scaled_max, double sensor_value){
        double ret = ((scaled_max - scaled_min) * (sensor_value - sensor_min)) / (sensor_max - sensor_min) + scaled_min;

        return  (float)ret;
    }
    /**
     * Set from inside IDE to indicate we are just simulating a sensor
     * @param simulated set to true if we are simulating
     */
    public static void setSimulatedOnly(boolean simulated) {
        simulatedOnly = simulated;
    }
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
        if (isSimulatedOnly()){
            listener.sensorUpdated();
        }
    }

    /**
     * Add a @{@link SensorUpdateListener} that will listen to this @{@link Sensor}.
     * Will be deprecated in 3.0
     * These listeners are not removed when device is reset
     * @param listener the listener to add.
     * deprecated use (@link addValueChangedListener) instead
     */
    public void addNonResettableListener(SensorUpdateListener listener) {
        nonResetableListeners.add(listener);
        if (isSimulatedOnly()){
            listener.sensorUpdated();
        }
    }


    /**
     * returns true if the sensor was loaded correctly.
     * @return true if sensor was loaded correctly
     */
    public boolean isValidLoadedSensor(){
        // This must be set inside subclasses
        return validLoad;
    }

    /**
     * This needs to be set inside sensor to indicate it had a valid load
     * @param valid set to true if loaded
     */
    protected  void setValidLoad(boolean valid) {
        validLoad = valid;
    }

    /**
     * Add a @{@link SensorValueChangedListener} that will listen to this @{@link Sensor}.
     * @param listener the listener to add.
     */
    public void addValueChangedListener(SensorValueChangedListener listener) {
        valueChangedListeners.add(listener);
        if (isSimulatedOnly()) {
            listener.sensorUpdated(this);
        }
    }
    /**
     * Add a @{@link SensorValueChangedListener} that will listen to this @{@link Sensor}.
     * These listeners are NOT removed when device is reset
     * @param listener the listener to add.
     */
    public void addNonResettableValueChangedListener(SensorValueChangedListener listener) {
        nonResetablevValueChangedListeners.add(listener);
        if (isSimulatedOnly()) {
            listener.sensorUpdated(this);
        }
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
                ex.printStackTrace();
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
                ex.printStackTrace();
            }
        }

        // Now do nonresettable
        for(SensorUpdateListener listener : nonResetableListeners) {
            try {
                listener.sensorUpdated();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

        }

        for (SensorValueChangedListener listener: nonResetablevValueChangedListeners)
        {
            try
            {
                listener.sensorUpdated(this);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }


    }

    /**
     * Override this function on any sensors that have resolution or the likes set
     */
    public void resetToDefault(){
    }

    /**
     * Override this on any classes you need to do something as a simulated sensor
     */
    public void reloadSimulation(){
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
    @SuppressWarnings("rawtypes")
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
