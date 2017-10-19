package net.happybrackets.core.control;

import java.util.Hashtable;
import java.util.Map;

/**
 * Control Map is a singleton that will store dynamic controls and allow us to access them via their hash code
 */
public class ControlMap {

    static private Map<Integer, DynamicControl> dynamicControls = new Hashtable<Integer, DynamicControl>();


    private ControlMap(){}

    /**
     * Add a control to our map
     * @param control
     */
    static public void addControl(DynamicControl control)
    {
        synchronized (dynamicControls)
        {
            dynamicControls.put(control.hashCode(), control);
        }
    }
}
