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

    /**
     * Create an Interface where we can look for created Dynamic Controls
     */
    public interface dynamicControlCreatedListener{
        void controlCreated(DynamicControl control);
    }

    /**
     * Create an Interface where we can look for created Dynamic Controls
     */
    public interface dynamicControlRemovedListener{
        void controlRemoved(DynamicControl control);
    }

    private List<dynamicControlCreatedListener> controlCreatedListenerList =  new ArrayList<>();

    private List<dynamicControlAdvertiseListener> controlListenerList = new ArrayList<>();

    private List<dynamicControlRemovedListener> controlRemovedListenerList = new ArrayList<>();


    // create a group of listeners for global controls over network
    private List<dynamicControlAdvertiseListener> globalControlListenerList = new ArrayList<>();

    // We will enforce singleton by instatiating it once
    private static ControlMap singletonInstance = null;

    // create a map based on Device name and instance number
    private LinkedHashMap<String, DynamicControl> dynamicControls = new  LinkedHashMap<>();

    // Devices are mapped based on name. We find controls based by name
    private LinkedHashMap<String, List<DynamicControl>> controlScopedDevices = new LinkedHashMap<>();


    // Define array of Dynamic controls used for simulation. These do not get removed on reset
    private List<DynamicControl> sensorSimulation = new ArrayList<>();

    // When we are in the IDE, we do not want to pass messages about the plugin
    // Just let the device send them
    static boolean disableControlMimic = false;

    // create locks for collections
    private final Object controlCreatedListenerListLock = new Object();
    private final Object controlListenerListLock = new Object();
    private final Object controlRemovedListenerListLock = new Object();
    private final Object globalControlListenerListLock = new Object();
    private final Object dynamicControlsLock = new Object();
    private final Object controlScopedDevicesLock = new Object();

    /**
     * We will disable sending messages to other controls with same name and scope
     * when we are in plugin
     * @param disable set true for plugin
     */
    public static void disableControlMimic(boolean disable){
        disableControlMimic = disable;
    }

    public void addDynamicControlCreatedListener(dynamicControlCreatedListener listener){
        //synchronized (controlCreatedListenerListLock) {
            controlCreatedListenerList.add(listener);
        //}
    }

    public void addDynamicControlAdvertiseListener(dynamicControlAdvertiseListener listener){
        //synchronized (controlListenerListLock)
        {
            controlListenerList.add(listener);
        }
    }

    public void addDynamicControlRemovedListener(dynamicControlRemovedListener listener){
        //synchronized (controlRemovedListenerListLock)
        {
            controlRemovedListenerList.add(listener);
        }
    }

    public void removeDynamicControlRemovedListener(dynamicControlRemovedListener listener){
        //synchronized (controlRemovedListenerListLock)
        {
            controlRemovedListenerList.remove(listener);
        }
    }

    public void addGlobalDynamicControlAdvertiseListener(dynamicControlAdvertiseListener listener){
        //synchronized (globalControlListenerListLock)
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
        //synchronized (controlListenerListLock)
        {
            for (dynamicControlAdvertiseListener listener : controlListenerList) {
                listener.dynamicControlEvent(msg);
            }
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
     * Send Control Specific Update Message
     * @param control Control that had value set
     */
    void sendUpdateMessage (DynamicControl control)
    {
        OSCMessage msg = control.buildUpdateMessage();
        sendDynamicControlMessage(msg);
    }
    /**
     * Add a control to our map. Generate a control added event
     * @param control The Dynamic Control we are adding
     */
    public void addControl(DynamicControl control)
    {
        //synchronized (dynamicControlsLock)
        {
            dynamicControls.put(control.getControlMapKey(), control);

            if (!disableControlMimic) {
                // We are going to add ourselves as a listener to the update value so we can send any updates to controller
                control.addGlobalControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {

                        // We need to update all the identical controls if this control is not us
                        if (control.getControlScope() != ControlScope.UNIQUE) {
                            List<DynamicControl> name_list = getControlsByName(control.getControlName());
                            for (DynamicControl mimic_control : name_list) {
                                if (mimic_control != control) { // Make sure it is not us
                                    mimic_control.updateControl(control);
                                }


                            }
                            // we need to see if this was a global scope
                            boolean send_global = false;
                            //synchronized (globalControlListenerListLock)
                            {
                                send_global = globalControlListenerList.size() > 0;
                            }

                            if (send_global) {

                                if (control.getControlScope() == ControlScope.GLOBAL) {
                                    // needs to be broadcast
                                    OSCMessage msg = control.buildGlobalMessage();
                                    sendGlobalDynamicControlMessage(msg);
                                }
                            }
                        }
                    }
                });
            } // end !disableControlMimic

            control.addValueSetListener(new DynamicControl.DynamicControlListener() {
                @Override
                public void update(DynamicControl control) {
                    sendUpdateMessage(control);
                }
            });


            String name = control.getControlName();
            getControlsByName(name).add(control);

        }

        boolean send_message = false;
        //synchronized (controlListenerListLock)
        {
            send_message = controlListenerList.size() > 0;
        }

        if (send_message)
        {
            OSCMessage msg = control.buildCreateMessage();
            sendDynamicControlMessage(msg);
        }

        //synchronized (controlCreatedListenerListLock)
        {
            for (dynamicControlCreatedListener dynamicControlCreatedListener : controlCreatedListenerList) {
                dynamicControlCreatedListener.controlCreated(control);
            }
        }
    }

    /**
     * Send OSC Message across to global senders - this message is for Global Scope controls
     * @param msg the Control Message to send
     */
    private synchronized void sendGlobalDynamicControlMessage(OSCMessage msg) {

        //synchronized (globalControlListenerListLock)
        {
            for (dynamicControlAdvertiseListener listener : globalControlListenerList) {
                listener.dynamicControlEvent(msg);
            }
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
        List<DynamicControl> name_list;
        //synchronized (controlScopedDevicesLock)
        {
            name_list = controlScopedDevices.get(name);
            if (name_list == null) {
                name_list = new ArrayList<>();
                controlScopedDevices.put(name, name_list);
            }
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
        //synchronized (dynamicControlsLock)
        {
            return  dynamicControls.getOrDefault(map_key, null);
        }
    }


    /**
     * Remove all listeners and reference to Dynamic Control
     * @param control the control to remove
     */
    public void removeControl(DynamicControl control){
        //synchronized (dynamicControlsLock)
        {
            control.eraseListeners();
            OSCMessage msg = control.buildRemoveMessage();
            sendDynamicControlMessage(msg);

            notifyRemovedListeners(control);

            dynamicControls.remove(control.getControlMapKey());

            // now remove from named Objects
            String name = control.getControlName();

            List<DynamicControl> name_list = getControlsByName(name);
            name_list.remove(control);
        }
    }

    /**
     * Add a control to the sensorSimulation list. These do not get removed
     * @param control the {@link DynamicControl} object we are adding
     */
    public void addSensorSimulationControl(DynamicControl control){
        sensorSimulation.add(control);
    }

    /**
     * Test if the Dynamic Control is a sensor Simulation
     * @param control the {@link DynamicControl} object we are adding
     * @return true if has been previously added through  {@link #addSensorSimulationControl(DynamicControl)} ()}
     */
    public boolean isSensorSimulation(DynamicControl control){
        return sensorSimulation.contains(control);
    }
    /**
     * Notify all listners that control has been removed and then clear list
     * @param control control bein g removed
     */
    private void notifyRemovedListeners(DynamicControl control) {
        //synchronized (controlRemovedListenerListLock)
        {
            for (dynamicControlRemovedListener listener: controlRemovedListenerList){
                listener.controlRemoved(control);
            }
            controlRemovedListenerList.clear();
        }
    }

    /**
     * Erase all the listeners
     */
    public void clearAllListeners()
    {
        //synchronized (dynamicControlsLock)
        {
            Collection<DynamicControl> controls = dynamicControls.values();
            for (DynamicControl control : controls) {
                if (!control.isSimulatorControl()) {
                    control.eraseListeners();

                    OSCMessage msg = control.buildRemoveMessage();
                    sendDynamicControlMessage(msg);
                }
            }
            dynamicControls.clear();

            // now add our simulator controls back
            for (DynamicControl control:
                 sensorSimulation) {
                dynamicControls.put(control.getControlMapKey(), control);
            }

        }
        //synchronized (controlScopedDevicesLock)
        {
            // Clear all Control scope Objects
            controlScopedDevices.forEach((name, named_list)->{
                named_list.clear();
            });

            // No need to clear the actual lists themselves
        }

    }

    private LinkedHashMap<String, DynamicControl> getDynamicControls(){
        return dynamicControls;
    }

    /**
     * get all Dynamic Controls sorted by creation order
     * @return List of dynamic controls on the device.
     */
    public List<DynamicControl> GetSortedControls()
    {
        List<DynamicControl> sorted_list = new ArrayList<DynamicControl>();

        //synchronized (dynamicControlsLock)
        {
            dynamicControls.forEach((key, value) -> {
                sorted_list.add(value);
            });
        }

        return sorted_list;
    }

}
