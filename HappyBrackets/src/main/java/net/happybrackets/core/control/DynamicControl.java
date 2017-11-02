package net.happybrackets.core.control;

import de.sciss.net.OSCMessage;
import net.happybrackets.core.Device;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.device.config.DeviceConfig;
import net.happybrackets.device.network.UDPCachedMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DynamicControl {

    static ControlMap controlMap = ControlMap.getInstance();

    private final int controlHashCode;

    /**
     * Create an Interface to listen to
     */
    public interface DynamicControlListener {
        public void update(DynamicControl control);
    }


    private List<DynamicControlListener> controlListenerList = new ArrayList();

    // The Object sketch that this control was created in
    String parentSketch;
    ControlType controlType;
    String controlName;
    ControlScope controlScope = ControlScope.SKETCH;

    Object objVal = 0;
    Object maximumValue = 0;
    Object minimumValue = 0;

    private void Init(String parent_sketch, ControlType control_type, String name, Object initial_value) {
        parentSketch = parent_sketch;
        controlType = control_type;
        controlName = name;
        objVal = initial_value;

    }

    /**
     * Dispose of this object and have it removed from control map and listeners
     */
    public void dispose()
    {
        controlMap.removeControl(this);
    }

    /**
     * A dynamic control that can be accessed from outside
     * it is created with the sketch object that contains it along with the type
     *
     * @param parent_sketch the object calling - typically this
     * @param control_type  The type of control you want to create
     * @param name          The name we will give to differentiate between different controls in this class
     * @param initial_value The initial value of the control
     */
    public DynamicControl(Object parent_sketch, ControlType control_type, String name, Object initial_value) {
        Init(parent_sketch.getClass().getName(), control_type, name, initial_value);
        controlHashCode = hashCode();
        controlMap.addControl(this);
        //sendCreateMessage();
    }

    /**
     * A dynamic control that can be accessed from outside
     * it is created with the sketch object that contains it along with the type
     *
     * @param parent_sketch the object calling - typically this
     * @param control_type  The type of control you want to create
     * @param name          The name we will give to differentiate between different controls in this class
     * @param initial_value The initial value of the control
     * @param min_value     The minimum value of the control
     * @param max_value     The maximum value of the control
     */
    public DynamicControl(Object parent_sketch, ControlType control_type, String name, Object initial_value, Object min_value, Object max_value) {
        Init(parent_sketch.getClass().getName(), control_type, name, initial_value);
        minimumValue = min_value;
        maximumValue = max_value;
        controlHashCode = hashCode();
        controlMap.addControl(this);
        //sendCreateMessage();
    }


    /**
     * Get the type of control we want
     * @return
     */
    public ControlType getControlType(){
        return controlType;
    }
    /**
     * Get the scope of this control. Can be Sketch, Class, Device, or global
     * @return
     */
    public ControlScope getControlScope(){
        return controlScope;
    }

    /**
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * @param new_scope
     */
    public synchronized void setControlScope(ControlScope new_scope)
    {
        ControlScope old_scope = controlScope;
        if (old_scope != new_scope) {
            controlScope = new_scope;
            ControlMap.getInstance().updateControlScope(this, old_scope);
        }
    }

    /**
     * Get the Dynamic control based on hash key
     *
     * @param hash_code the hash_code that we are using as the key
     * @return the Object associated with this control
     */
    public static DynamicControl getControl(int hash_code) {

        return controlMap.getControl(hash_code);
    }

    /**
     * Process the Update Message from an OSC Message. Examine buildUpdateMessage for parameters inside Message
     * @param msg OSC message with new value
     */
    public static void processUpdateMessage(OSCMessage msg){
        final int HASH_CODE = 1;
        final int OBJ_VAL = 2;
        int hash_code = (int) msg.getArg(HASH_CODE);
        Object obj_val = msg.getArg(OBJ_VAL);
        DynamicControl control = getControl(hash_code);
        if (control != null)
        {
            control.setValue(obj_val);
        }
    }

    /**
     * Build OSC Message that sepecifies a removal
     * @return
     */
    public OSCMessage buildRemoveMessage(){
        return new OSCMessage(OSCVocabulary.DynamicControlMessage.DESTROY,
                new Object[]{
                        Device.getDeviceName(),
                        getControlHashCode()
                });

    }

    /**
     * Build OSC Message that sepecifies an update
     * @return
     */
    public OSCMessage buildUpdateMessage(){
        return new OSCMessage(OSCVocabulary.DynamicControlMessage.UPDATE,
                new Object[]{
                        Device.getDeviceName(),
                        getControlHashCode(),
                        objVal
                });

    }


    /**
     * Build the OSC Message for a create message
     * @return DeviceName, HashCode, name, parentClass, ControlType, value, min, max
     */
    public OSCMessage buildCreateMessage() {
        return new OSCMessage(OSCVocabulary.DynamicControlMessage.CREATE,
                new Object[]{
                        Device.getDeviceName(),
                        hashCode(),
                        controlName,
                        parentSketch,
                        controlType.ordinal(),
                        objVal,
                        minimumValue,
                        maximumValue,
                        controlScope.ordinal()
                });

    }


    /**
     * Create a DynamicControl based on OSC Message. This will keep OSC implementation inside this class
     * The buildUpdateMessage shows how messages are constructed
     * @param msg the OSC Message with the paramaters to make Control
     */
    public DynamicControl (OSCMessage msg)
    {
        final int HASH_CODE = 1;
        final int CONTROL_NAME = 2;
        final int PARENT_SKETCH = 3;
        final int CONTROL_TYPE = 4;
        final int OBJ_VAL = 5;
        final int MIN_VAL = 6;
        final int MAX_VAL = 7;
        final int CONTROL_SCOPE = 8;

        controlHashCode = (int) msg.getArg(HASH_CODE);
        controlName = (String) msg.getArg(CONTROL_NAME);
        parentSketch = (String) msg.getArg(PARENT_SKETCH);
        controlType = ControlType.values ()[(int) msg.getArg(CONTROL_TYPE)];
        objVal = msg.getArg(OBJ_VAL);
        minimumValue = msg.getArg(MIN_VAL);
        maximumValue = msg.getArg(MAX_VAL);
        controlScope = ControlScope.values ()[(int) msg.getArg(CONTROL_SCOPE)];
        controlMap.addControl(this);
    }

    /**
     * Get the hash code created in the device as a method for mapping back
     * @return
     */
    public int getControlHashCode(){
        return  controlHashCode;
    }
    /**
     * Set the value of the object and notify any lsiteners
     * @param val the value to set
     */
    public void setValue(Object val)
    {
        objVal = val;
        notifyListeners();
    }


    public Object getValue(){
        return objVal;
    }

    public Object getMaximumValue(){
        return maximumValue;
    }

    public Object getMinimumValue(){
        return minimumValue;
    }

    public String getControlName(){
        return controlName;
    }
    /**
     * Register Listener
     * @param listener Listener to register for events
     */
    public void addControlListener(DynamicControlListener listener)
    {
        if (listener != null) {
            synchronized (controlListenerList) {
                controlListenerList.add(listener);
            }
        }
    }

    /**
     * Deregister listener
     * @param listener
     */
    public void removeControlListener(DynamicControlListener listener)
    {
        if (listener != null) {
            synchronized (controlListenerList) {
                controlListenerList.remove(listener);
            }
        }
    }


    /**
     * Erase all listeners from this control
     */
    public void eraseListeners()
    {
        synchronized (controlListenerList) {controlListenerList.clear();}
    }

    /**
     * Notify all registered listeners of object value
     */
    public void notifyListeners()
    {
        synchronized (controlListenerList)
        {
            controlListenerList.forEach(listener ->
            {
                try
                {
                    listener.update(this);
                }
                catch (Exception ex)
                {
                    System.out.println(ex.getMessage());
                }
            });
        }
    }

}
