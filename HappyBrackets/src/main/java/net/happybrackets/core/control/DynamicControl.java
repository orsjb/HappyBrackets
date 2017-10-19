package net.happybrackets.core.control;

import java.util.ArrayList;
import java.util.List;

public class DynamicControl {

    /**
     * Create an Interface to
     */
    public interface DynamicControlListener {
        public void update(Object val);
    }

    private List<DynamicControlListener> controlListenerList = new ArrayList();

    // The Object sketch that this control was created in
    Object parentSketch;
    ControlType controlType;

    Object objVal = null;
    /**
     * A dynamic control that can be accessed from outside
     * it is created with the sketch object that contains it along with the type
     * @param parent_sketch the object calling - typically this
     * @param control_type The type of control you wan to create
     */
    public DynamicControl (Object parent_sketch, ControlType control_type)
    {
        parentSketch = parent_sketch;
        controlType = control_type;
        ControlMap.addControl(this);
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
     * Register Listener
     * @param listener Listener to register for events
     */
    private void addControlListener(DynamicControlListener listener)
    {
        synchronized (controlListenerList) {controlListenerList.add(listener);}
    }

    /**
     * Deregister listener
     * @param listener
     */
    private void removeControlListener(DynamicControlListener listener)
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
                catch (Exception ex){}
            });
        }
    }

}
