package net.happybrackets.core.control;

import de.sciss.net.OSCMessage;

import java.util.*;

/**
 * Control Map is a singleton that will store dynamic controls and allow us to access them via their unique map key string
 */
public class ControlMap {

    /**
     * Create an interface used for sending OSC Messages to various listeners
     */
    public interface dynamicControlAdvertiseListener{
        void dynamicControlEvent(OSCMessage msg);
    }

    private List<dynamicControlAdvertiseListener> controlListenerList = new ArrayList();

    // create a group of listeners for global controls over network
    private List<dynamicControlAdvertiseListener> globalControlListenerList = new ArrayList();

    // We will enforce singleton by instatiating it once
    private static ControlMap singletonInstance = null;

    // create a map based on Device name and instance number
    private LinkedHashMap<String, DynamicControl> dynamicControls = new LinkedHashMap<>();

    // Devices are mapped based on name
    private LinkedHashMap<String, List<DynamicControl>> controlScopedDevices = new LinkedHashMap<>();



    public void addDynamicControlAdvertiseListener(dynamicControlAdvertiseListener listener){
        synchronized (controlListenerList)
        {
            controlListenerList.add(listener);
        }
    }

    public void addGlobalDynamicControlAdvertiseListener(dynamicControlAdvertiseListener listener){
        synchronized (globalControlListenerList)
        {
            globalControlListenerList.add(listener);
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
            dynamicControls.put(control.getControlMapKey(), control);
            // We are going to add ourseleves as a listener to the update value so we can send any updates to controller
            control.addControlListener(new DynamicControl.DynamicControlListener() {
                @Override
                public void update(DynamicControl control) {
                    if (controlListenerList.size() > 0)
                    {
                        OSCMessage msg = control.buildUpdateMessage();
                        sendDynamicControlMessage(msg);

                        // We need to update all the identical controls if this control is not sk
                        if (control.getControlScope() != ControlScope.SKETCH) {
                            List<DynamicControl> name_list = getControlsByName(control.getControlName());
                            for (DynamicControl mimic_control : name_list) {
                                if (mimic_control != control){ // Make sure it is not us
                                    mimic_control.updateControl(control);
                                }


                            }
                            // we need to see if this was a global scope

                            if (control.getControlScope() == ControlScope.GLOBAL)
                            {
                                // needs to be broadcast
                                sendGlobalDynamicControlMessage (msg);
                            }
                        }
                    }
                }
            });

            String name = control.getControlName();
            getControlsByName(name).add(control);

        }
        if (controlListenerList.size() > 0)
        {
            OSCMessage msg = control.buildCreateMessage();
            sendDynamicControlMessage(msg);
        }
    }

    /**
     * Send OSC Message across to global senders - this message is for Global Scope controls
     * @param msg the Control Message to send
     */
    private synchronized void sendGlobalDynamicControlMessage(OSCMessage msg) {

        for (dynamicControlAdvertiseListener listener : globalControlListenerList) {
            listener.dynamicControlEvent(msg);
        }
    }


    /**
     * Get the Control list by name.
     * If no list exists, it will create a list and add it to controlScopedDevices
     * @param name the name used as a search key
     * @return the List of DynamicControls that have that name
     */
    public List<DynamicControl> getControlsByName(String name)
    {
        List<DynamicControl> name_list = controlScopedDevices.get(name);
        if (name_list == null)
        {
            name_list = new ArrayList<>();
            controlScopedDevices.put(name, name_list);
        }

        return name_list;
    }
    /**
     * Get the Dynamic Control based on HashCode
     * @param map_key the String we are using as the key
     * @return the Dynamic control associated, otherwise null if does not exist
     */
    public DynamicControl getControl(String map_key)
    {
        synchronized (dynamicControls)
        {
            return  dynamicControls.getOrDefault(map_key, null);
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
            dynamicControls.remove(control.getControlMapKey());

            // now remove from named Objects
            String name = control.getControlName();

            List<DynamicControl> name_list = getControlsByName(name);
            name_list.remove(control);
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
            dynamicControls.clear();

            // Clear all Control scope Objects
            controlScopedDevices.forEach((name, named_list)->{
                named_list.clear();
            });

            // No need to clear the actual lists themselves
        }

    }

    public LinkedHashMap<String, DynamicControl> getDynamicControls(){
        return dynamicControls;
    }

    /**
     * get all Dynamic Controls
     */
    public List<DynamicControl> GetSortedControls()
    {
        List<DynamicControl> sorted_list = new ArrayList<DynamicControl>();

        dynamicControls.forEach((key, value) -> {
            sorted_list.add(value);
        });

        return sorted_list;
    }

}
