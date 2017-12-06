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
import java.util.Set;

/**
 * Abstract base class for types of things that are sensors. Enables adding and removing of sensor listeners, and getting sensor name. See a real Sensor implementation such as {@link MiniMU} for more details.
 *
 * Created by ollie on 1/06/2016.
 */
public abstract class Sensor {

    private final Set<SensorUpdateListener> listeners = new HashSet<>();

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

    /**
     * Notify all listeners that the sensor has updated
     */
    protected void notifyListeners()
    {
        for(SensorUpdateListener listener : listeners) {
            listener.sensorUpdated();
        }
    }
}
