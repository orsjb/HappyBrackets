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
        void update(DynamicControl control);
    }

    public interface ControlScopeChangedListener {
        void controlScopeChanged(ControlScope new_scope);
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
        CONTROL_SCOPE
    }


    // Define  Global Message arguments
    private enum GLOBAL_MESSAGE_ARGS {
        DEVICE_NAME,
        CONTROL_NAME,
        CONTROL_TYPE,
        OBJ_VAL
    }

    static ControlMap controlMap = ControlMap.getInstance();

    private static int instanceCounter = 0; // we will use this to order the creation of our objects and give them a unique number on device
    private Object instanceCounterLock = new Object();

    private final String controlMapKey;



    private List<DynamicControlListener> controlListenerList = new ArrayList();
    private List<DynamicControlListener> globalControlListenerList = new ArrayList();
    private List<ControlScopeChangedListener> controlScopeChangedList = new ArrayList();

    // This listener is only called when value on control set
    private List<DynamicControlListener> valueSetListenerList = new ArrayList();

    /**
     * Create the text we will display at the beginning of tooltip
     * @param tooltipPrefix The starting text of the tooltip
     * @return this object
     */
    public DynamicControl setTooltipPrefix(String tooltipPrefix) {
        this.tooltipPrefix = tooltipPrefix;
        return this;
    }

    private String tooltipPrefix = "";



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
     * @return The type of value this control is
     */
    public ControlType getControlType(){
        return controlType;
    }
    /**
     * Get the scope of this control. Can be Sketch, Class, Device, or global
     * @return The Scope
     */
    public ControlScope getControlScope(){
        return controlScope;
    }

    /**
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * @param new_scope The new Control Scope
     * @return this object
     */
    public synchronized DynamicControl setControlScope(ControlScope new_scope)
    {
        ControlScope old_scope = controlScope;
        if (old_scope != new_scope) {
            controlScope = new_scope;
            notifyValueSetListeners();
            notifyLocalListeners();
            notifyControlChangeListeners();

        }
        return this;
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
     * @return this object
     */
    public DynamicControl updateControl(DynamicControl mirror_control){
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
                // do not use setters as we only want to generate one notifyLocalListeners
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


                if (changed) {
                    notifyLocalListeners();
                }
            }
        }
        return this;
    }




    /**
     * Process the Global Message from an OSC Message. Examine buildUpdateMessage for parameters inside Message
     * We will not process messages that have come from this device because they will be actioned through local listeners
     * @param msg OSC message with new value
     */
    public static void processGlobalMessage(OSCMessage msg) {

        String device_name = (String) msg.getArg(GLOBAL_MESSAGE_ARGS.DEVICE_NAME.ordinal());
        String control_name = (String) msg.getArg(GLOBAL_MESSAGE_ARGS.CONTROL_NAME.ordinal());
        ControlType control_type = ControlType.values()[(int) msg.getArg(GLOBAL_MESSAGE_ARGS.CONTROL_TYPE.ordinal())];
        Object obj_val = msg.getArg(GLOBAL_MESSAGE_ARGS.OBJ_VAL.ordinal());

        // Make sure we ignore messages from this device
        if (!device_name.equals(Device.getDeviceName())) {
            List<DynamicControl> named_controls = controlMap.getControlsByName(control_name);
            for (DynamicControl named_control : named_controls) {
                if (named_control.controlScope == ControlScope.GLOBAL && control_type.equals(named_control.controlType)) {
                    // we must NOT call setVal as this will generate a global series again.
                    // Just notifyListeners specific to this control but not globally
                    named_control.objVal = obj_val;
                    named_control.notifyLocalListeners();
                }
            }
        }

    }


    /**
     * Process the Update Message from an OSC Message. Examine buildUpdateMessage for parameters inside Message
     * The message is directed as a specific control defined by the MAP_KEY parameter in the OSC Message
     * @param msg OSC message with new value
     */
    public static void processUpdateMessage(OSCMessage msg){

        String map_key = (String) msg.getArg(UPDATE_MESSAGE_ARGS.MAP_KEY.ordinal());
        String control_name = (String) msg.getArg(UPDATE_MESSAGE_ARGS.CONTROL_NAME.ordinal());


        ControlType control_type = ControlType.values()[(int) msg.getArg(UPDATE_MESSAGE_ARGS.CONTROL_TYPE.ordinal())];


        Object obj_val = msg.getArg(UPDATE_MESSAGE_ARGS.OBJ_VAL.ordinal());
        ControlScope control_scope = ControlScope.values ()[(int) msg.getArg(UPDATE_MESSAGE_ARGS.CONTROL_SCOPE.ordinal())];

        DynamicControl control = getControl(map_key);
        if (control != null)
        {
            // do not use setters as we only want to generate one notifyLocalListeners
            boolean changed = false;
            boolean control_scope_changed = false;

            if (!obj_val.equals(control.objVal)) {
                control.objVal = obj_val;
                changed = true;
            }

            if (!control_scope.equals(control.controlScope)) {
                control.controlScope = control_scope;
                changed = true;
                control_scope_changed = true;
            }

            if (changed) {
                control.notifyLocalListeners();
                if (control.getControlScope() != ControlScope.SKETCH){
                    control.notifyGlobalListeners();
                }

            }
            if (control_scope_changed)
            {
                control.notifyControlChangeListeners();
            }
        }
    }

    /**
     * Build OSC Message that specifies a removal of a control
     * @return OSC Message to notify removal
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
     * @return OSC Message To send to specific control
     */
    public OSCMessage buildUpdateMessage(){
        return new OSCMessage(OSCVocabulary.DynamicControlMessage.UPDATE,
                new Object[]{
                        deviceName,
                        controlName,
                        controlType.ordinal(),
                        controlMapKey,
                        objVal,
                        controlScope.ordinal()
                });

    }

    /**
     * Build OSC Message that specifies a Global update
     * @return OSC Message directed to controls with same name, scope, but on different devices
     */
    public OSCMessage buildGlobalMessage(){
        return new OSCMessage(OSCVocabulary.DynamicControlMessage.GLOBAL,
                new Object[]{
                        deviceName,
                        controlName,
                        controlType.ordinal(),
                        objVal,
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
     * @return The unique key to identify this object
     */
    public String getControlMapKey(){
        return controlMapKey;
    }


    /**
     * Set the value of the object and notify any listeners
     * Additionally, the value will propagate to any controls that match the control scope
     * If we are using a trigger, send a random number or a unique value
     * @param val the value to set
     * @return this object
     */
    public DynamicControl setValue(Object val)
    {
        if (!objVal.equals(val)) {
            objVal = val;
            notifyValueSetListeners();
            notifyLocalListeners();
            notifyGlobalListeners();
        }
        return this;
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
     * @return The name of the control for scope matching
     */
    public String getControlName(){
        return controlName;
    }

    /**
     * Register Listener to receive changed values in the control
     * @param listener Listener to register for events
     * @return this listener that has been created
     */
    public DynamicControlListener addControlListener(DynamicControlListener listener)
    {
        if (listener != null) {
            synchronized (controlListenerList) {
                controlListenerList.add(listener);
            }
        }

        return  listener;
    }


    /**
     * Register Listener to receive changed values in the control that need to be global type messages
     * @param listener Listener to register for events
     * @return this listener that has been created
     */
    public DynamicControlListener addGlobalControlListener(DynamicControlListener listener)
    {
        if (listener != null) {
            synchronized (globalControlListenerList) {
                globalControlListenerList.add(listener);
            }
        }

        return  listener;
    }

    /**
     * Register Listener to receive changed values in the control that need to be received when value is specifically set from
     * Within sketch
     * @param listener Listener to register for events
     * @return this listener that has been created
     */
    public DynamicControlListener addValueSetListener(DynamicControlListener listener)
    {
        if (listener != null) {
            synchronized (valueSetListenerList) {
                valueSetListenerList.add(listener);
            }
        }

        return  listener;
    }




    /**
     * Deregister listener so it no longer receives messages from this control
     * @param listener The lsitener we are removing
     * @return this object
     */
    public DynamicControl removeControlListener(DynamicControlListener listener) {
        if (listener != null) {
            synchronized (controlListenerList) {
                controlListenerList.remove(listener);
            }
        }
        return this;
    }


    /**
     * Deregister listener so it no longer receives messages from this control
     * @param listener the listener we are remmoving
     * @return this object
     */
    public DynamicControl removeGlobalControlListener(DynamicControlListener listener) {
        if (listener != null) {
            synchronized (globalControlListenerList) {
                globalControlListenerList.remove(listener);
            }
        }
        return this;
    }
    /**
     * Register Listener to receive changed values in the control scope
     * @param listener Listener to register for events
     * @return this object
     */
    public  DynamicControl addControlScopeListener(ControlScopeChangedListener listener){
        if (listener != null) {
            synchronized (controlScopeChangedList) {
                controlScopeChangedList.add(listener);
            }
        }

        return  this;
    }


    /**
     * Deregister listener so it no longer receives messages from this control
     * @param listener the listener
     * @return this object
     */
    public DynamicControl removeControlScopeChangedListener(ControlScopeChangedListener listener) {
        if (listener != null) {
            synchronized (controlScopeChangedList) {
                controlScopeChangedList.remove(listener);
            }
        }
        return this;
    }

    /**
     * Erase all listeners from this control
     * @return this object
     */
    public DynamicControl eraseListeners()
    {
        synchronized (controlListenerList) {controlListenerList.clear();}
        synchronized (controlScopeChangedList) {controlScopeChangedList.clear();}
        return this;
    }

    /**
     * Notify all registered listeners of object value on this device
     * @return this object
     */
    public DynamicControl notifyLocalListeners()
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
        return this;
    }



    /**
     * Send Update Message when value set
     */
    public void notifyValueSetListeners(){
        synchronized (valueSetListenerList)
        {
            valueSetListenerList.forEach(listener ->
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

    /**
     * Send Global Update Message
     */
    public void notifyGlobalListeners(){
        synchronized (globalControlListenerList)
        {
            globalControlListenerList.forEach(listener ->
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
    /**
     * Notify all registered listeners of object value
     * @return this object
     */
    public DynamicControl notifyControlChangeListeners()
    {
        synchronized (controlScopeChangedList)
        {
            controlScopeChangedList.forEach(listener ->
            {
                try
                {
                    listener.controlScopeChanged(this.getControlScope());
                }
                catch (Exception ex)
                {
                    System.out.println(ex.getMessage());
                }
            });
        }
        return this;
    }


    /**
     * Get the tooltip to display
     * @return the tooltip to display
     */
    public String getTooltipText(){

        String control_scope_text = "";

        if (getControlScope() == ControlScope.SKETCH)
        {
            control_scope_text = "SKETCH scope";
        }
        else if (getControlScope() == ControlScope.CLASS)
        {
            control_scope_text = "CLASS scope - " + parentSketchName;
        }

        else if (getControlScope() == ControlScope.DEVICE)
        {
            control_scope_text = "DEVICE scope - " + deviceName;
        }
        else if (getControlScope() == ControlScope.GLOBAL)
        {
            control_scope_text = "GLOBAL scope";
        }


        return tooltipPrefix + "\n" + control_scope_text;
    }

}
