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
        public void update(Object val);
    }

    private List<DynamicControlListener> controlListenerList = new ArrayList();

    // The Object sketch that this control was created in
    String parentSketch;
    ControlType controlType;
    String controlName;

    Object objVal = null;
    Object maximumValue = null;
    Object minimumValue = null;

    private void Init (String parent_sketch, ControlType control_type, String name, Object initial_value) {
        parentSketch = parent_sketch;
        controlType = control_type;
        controlName = name;
        objVal = initial_value;

    }
    /**
     * A dynamic control that can be accessed from outside
     * it is created with the sketch object that contains it along with the type
     * @param parent_sketch the object calling - typically this
     * @param control_type The type of control you want to create
     * @param name The name we will give to differentiate between different controls in this class
     * @param initial_value The initial value of the control
     *
     */
    public DynamicControl (Object parent_sketch, ControlType control_type, String name, Object initial_value)
    {
        Init(parent_sketch.getClass().getName(), control_type, name, initial_value);
        controlHashCode = hashCode();
        controlMap.addControl(this);
        sendCreateMessage();
    }

    /**
     * A dynamic control that can be accessed from outside
     * it is created with the sketch object that contains it along with the type
     * @param parent_sketch the object calling - typically this
     * @param control_type The type of control you want to create
     * @param name The name we will give to differentiate between different controls in this class
     * @param initial_value The initial value of the control
     * @param min_value The minimum value of the control
     * @param max_value The maximum value of the control
     *
     */
    public DynamicControl (Object parent_sketch, ControlType control_type, String name, Object initial_value, Object min_value, Object max_value)
    {
        Init(parent_sketch.getClass().getName(), control_type, name, initial_value);
        minimumValue = min_value;
        maximumValue = max_value;
        controlHashCode = hashCode();
        controlMap.addControl(this);
        sendCreateMessage();
    }



    /**
     * Get the Dynamic control based on hash key
     * @param hash_code the hash_code that we are using as the key
     * @return the Object associated with this control
     */
    public static DynamicControl getControl (int hash_code)
    {
        return controlMap.getControl(hash_code);
    }

    /**
     * Build the OSC Message for a create message
     * @return DeviceName, HashCode, name, parentClass, ControlType, value, min, max
     */
    void sendCreateMessage()
    {
        OSCMessage msg = new OSCMessage(OSCVocabulary.DynamicControlMessage.CREATE,
                new Object[]{
                        Device.getDeviceName(),
                        hashCode(),
                        controlName,
                        parentSketch,
                        controlType.ordinal(),
                        objVal,
                        minimumValue,
                        maximumValue
                });

        try {
            UDPCachedMessage cached_message = new UDPCachedMessage(msg);
            DeviceConfig.getInstance().sendMessageToAllControllers(cached_message.getCachedPacket());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * Create a Dynamic COntrol based on OSC Message. This will keep OSC implementation inside this class
     * The sendCreateMessages shows how messages are constructed
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

        controlHashCode = (int) msg.getArg(HASH_CODE);
        controlName = (String) msg.getArg(CONTROL_NAME);
        parentSketch = (String) msg.getArg(PARENT_SKETCH);
        controlType = ControlType.values ()[(int) msg.getArg(CONTROL_TYPE)];
        objVal = msg.getArg(OBJ_VAL);
        minimumValue = msg.getArg(MIN_VAL);
        maximumValue = msg.getArg(MAX_VAL);
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

    /**
     * Erase all the listeners
     */
    public static void clearAllListeners()
    {
        synchronized (controlMap)
        {
            Collection<DynamicControl> controls = controlMap.getAllControlls().values();
            for (DynamicControl control : controls) {
                control.controlListenerList.clear();
            }
        }
    }

    /**
     * Erase all the listeners
     */
    public static void sendAllControlsToController()
    {
        synchronized (controlMap)
        {
            Collection<DynamicControl> controls = controlMap.getAllControlls().values();
            for (DynamicControl control : controls) {
                control.sendCreateMessage();

            }
        }
    }



    /**
     * Register Listener
     * @param listener Listener to register for events
     */
    public void addControlListener(DynamicControlListener listener)
    {
        synchronized (controlListenerList) {controlListenerList.add(listener);}
    }

    /**
     * Deregister listener
     * @param listener
     */
    public void removeControlListener(DynamicControlListener listener)
    {
        synchronized (controlListenerList) {controlListenerList.remove(listener);}
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
                    listener.update(objVal);
                }
                catch (Exception ex)
                {
                    System.out.println(ex.getMessage());
                }
            });
        }
    }

}
