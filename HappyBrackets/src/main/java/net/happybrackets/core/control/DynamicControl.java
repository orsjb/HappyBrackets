package net.happybrackets.core.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DynamicControl {

    static ControlMap controlMap = ControlMap.getInstance();
    /**
     * Create an Interface to listen to
     */
    public interface DynamicControlListener {
        public void update(Object val);
    }

    private List<DynamicControlListener> controlListenerList = new ArrayList();

    // The Object sketch that this control was created in
    Object parentSketch;
    ControlType controlType;
    String controlName;

    Object objVal = null;
    Object maximumValue = null;
    Object minimumValue = null;

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
        parentSketch = parent_sketch;
        controlType = control_type;
        controlName = name;
        objVal = initial_value;
        controlMap.addControl(this);
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
        this(parent_sketch, control_type, name, initial_value);
        minimumValue = min_value;
        maximumValue = max_value;
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
