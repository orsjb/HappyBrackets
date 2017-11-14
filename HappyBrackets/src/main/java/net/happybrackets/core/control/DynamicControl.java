package net.happybrackets.core.control;

import de.sciss.net.OSCMessage;
import net.happybrackets.core.Device;
import net.happybrackets.core.OSCVocabulary;

import java.util.ArrayList;
import java.util.List;

/**
 * This class facilitates sending message values between sketches,
 * devices, and a graphical environment
 * The values can be represented as sliders, text boxes, check boxes, and buttons
 *
 * A message can either be an integer, a float, a string, a boolean, or a trigger
 *
 * The messages are send and received via DynamicControlListener
 *
 * The ControlScope determines wehther the control can be seen in other classes or even other devices on the network
 */
public class DynamicControl {

    /**
     * Create an Interface to listen to
     */
    public interface DynamicControlListener {
        public void update(DynamicControl control);
    }

    /**
     * The way Create Messages are sent
     */
    private enum CREATE_MESSAGE_ARGS {
        DEVICE_NAME,
        MAP_KEY,
        CONTROL_NAME,
        PARENT_SKETCH_NAME,
        PARENT_SKETCH_ID,
        CONTROL_TYPE,
        OBJ_VAL,
        MIN_VAL,
        MAX_VAL,
        CONTROL_SCOPE
    }

    // Define the Arguments used in an Update message
    private enum UPDATE_MESSAGE_ARGS {
        DEVICE_NAME,
        CONTROL_NAME,
        CONTROL_TYPE,
        MAP_KEY,
        OBJ_VAL,
        MIN_VAL,
        MAX_VAL,
        CONTROL_SCOPE
    }


    static ControlMap controlMap = ControlMap.getInstance();

    private static int instanceCounter = 0; // we will use this to order the creation of our objects and give them a unique number on device
    private Object instanceCounterLock = new Object();

    private final String controlMapKey;



    private List<DynamicControlListener> controlListenerList = new ArrayList();

    // The Object sketch that this control was created in
    private Object parentSketch = null;
    final int parentId;

    private final String deviceName;

    private String parentSketchName;
    private ControlType controlType;
    final String controlName;
    private ControlScope controlScope = ControlScope.SKETCH;

    private Object objVal = 0;
    private Object maximumDisplayValue = 0;
    private Object minimumDisplayValue = 0;



    /**
     * This is a private constructor used to initialise constant attributes of this object
     *
     * @param parent_sketch the object calling - typically this
     * @param control_type  The type of control you want to create
     * @param name          The name we will give to differentiate between different controls in this class
     * @param initial_value The initial value of the control
     * @param init_only This is unused and just used because be cannot add default parameters to
     *                  a constructor - hence, a private constructor
     */
    private DynamicControl(Object parent_sketch, ControlType control_type, String name, Object initial_value, boolean init_only) {
        parentSketch = parent_sketch;
        parentSketchName = parent_sketch.getClass().getName();
        controlType = control_type;
        controlName = name;
        objVal = initial_value;
        parentId = parent_sketch.hashCode();
        deviceName = Device.getDeviceName();
        synchronized (instanceCounterLock) {
            controlMapKey = Device.getDeviceName() +  instanceCounter;
            instanceCounter++;
        }

    }

    /**
     * Dispose of this object and have it removed from control map and listeners
     */
    public void dispose()
    {
        controlMap.removeControl(this);
    }

    /**
     * A dynamic control that can be accessed from outside this sketch
     * it is created with the sketch object that contains it along with the type
     *
     * @param parent_sketch the object calling - typically this, however, you can use any class object
     * @param control_type  The type of control message you want to send
     * @param name          The name we will give to associate it with other DynamicControls with identical ControlScope and type.
     * @param initial_value The initial value of the control
     */
    public DynamicControl(Object parent_sketch, ControlType control_type, String name, Object initial_value) {
        this(parent_sketch, control_type, name, initial_value, true);
        controlMap.addControl(this);
    }



