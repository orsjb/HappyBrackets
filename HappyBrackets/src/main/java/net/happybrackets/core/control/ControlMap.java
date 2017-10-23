package net.happybrackets.core.control;

import java.util.Hashtable;
import java.util.Map;

/**
 * Control Map is a singleton that will store dynamic controls and allow us to access them via their hash code
 */
public class ControlMap {

    // We will enforce singleton by instatiating it once
    private static ControlMap singletonInstance = null;

    private Map<Integer, DynamicControl> dynamicControls = new Hashtable<Integer, DynamicControl>();


    private ControlMap(){}

    /**
     * Get the Control Map
     * @return the singletonInstance
     */
    public static synchronized ControlMap  getInstance()
    {
        if (singletonInstance == null)
        {
            singletonInstance = new ControlMap();
        }
        return  singletonInstance;
    }
    /**
     * Add a control to our map
     * @param control
     */
    public void addControl(DynamicControl control)
    {
        synchronized (dynamicControls)
        {
            dynamicControls.put(control.hashCode(), control);
        }
    }

    /**
     * Get the Dynamic Control based on HashCode
     * @param hash_code the hash_code we are using as the key
     * @return the Dynamic control associated, otherwise null if does not exist
     */
    public DynamicControl getControl(int hash_code)
    {
        synchronized (dynamicControls)
        {
            return  dynamicControls.getOrDefault(hash_code, null);
        }
    }


    /**
     * Get all our controls
     * @return
     */
    public Map<Integer, DynamicControl> getAllControlls()
    {
        return dynamicControls;
    }
}
