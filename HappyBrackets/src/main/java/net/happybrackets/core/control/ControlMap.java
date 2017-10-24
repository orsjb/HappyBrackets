package net.happybrackets.core.control;

import de.sciss.net.OSCMessage;

import java.util.*;

/**
 * Control Map is a singleton that will store dynamic controls and allow us to access them via their hash code
 */
public class ControlMap {

    /**
     * Create an interface used for sending OSC Messages to various listeners
     */
    public interface dynamicControlAdvertiseListener{
        void dynamicControlEvent(OSCMessage msg);
    }

    private List<dynamicControlAdvertiseListener> controlListenerList = new ArrayList();


    public void addDynamicControlAdvertiseListener(dynamicControlAdvertiseListener listener){
        synchronized (controlListenerList)
        {
            controlListenerList.add(listener);
        }
    }

    /**
     * Sennd OSC Message to controllers. Using an event based interface allows us to reduce coupling
     * @param msg
     */
    private synchronized void sendDynamicControlMessage(OSCMessage msg)
    {
        for (dynamicControlAdvertiseListener listener : controlListenerList) {
            listener.dynamicControlEvent(msg);
        }
    }

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
     * Add a control to our map. Generate a control added event
     * @param control
     */
    public void addControl(DynamicControl control)
    {
        synchronized (dynamicControls)
        {
            dynamicControls.put(control.getControlHashCode(), control);
            // We are going to add ourseleves as a listener to the update value so we can send any updates to controller
            control.addControlListener(new DynamicControl.DynamicControlListener() {
                @Override
                public void update(DynamicControl control, Object val) {
                    if (controlListenerList.size() > 0)
                    {
                        OSCMessage msg = control.buildUpdateMessage();
                        sendDynamicControlMessage(msg);
                    }
                }
            });
        }
        if (controlListenerList.size() > 0)
        {
            OSCMessage msg = control.buildCreateMessage();
            sendDynamicControlMessage(msg);
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
     * Update the control scope in the maps. Removes from old scope map and adds to new
     * Still needs to be implemented
     * @param control the control being updated
     * @param old_scope the scope this control used to have
     */
    public void updateControlScope(DynamicControl control, ControlScope old_scope) {
        synchronized (dynamicControls) {

        }
    }

    /**
     * Remove all listeners and reference to Dynamic Control
     * @param control the control to remove
     */
    public void removeControl(DynamicControl control){
        synchronized (dynamicControls)
        {
            control.eraseListeners();
            OSCMessage msg = control.buildRemoveMessage();
            sendDynamicControlMessage(msg);
            dynamicControls.remove(control.getControlHashCode());
        }
    }

    /**
     * Erase all the listeners
     */
    public void clearAllListeners()
    {
        synchronized (dynamicControls)
        {
            Collection<DynamicControl> controls = dynamicControls.values();
            for (DynamicControl control : controls) {
                control.eraseListeners();

                OSCMessage msg = control.buildRemoveMessage();
                sendDynamicControlMessage(msg);

            }
        }
    }

    /**
     * Send all Create messages to controller
     */
    public void sendAllControlsToController()
    {
        if (controlListenerList.size() > 0) {
            synchronized (dynamicControls) {
                Collection<DynamicControl> controls = dynamicControls.values();
                for (DynamicControl control : controls) {
                    OSCMessage msg = control.buildCreateMessage();
                    sendDynamicControlMessage(msg);
                }
            }
        }
    }

}