    /**
     * A dynamic control that can be accessed from outside
     * it is created with the sketch object that contains it along with the type
     *
     * @param parent_sketch the object calling - typically this, however, you can use any class object
     * @param control_type  The type of control message you want to send
     * @param name          The name we will give to associate it with other DynamicControls with identical ControlScope and type.
     * @param initial_value The initial value of the control
     * @param min_value     The minimum display value of the control. Only used for display purposes
     * @param max_value     The maximum display value of the control. Only used for display purposes
     */
    public DynamicControl(Object parent_sketch, ControlType control_type, String name, Object initial_value, Object min_value, Object max_value) {
        this(parent_sketch, control_type, name, initial_value, true);
        minimumDisplayValue = min_value;
        maximumDisplayValue = max_value;

        controlMap.addControl(this);
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
            notifyListeners();
        }
    }

    /**
     * Get the Dynamic control based on Map key
     *
     * @param map_key the string that we are using as the key
     * @return the Object associated with this control
     */
    public static DynamicControl getControl(String map_key) {

        return controlMap.getControl(map_key);
    }

    /**
     * Update the parameters of this control with another. This would have been caused by an object having other than SKETCH control scope
     * If the parameters are changed, this object will notify it's listeners that a change has occured
     * @param mirror_control The control that we are copying from
     */
    public void updateControl(DynamicControl mirror_control){
        if (mirror_control != null) {

            // first check our scope and type are the same
            boolean scope_matches = getControlScope() == mirror_control.getControlScope() && getControlType() == mirror_control.getControlType();

            if (scope_matches)
            {
                // Now we need to check whether the scope matches us
                if (getControlScope() == ControlScope.CLASS)
                {
                    scope_matches = this.parentSketchName.equals(mirror_control.parentSketchName);
                }
                else if (getControlScope() == ControlScope.DEVICE){
                    scope_matches = this.deviceName.equals(mirror_control.deviceName);
                }
                // Otherwise it must e global. We have a match

            }
            if (scope_matches){
                // do not use setters as we only want to generate one notifyListeners
                boolean changed = false;

                if (!objVal.equals(mirror_control.objVal)) {
                    objVal = mirror_control.objVal;
                    changed = true;
                }

                if (!minimumDisplayValue.equals(mirror_control.minimumDisplayValue)) {
                    minimumDisplayValue = mirror_control.minimumDisplayValue;
                    changed = true;
                }

                if (!maximumDisplayValue.equals(mirror_control.maximumDisplayValue)) {
                    maximumDisplayValue = mirror_control.maximumDisplayValue;
                    changed = true;
                }

                if (!controlScope.equals(mirror_control.controlScope)) {
                    controlScope = mirror_control.controlScope;
                    changed = true;
                }

                if (changed) {
                    notifyListeners();
                }
            }
        }
    }


    /**
     * Process the Update Message from an OSC Message. Examine buildUpdateMessage for parameters inside Message
     * @param msg OSC message with new value
     */
    public static void processUpdateMessage(OSCMessage msg){

        String map_key = (String) msg.getArg(UPDATE_MESSAGE_ARGS.MAP_KEY.ordinal());
        String control_name = (String) msg.getArg(UPDATE_MESSAGE_ARGS.CONTROL_NAME.ordinal());


        ControlType control_type = ControlType.values()[(int) msg.getArg(UPDATE_MESSAGE_ARGS.CONTROL_TYPE.ordinal())];


        Object obj_val = msg.getArg(UPDATE_MESSAGE_ARGS.OBJ_VAL.ordinal());
        Object min_val = msg.getArg(UPDATE_MESSAGE_ARGS.MIN_VAL.ordinal());
        Object max_val = msg.getArg(UPDATE_MESSAGE_ARGS.MAX_VAL.ordinal());
        ControlScope control_scope = ControlScope.values ()[(int) msg.getArg(UPDATE_MESSAGE_ARGS.CONTROL_SCOPE.ordinal())];

        DynamicControl control = getControl(map_key);
        if (control != null)
        {
            // do not use setters as we only want to generate one notifyListeners
            boolean changed = false;

            if (!obj_val.equals(control.objVal)) {
                control.objVal = obj_val;
                changed = true;
            }

            if (!max_val.equals(control.maximumDisplayValue)) {
                control.maximumDisplayValue = max_val;
                changed = true;
            }

            if (!min_val.equals(control.minimumDisplayValue)) {
                control.maximumDisplayValue = min_val;
                changed = true;
            }

            if (!control_scope.equals(control.controlScope)) {
                control.controlScope = control_scope;
                changed = true;
            }

            if (changed) {
                control.notifyListeners();
            }
        }
        else if (control_scope == ControlScope.GLOBAL)
        {
            List<DynamicControl> named_controls = controlMap.getControlsByName(control_name);
            for (DynamicControl named_control : named_controls) {
                if (control_scope.equals(named_control.controlScope) && control_type.equals(named_control.controlType)) {
                    named_control.setValue(obj_val);
                }
            }
        }
    }

    /**
     * Build OSC Message that specifies a removal of a control
     * @return
     */
    public OSCMessage buildRemoveMessage(){
        return new OSCMessage(OSCVocabulary.DynamicControlMessage.DESTROY,
                new Object[]{
                        deviceName,
                        controlMapKey
                });

    }

    /**
     * Build OSC Message that specifies an update
     * @return
     */
    public OSCMessage buildUpdateMessage(){
        return new OSCMessage(OSCVocabulary.DynamicControlMessage.UPDATE,
                new Object[]{
                        deviceName,
                        controlName,
                        controlType.ordinal(),
                        controlMapKey,
                        objVal,
                        minimumDisplayValue,
                        maximumDisplayValue,
                        controlScope.ordinal()
                });

    }


    /**
     * Build the OSC Message for a create message
     * @return OSC Message required to create the object
     */
    public OSCMessage buildCreateMessage() {
        return new OSCMessage(OSCVocabulary.DynamicControlMessage.CREATE,
                new Object[]{
                        deviceName,
                        controlMapKey,
                        controlName,
                        parentSketchName,
                        parentId,
                        controlType.ordinal(),
                        objVal,
                        minimumDisplayValue,
                        maximumDisplayValue,
                        controlScope.ordinal()
                });

    }


    /**
     * Create a DynamicControl based on OSC Message. This will keep OSC implementation inside this class
     * The buildUpdateMessage shows how messages are constructed
     * @param msg the OSC Message with the parameters to make Control
     */
    public DynamicControl (OSCMessage msg)
    {
        deviceName = (String) msg.getArg(CREATE_MESSAGE_ARGS.DEVICE_NAME.ordinal());
        controlMapKey = (String) msg.getArg(CREATE_MESSAGE_ARGS.MAP_KEY.ordinal());
        controlName = (String) msg.getArg(CREATE_MESSAGE_ARGS.CONTROL_NAME.ordinal());
        parentSketchName = (String) msg.getArg(CREATE_MESSAGE_ARGS.PARENT_SKETCH_NAME.ordinal());
        parentId =  (int) msg.getArg(CREATE_MESSAGE_ARGS.PARENT_SKETCH_ID.ordinal());
        controlType = ControlType.values ()[(int) msg.getArg(CREATE_MESSAGE_ARGS.CONTROL_TYPE.ordinal())];
        objVal = msg.getArg(CREATE_MESSAGE_ARGS.OBJ_VAL.ordinal());
        minimumDisplayValue = msg.getArg(CREATE_MESSAGE_ARGS.MIN_VAL.ordinal());
        maximumDisplayValue = msg.getArg(CREATE_MESSAGE_ARGS.MAX_VAL.ordinal());
        controlScope = ControlScope.values ()[(int) msg.getArg(CREATE_MESSAGE_ARGS.CONTROL_SCOPE.ordinal())];
        controlMap.addControl(this);
    }

    /**
     * Get the map key created in the device as a method for mapping back
     * @return
     */
    public String getControlMapKey(){
        return controlMapKey;
    }


    /**
     * Set the value of the object and notify any listeners
     * Additionally, the value will propagate to any controls that match the control scope
     * @param val the value to set
     */
    public void setValue(Object val)
    {
        if (!objVal.equals(val)) {
            objVal = val;
            notifyListeners();
        }
    }


    /**
     * Gets the value of the control. The type needs to be cast to the required type in the listener
     * @return Control Value
     */
    public Object getValue(){
        return objVal;
    }

    /**
     * The maximum value that we want as a display, for example, in a slider control. Does not limit values in the messages
     * @return The maximum value we want a graphical display to be set to
     */
    public Object getMaximumDisplayValue(){
        return maximumDisplayValue;
    }

    /**
     * The minimum value that we want as a display, for example, in a slider control. Does not limit values in the messages
     * @return The minimum value we want a graphical display to be set to
     */
    public Object getMinimumDisplayValue(){
        return minimumDisplayValue;
    }

    /**
     * Get the name of the control used for ControlScope matching. Also displayed in GUI
     * @return
     */
    public String getControlName(){
        return controlName;
    }

    /**
     * Register Listener to receive changed values in the control
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
     * Deregister listener so it no longer receives messages from this control
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
