package net.happybrackets.core.control;

import com.google.gson.Gson;
import de.sciss.net.OSCMessage;
import net.happybrackets.core.Device;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.core.scheduling.HBScheduler;
import net.happybrackets.core.scheduling.ScheduledEventListener;
import net.happybrackets.core.scheduling.ScheduledObject;
import net.happybrackets.device.HB;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * This class facilitates sending message values between sketches,
 * devices, and a graphical environment
 * The values can be represented as sliders, text boxes, check boxes, and buttons
 *
 * A message can either be an integer, a double, a string, a boolean, or a trigger
 *
 * The messages are send and received via DynamicControlListener
 *
 * The ControlScope determines whether the control can be seen in other classes or even other devices on the network
 */
public class DynamicControl implements ScheduledEventListener {

    static Gson gson = new Gson();

    // flag for testing
    static boolean ignoreName = false;
    private boolean isSimulatorControl = false;

    /**
     * Set ignore name for testing
     * @param ignore true to ignore
     */
    static void setIgnoreName(boolean ignore){
        ignoreName =  true;
    }


    /**
     * Define a list of target devices. Can be either device name or IP address
     * If it is a device name, there will be a lookup of stored device names
     */
    Set<String> targetDevices = new HashSet<>();

    /**
     * Define how we want the object displayed in the plugin
     */
    public  enum DISPLAY_TYPE {
        DISPLAY_DEFAULT,
        DISPLAY_HIDDEN,
        DISPLAY_DISABLED,
        DISPLAY_ENABLED_BUDDY,
        DISPLAY_DISABLED_BUDDY
    }

    /**
     * Return all mapped device addresses for this control
     * @return returns the set of mapped targeted devices
     */
    public Set<String>  getTargetDeviceAddresses(){
        return targetDevices;
    }

    @Override
    public void doScheduledEvent(double scheduledTime, Object param) {

        FutureControlMessage message = (FutureControlMessage) param;
        this.objVal = message.controlValue;
        this.executionTime = 0;

        notifyLocalListeners();

        if (!message.localOnly) {
            notifyValueSetListeners();
        }

        synchronized (futureMessageListLock) {
            futureMessageList.remove(message);
        }
    }

    /**
     * Add one or more device names or addresses as strings to use in {@link ControlScope#TARGET} Message
     * @param deviceNames device name or IP Address
     */
    public synchronized void addTargetDevice(String... deviceNames){
        for (String name:
                deviceNames) {
            targetDevices.add(name);
        }
    }


    /**
     * Remove all set target devices and replace with the those provided as arguments
     * Adds device address as a string or device name to {@link ControlScope#TARGET} Message
     * @param deviceNames device name or IP Address
     */
    public synchronized void setTargetDevice(String... deviceNames){
        targetDevices.clear();
        addTargetDevice(deviceNames);
    }

    /**
     * Remove all set target devices and replace with the those provided as arguments
     * Adds device addresses to {@link ControlScope#TARGET} Message
     * @param inetAddresses device name or IP Address
     */
    public synchronized void setTargetDevice(InetAddress... inetAddresses){
        targetDevices.clear();
        addTargetDevice(inetAddresses);
    }

    /**
     * Add one or more device {@link InetAddress} for use in {@link ControlScope#TARGET} Message
     * @param inetAddresses the target addresses to add
     */
    public void addTargetDevice(InetAddress... inetAddresses){
        for (InetAddress address:
                inetAddresses) {
            targetDevices.add(address.getHostAddress());

        }

    }

    /**
     * Clear all devices as Targets
     */
    public synchronized void clearTargetDevices(){
        targetDevices.clear();
    }


    /**
     * Remove one or more device names or addresses as a string.
     * For use in {@link ControlScope#TARGET}  Messages
     * @param deviceNames device names or IP Addresses to remove
     */
    public synchronized void removeTargetDevice(String... deviceNames){
        for (String name:
                deviceNames) {
            targetDevices.remove(name);
        }

    }

    /**
     * Remove one or more {@link InetAddress} for use in {@link ControlScope#TARGET} Message
     * @param inetAddresses the target addresses to remove
     */
    public void removeTargetDevice(InetAddress... inetAddresses){
        for (InetAddress address:
                inetAddresses) {
            targetDevices.remove(address.getHostAddress());
        }
    }

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
        CONTROL_SCOPE,
        DISPLAY_TYPE_VAL
    }

    // Define the Arguments used in an Update message
    private enum UPDATE_MESSAGE_ARGS {
        DEVICE_NAME,
        CONTROL_NAME,
        CONTROL_TYPE,
        MAP_KEY,
        OBJ_VAL,
        CONTROL_SCOPE,
        EXECUTE_TIME,
        DISPLAY_TYPE_VAL
    }


    // Define  Global Message arguments
    private enum NETWORK_TRANSMIT_MESSAGE_ARGS {
        DEVICE_NAME,
        CONTROL_NAME,
        CONTROL_TYPE,
        OBJ_VAL,
        EXECUTE_TIME
    }

    // Define  Device Name Message arguments
    private enum DEVICE_NAME_ARGS {
        DEVICE_NAME
    }

    // Define where our first Array type global dynamic control message is in OSC
    final static int OSC_TRANSMIT_ARRAY_ARG = NETWORK_TRANSMIT_MESSAGE_ARGS.EXECUTE_TIME.ordinal() + 1;

    // When an event nis scheduled in the future, we will create one of these and schedule it
    class FutureControlMessage{
        /**
         * Create a Future COntrol message
         * @param value the value to be executed
         * @param execution_time the time the value needs to be executed
         */
        public FutureControlMessage(Object value, double execution_time){
            controlValue = value;
            executionTime = execution_time;
        }

        Object controlValue;
        double executionTime;
        boolean localOnly = false; // if we are local only, we will not sendValue changed listeners

        /// have a copy of our pending scheduled object in case we want to cancel it
        ScheduledObject pendingSchedule = null;
    }

    static ControlMap controlMap = ControlMap.getInstance();

    private static final Object controlMapLock = new Object();

    private static int instanceCounter = 0; // we will use this to order the creation of our objects and give them a unique number on device
    private final Object instanceCounterLock = new Object();

    private final Object valueChangedLock = new Object();
    private final String controlMapKey;



    private List<DynamicControlListener> controlListenerList = new ArrayList<>();
    private List<DynamicControlListener> globalControlListenerList = new ArrayList<>();
    private List<ControlScopeChangedListener> controlScopeChangedList = new ArrayList<>();

    private List<FutureControlMessage> futureMessageList =  new ArrayList<>();

    // This listener is only called when value on control set
    private List<DynamicControlListener> valueSetListenerList = new ArrayList<>();

    // Create Object to lock shared resources
    private final Object controlScopeChangedLock = new Object();
    private final Object controlListenerLock = new Object();
    private final Object globalListenerLock = new Object();
    private final Object valueSetListenerLock = new Object();
    private final Object futureMessageListLock = new Object();


    static boolean disableScheduler = false; // set flag if we are going to disable scheduler - eg, in GUI
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
    private ControlScope controlScope = ControlScope.UNIQUE;

    private Object objVal = 0;
    private Object maximumDisplayValue = 0;
    private Object minimumDisplayValue = 0;

    // This is the time we want to execute the control value
    private long executionTime =  0;

    DISPLAY_TYPE displayType = DISPLAY_TYPE.DISPLAY_DEFAULT; // Whether the control is displayType on control Screen


    /**
     * Set whether we disable setting all values in context of scheduler
     * @param disabled set true to disable
     */
    public static void setDisableScheduler(boolean disabled){
        disableScheduler = disabled;
    }

    /**
     * Whether we disable the control on the screen
     * @return How we will disable control on screen
     */
    public DISPLAY_TYPE getDisplayType(){
        return displayType;
    }

    /**
     * Set how we will display control  object on the screen
     * @param display_type how we will display control
     * @return this
     */
    public DynamicControl setDisplayType(DISPLAY_TYPE display_type){
        displayType = display_type;
        notifyValueSetListeners();
        notifyLocalListeners();
        return  this;
    }

    /**
     * Returns the JVM execution time we last used when we set the value
     * @return lastExecution time set
     */
    public long getExecutionTime(){
        return executionTime;
    }

    /**
     * Convert a float or int into required number type based on control. If not a FLOAT or INT, will just return value
     * @param control_type the control type
     * @param source_value the value we want
     * @return the converted value
     */
    static private Object convertValue (ControlType control_type, Object source_value) {
        Object ret = source_value;

        // Convert if we are a float control
        if (control_type == ControlType.FLOAT) {
            if (source_value == null){
                ret = 0.0;
            }else if (source_value instanceof Integer) {
                Integer i = (Integer) source_value;
                double f = i.doubleValue();
                ret = f;

            }else if (source_value instanceof Double) {
                Double d = (Double) source_value;
                ret = d;

            }else if (source_value instanceof Long) {
                Long l = (Long) source_value;
                double f = l.doubleValue();
                ret = f;
            } else if (source_value instanceof Float) {
                double f = (Float) source_value;
                ret = f;
            } else if (source_value instanceof String) {
                double f = Double.parseDouble((String)source_value);
                ret = f;
            }
            // Convert if we are an int control
        } else if (control_type == ControlType.INT) {
            if (source_value == null){
                ret = 0;
            }else if (source_value instanceof Float) {
                Float f = (Float) source_value;
                Integer i = f.intValue();
                ret = i;
            }else if (source_value instanceof Double) {
                Double d = (Double) source_value;
                Integer i = d.intValue();
                ret = i;
            }else if (source_value instanceof Long) {
                Long l = (Long) source_value;
                Integer i = l.intValue();
                ret = i;
            }

            // Convert if we are a BOOLEAN control
        } else if (control_type == ControlType.BOOLEAN) {
            if (source_value == null){
                ret = 0;
            }if (source_value instanceof Integer) {
                Integer i = (Integer) source_value;
                Boolean b = i != 0;
                ret = b;

            }else if (source_value instanceof Long) {
                Long l = (Long) source_value;
                Integer i = l.intValue();
                Boolean b = i != 0;
                ret = b;
            }


        // Convert if we are a TRIGGER control
        }else if (control_type == ControlType.TRIGGER) {
            if (source_value == null) {
                ret = System.currentTimeMillis();
            }

        // Convert if we are a TEXT control
        }else if (control_type == ControlType.TEXT) {
            if (source_value == null) {
                ret = "";
            }

        }

        return ret;
    }

    /**
     * Get the Sketch or class object linked to this control
     * @return the parentSketch or Object
     */
    public Object getParentSketch() {
        return parentSketch;
    }


    /**
     * This is a private constructor used to initialise constant attributes of this object
     *
     * @param parent_sketch the object calling - typically this
     * @param control_type  The type of control you want to create
     * @param name          The name we will give to differentiate between different controls in this class
     * @param initial_value The initial value of the control
     * @param display_type  how we want to display the object
     *
     */
    private DynamicControl(Object parent_sketch, ControlType control_type, String name, Object initial_value, DISPLAY_TYPE display_type) {
        if (parent_sketch == null){
            parent_sketch = new Object();
        }

        displayType = display_type;
        parentSketch = parent_sketch;
        parentSketchName = parent_sketch.getClass().getName();
        controlType = control_type;
        controlName = name;

        objVal = convertValue (control_type, initial_value);

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
     * @param control_type  The type of control message you want to send
     * @param name          The name we will give to associate it with other DynamicControls with identical ControlScope and type.
     * @param initial_value The initial value of the control
     */
    public DynamicControl(ControlType control_type, String name, Object initial_value) {
        this(new Object(), control_type, name, initial_value, DISPLAY_TYPE.DISPLAY_DEFAULT);
        synchronized (controlMapLock) {
            controlMap.addControl(this);
        }
    }


    /**
     * A dynamic control that can be accessed from outside this sketch
     * it is created with the sketch object that contains it along with the type
     * @param parent_sketch the object calling - typically this, however, you can use any class object
     * @param control_type  The type of control message you want to send
     * @param name          The name we will give to associate it with other DynamicControls with identical ControlScope and type.
     */
    public DynamicControl(Object parent_sketch, ControlType control_type, String name) {
        this(parent_sketch, control_type, name,  null, DISPLAY_TYPE.DISPLAY_DEFAULT);
        synchronized (controlMapLock) {
            controlMap.addControl(this);
        }
    }

    /**
     * A dynamic control that can be accessed from outside this sketch
     * it is created with the sketch object that contains it along with the type
     *
     * @param control_type  The type of control message you want to send
     * @param name          The name we will give to associate it with other DynamicControls with identical ControlScope and type.
     */
    public DynamicControl(ControlType control_type, String name) {
        this(new Object(), control_type, name, convertValue(control_type, null), DISPLAY_TYPE.DISPLAY_DEFAULT);
        synchronized (controlMapLock) {
            controlMap.addControl(this);
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
        this(parent_sketch, control_type, name, initial_value, DISPLAY_TYPE.DISPLAY_DEFAULT);
        synchronized (controlMapLock) {
            controlMap.addControl(this);
        }
    }


    /**
     * Set this control as a SensorSimulation control so it does not get removed on reset
     * @return this
     */
    public DynamicControl setSensorSimulationController(){
        controlMap.addSensorSimulationControl(this);
        isSimulatorControl = true;
        return this;
    }


    /**
     * See if control is a simulator control
     * @return true if a simulator control
     */
    public boolean isSimulatorControl() {
        return isSimulatorControl;
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
        this(parent_sketch, control_type, name, initial_value, DISPLAY_TYPE.DISPLAY_DEFAULT);

        minimumDisplayValue = convertValue (control_type, min_value);
        maximumDisplayValue  = convertValue (control_type, max_value);

        synchronized (controlMapLock) {
            controlMap.addControl(this);
        }
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
     * @param display_type  The way we want the control displayed
     */
    public DynamicControl(Object parent_sketch, ControlType control_type, String name, Object initial_value, Object min_value, Object max_value, DISPLAY_TYPE display_type) {
        this(parent_sketch, control_type, name, initial_value, display_type);

        minimumDisplayValue = convertValue (control_type, min_value);
        maximumDisplayValue  = convertValue (control_type, max_value);

        synchronized (controlMapLock) {
            controlMap.addControl(this);
        }
    }

    /**
     * A dynamic control that can be accessed from outside
     * it is created with the sketch object that contains it along with the type
     *
     * @param control_type  The type of control message you want to send
     * @param name          The name we will give to associate it with other DynamicControls with identical ControlScope and type.
     * @param initial_value The initial value of the control
     * @param min_value     The minimum display value of the control. Only used for display purposes
     * @param max_value     The maximum display value of the control. Only used for display purposes
     */
    public DynamicControl(ControlType control_type, String name, Object initial_value, Object min_value, Object max_value) {
        this(new Object(), control_type, name, initial_value, DISPLAY_TYPE.DISPLAY_DEFAULT);

        minimumDisplayValue = convertValue (control_type, min_value);
        maximumDisplayValue  = convertValue (control_type, max_value);

        synchronized (controlMapLock) {
            controlMap.addControl(this);
        }
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

            // prevent control scope from changing the value
            //notifyLocalListeners();
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

        DynamicControl ret = null;
        synchronized (controlMapLock) {
            ret = controlMap.getControl(map_key);
        }
        return ret;
    }

    /**
     * Update the parameters of this control with another. This would have been caused by an object having other than SKETCH control scope
     * If the parameters are changed, this object will notify it's listeners that a change has occurred
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
                if (getControlScope() == ControlScope.SKETCH)
                {
                    scope_matches = this.parentSketch == mirror_control.parentSketch && this.parentSketch != null;
                }
                // Now we need to check whether the scope matches us
                else if (getControlScope() == ControlScope.CLASS)
                {
                    scope_matches = this.parentSketchName.equals(mirror_control.parentSketchName);
                }
                else if (getControlScope() == ControlScope.DEVICE){
                    scope_matches = this.deviceName.equals(mirror_control.deviceName);
                }
                else if (getControlScope() == ControlScope.TARGET){
                    // check if our mirror has this address
                    scope_matches = mirror_control.targetsThisDevice();
                }
                // Otherwise it must be global. We have a match

            }
            if (scope_matches) {
                // do not use setters as we only want to generate one notifyLocalListeners
                boolean changed = false;

                    if (mirror_control.executionTime == 0) { // his needs to be done now
                        if (!objVal.equals(mirror_control.objVal)) {
                            //objVal = mirror_control.objVal; // let this get done inside the scheduleValue return
                            changed = true;
                        }


                        if (changed) {
                            scheduleValue(mirror_control.objVal, 0);
                        }

                }
            }
        }
        return this;
    }

    /**
     * Check whether this device is targeted by checking the loopback, localhost and devicenames
     * @return
     */
    private boolean targetsThisDevice() {
        boolean ret = false;
        String device_name = Device.getDeviceName();
        String loopback = InetAddress.getLoopbackAddress().getHostAddress();

        for (String device:
             targetDevices) {
            if (device_name.equalsIgnoreCase(device)){
                return true;
            }
            if (device_name.equalsIgnoreCase(loopback)){
                return true;
            }
            try {
                if (InetAddress.getLocalHost().getHostAddress().equalsIgnoreCase(device)){
                    return true;
                }
            } catch (UnknownHostException e) {
                //e.printStackTrace();
            }
        }

        return ret;
    }


    /**
     * Schedule this control to change its value in context of scheduler
     * @param value the value to send
     * @param execution_time the time it needs to be executed
     * @param local_only if true, will not send value changed to valueChangedListeners
     */
    void scheduleValue(Object value, double execution_time, boolean local_only){

        // We need to convert the Object value into the exact type. EG, integer must be cast to boolean if that is thr control type
        Object converted_value = convertValue(controlType, value);

        if (disableScheduler || execution_time ==  0){
            this.objVal = converted_value;
            this.executionTime = 0;

            notifyLocalListeners();

            if (!local_only) {
                notifyValueSetListeners();
            }
        }
        else {
            FutureControlMessage message = new FutureControlMessage(converted_value, execution_time);

            message.localOnly = local_only;

            message.pendingSchedule = HBScheduler.getGlobalScheduler().addScheduledObject(executionTime, message, this);
            synchronized (futureMessageListLock) {
                futureMessageList.add(message);
            }
        }
    }
    /**
     * Schedule this control to send a value to it's locallisteners at a scheduled time. Will also notify valueListeners (eg GUI controls)
     * @param value the value to send
     * @param execution_time the time it needs to be executed
     */
    void scheduleValue(Object value, double execution_time) {

        scheduleValue(value, execution_time, false);
    }


    /**
     * Process the DynamicControl deviceName message and map device name to IPAddress
     * We ignore our own device
     * @param src_address The address of the device
     * @param msg The OSC Message that has device name
     */
    public static void processDeviceNameMessage(InetAddress src_address, OSCMessage msg) {
        String device_name = (String) msg.getArg(DEVICE_NAME_ARGS.DEVICE_NAME.ordinal());
        if (!Device.getDeviceName().equalsIgnoreCase(device_name)) {
            HB.HBInstance.addDeviceAddress(device_name, src_address);
        }
    }

    /**
     * Process the DynamicControl deviceRequest message
     * Send a deviceName back to src.  Test that their name is mapped correctly
     * If name is not mapped we will request from all devices globally
     * @param src_address The address of the device
     * @param msg The OSC Message that has device name
     */
    public static void processRequestNameMessage(InetAddress src_address, OSCMessage msg) {
        String device_name = (String) msg.getArg(DEVICE_NAME_ARGS.DEVICE_NAME.ordinal());

        // ignore ourself
        if (!Device.getDeviceName().equalsIgnoreCase(device_name)) {
            // send them our message

            OSCMessage nameMessage = buildDeviceNameMessage();
            ControlMap.getInstance().sendGlobalDynamicControlMessage(nameMessage, null);

            // See if we have them mapped the same
            boolean address_changed =  HB.HBInstance.addDeviceAddress(device_name, src_address);

            if (address_changed){
                // request all
                postRequestNamesMessage();
            }
        }
    }

    /**
     * Post a request device name message to other devices so we can target them specifically and update our map
     */
    public static void postRequestNamesMessage(){
        OSCMessage requestMessage = buildDeviceRequestNameMessage();
        ControlMap.getInstance().sendGlobalDynamicControlMessage(requestMessage, null);
    }


    /**
     * Build OSC Message that gives our device name
     * @return OSC Message that has name
     */
    public static OSCMessage buildDeviceNameMessage(){
        return new OSCMessage(OSCVocabulary.DynamicControlMessage.DEVICE_NAME,
                new Object[]{
                        Device.getDeviceName(),
                });
    }

    /**
     * Build OSC Message that requests devices send us their name
     * @return OSC Message to request name
     */
    public static OSCMessage buildDeviceRequestNameMessage(){
        return new OSCMessage(OSCVocabulary.DynamicControlMessage.REQUEST_NAME,
                new Object[]{
                        Device.getDeviceName(),
                });

    }
    /**
     * Process the Global or Target Message from an OSC Message. Examine buildUpdateMessage for parameters inside Message
     * We will not process messages that have come from this device because they will be actioned through local listeners
     * @param msg OSC message with new value
     * @param controlScope the type of {@link ControlScope};
     */
    public static void processOSCControlMessage(OSCMessage msg, ControlScope controlScope) {

        String device_name = (String) msg.getArg(NETWORK_TRANSMIT_MESSAGE_ARGS.DEVICE_NAME.ordinal());
        String control_name = (String) msg.getArg(NETWORK_TRANSMIT_MESSAGE_ARGS.CONTROL_NAME.ordinal());
        ControlType control_type = ControlType.values()[(int) msg.getArg(NETWORK_TRANSMIT_MESSAGE_ARGS.CONTROL_TYPE.ordinal())];
        Object obj_val = msg.getArg(NETWORK_TRANSMIT_MESSAGE_ARGS.OBJ_VAL.ordinal());

        long execution_time = 0;

        if (msg.getArgCount() > NETWORK_TRANSMIT_MESSAGE_ARGS.EXECUTE_TIME.ordinal())
        {
            Object time = msg.getArg(NETWORK_TRANSMIT_MESSAGE_ARGS.EXECUTE_TIME.ordinal());
            execution_time = (int) time;
        }

        // Make sure we ignore messages from this device
        if (ignoreName || !device_name.equals(Device.getDeviceName())) {
            synchronized (controlMapLock) {
                List<DynamicControl> named_controls = controlMap.getControlsByName(control_name);
                for (DynamicControl named_control : named_controls) {
                    if (named_control.controlScope == controlScope && control_type.equals(named_control.controlType)) {
                        // we must NOT call setVal as this will generate a global series again.
                        // Just notifyListeners specific to this control but not globally

                        // we need to see if this is a boolean Object as OSC does not support that
                        if (control_type == ControlType.BOOLEAN) {
                            int osc_val = (int) obj_val;
                            Boolean bool_val = osc_val != 0;
                            obj_val = bool_val;
                        }
                        else if (control_type == ControlType.OBJECT){
                            if (! (obj_val instanceof String)){
                                // This is not a Json Message
                                // We will need to get all the remaining OSC arguments after the schedule time and store that as ObjVal
                                int num_args = msg.getArgCount() - OSC_TRANSMIT_ARRAY_ARG;
                                Object [] restore_args =  new Object[num_args];
                                for (int i = 0; i < num_args; i++){
                                    restore_args[i] = msg.getArg(OSC_TRANSMIT_ARRAY_ARG + i);
                                }

                                obj_val = restore_args;
                            }
                        }

                        // We need to schedule this value
                        named_control.scheduleValue(obj_val, execution_time);
                    }
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
        Object obj_val = msg.getArg(UPDATE_MESSAGE_ARGS.OBJ_VAL.ordinal());
        ControlScope control_scope = ControlScope.values ()[(int) msg.getArg(UPDATE_MESSAGE_ARGS.CONTROL_SCOPE.ordinal())];

        long execution_time = 0;
        DISPLAY_TYPE display_type = DISPLAY_TYPE.DISPLAY_DEFAULT;

        if (msg.getArgCount() > UPDATE_MESSAGE_ARGS.EXECUTE_TIME.ordinal())
        {
            // this should always be zero
            execution_time = (int) msg.getArg(UPDATE_MESSAGE_ARGS.EXECUTE_TIME.ordinal());
        }

        if (msg.getArgCount() > UPDATE_MESSAGE_ARGS.DISPLAY_TYPE_VAL.ordinal())
        {
            int osc_val = (int) msg.getArg(UPDATE_MESSAGE_ARGS.DISPLAY_TYPE_VAL.ordinal());
            display_type = DISPLAY_TYPE.values ()[osc_val];
        }


        DynamicControl control = getControl(map_key);
        if (control != null)
        {
            // do not use setters as we only want to generate one notifyLocalListeners
            boolean changed = false;

            boolean control_scope_changed = false;

            control.displayType = display_type;

            obj_val = convertValue(control.controlType, obj_val);

            if (!obj_val.equals(control.objVal)) {
                changed = true;
            }

            if (!control_scope.equals(control.controlScope)) {
                control.controlScope = control_scope;
                //control.executionTime = execution_time;
                changed = true;
                control_scope_changed = true;
            }

            if (changed) {
                control.scheduleValue(obj_val, 0, true);

                if (control.getControlScope() != ControlScope.UNIQUE){
                    // we will execute via schedule if necessary
                    //control.scheduleValue(obj_val, execution_time);

                    control.objVal = obj_val;
                    control.notifyGlobalListeners();
                }
                /*
                else
                {

                    control.objVal = convertValue(control.controlType, obj_val);
                    control.executionTime = execution_time;

                    control.notifyLocalListeners();
                }
                */

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
     * Return an object that can be sent by OSC based on control TYpe
     * @param obj_val The object value we want to send
     * @return the type we will actually send
     */
    private Object OSCArgumentObject (Object obj_val){
        Object ret = obj_val;

        if (obj_val instanceof Boolean)
        {
            boolean b = (Boolean) obj_val;
            return b? 1:0;
        }
        else if (obj_val instanceof  Double){
            String s = ((Double)obj_val).toString();
            ret = s;
        }
        return ret;

    }
    /**
     * Build OSC Message that specifies an update
     * @return OSC Message To send to specific control
     */
    public OSCMessage buildUpdateMessage(){

        Object sendObjType = objVal;
        if (controlType == ControlType.OBJECT){
            sendObjType = objVal.toString();
        }

        return new OSCMessage(OSCVocabulary.DynamicControlMessage.UPDATE,
                new Object[]{
                        deviceName,
                        controlName,
                        controlType.ordinal(),
                        controlMapKey,
                        OSCArgumentObject(sendObjType),
                        controlScope.ordinal(),
                        0, // We will make zero as we want to update now  (int)executionTime,
                        displayType.ordinal()
                });

    }

    /**
     * Build OSC Message that specifies a Network update
     * @return OSC Message directed to controls with same name, scope, but on different devices
     */
    public OSCMessage buildGlobalMessage(){

        String OSC_MessageName = OSCVocabulary.DynamicControlMessage.GLOBAL;

        if (controlScope ==  ControlScope.TARGET){
            OSC_MessageName = OSCVocabulary.DynamicControlMessage.TARGET;
        }

        if (controlType == ControlType.OBJECT){

            // we need to see if we have a custom encode function
            if (objVal instanceof CustomGlobalEncoder){
                Object [] encode_data = ((CustomGlobalEncoder)objVal).encodeGlobalMessage();
                int num_args = OSC_TRANSMIT_ARRAY_ARG + encode_data.length;
                Object [] osc_args = new Object[num_args];
                osc_args[0] = deviceName;
                osc_args[1] = controlName;
                osc_args[2] = controlType.ordinal();
                osc_args[3] = 0; // by defining zero we are going to say this is NOT json
                osc_args[4] = (int)executionTime;

                // now encode the object parameters
                for (int i = 0; i < encode_data.length; i++){
                    osc_args[OSC_TRANSMIT_ARRAY_ARG + i] = encode_data[i];
                }
                return new OSCMessage(OSC_MessageName,
                        osc_args);
            }
            else
            {
                String jsonString =  gson.toJson(objVal);
                return new OSCMessage(OSC_MessageName,
                        new Object[]{
                                deviceName,
                                controlName,
                                controlType.ordinal(),
                                jsonString,
                                (int) executionTime
                        });
            }
        }
        else {
            return new OSCMessage(OSCVocabulary.DynamicControlMessage.GLOBAL,
                    new Object[]{
                            deviceName,
                            controlName,
                            controlType.ordinal(),
                            OSCArgumentObject(objVal),
                            (int) executionTime
                    });
        }
    }
    /**
     * Build the OSC Message for a create message
     * @return OSC Message required to create the object
     */
    public OSCMessage buildCreateMessage() {

        Object sendObjType = objVal;
        if (controlType == ControlType.OBJECT){
            sendObjType = objVal.toString();
        }

        return new OSCMessage(OSCVocabulary.DynamicControlMessage.CREATE,
                new Object[]{
                        deviceName,
                        controlMapKey,
                        controlName,
                        parentSketchName,
                        parentId,
                        controlType.ordinal(),
                        OSCArgumentObject(sendObjType),
                        OSCArgumentObject(minimumDisplayValue),
                        OSCArgumentObject(maximumDisplayValue),
                        controlScope.ordinal(),
                        displayType.ordinal()
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
        objVal = convertValue  (controlType, msg.getArg(CREATE_MESSAGE_ARGS.OBJ_VAL.ordinal()));
        minimumDisplayValue = convertValue  (controlType, msg.getArg(CREATE_MESSAGE_ARGS.MIN_VAL.ordinal()));
        maximumDisplayValue = convertValue  (controlType, msg.getArg(CREATE_MESSAGE_ARGS.MAX_VAL.ordinal()));
        controlScope = ControlScope.values ()[(int) msg.getArg(CREATE_MESSAGE_ARGS.CONTROL_SCOPE.ordinal())];

        if (msg.getArgCount() > CREATE_MESSAGE_ARGS.DISPLAY_TYPE_VAL.ordinal())
        {
            int osc_val = (int) msg.getArg(CREATE_MESSAGE_ARGS.DISPLAY_TYPE_VAL.ordinal());
            displayType = DISPLAY_TYPE.values ()[osc_val];
        }


        synchronized (controlMapLock) {
            controlMap.addControl(this);
        }
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
       return setValue(val, 0);
    }

    /**
     * Set the value of the object and notify any listeners
     * Additionally, the value will propagate to any controls that match the control scope
     * If we are using a trigger, send a random number or a unique value
     * @param val the value to set
     * @param execution_time the Scheduler time we want this to occur
     * @return this object
     */
    public DynamicControl setValue(Object val, long execution_time)
    {
        executionTime = execution_time;
        val = convertValue (controlType, val);

        if (!objVal.equals(val)) {
            if (controlType == ControlType.FLOAT)
            {
                objVal = (Double) val;
            }
            else {
                objVal = val;
            }

            notifyGlobalListeners();

            scheduleValue(val, execution_time);

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
     * @return this
     */
    public DynamicControl addControlListener(DynamicControlListener listener)
    {
        if (listener != null) {
            synchronized (controlListenerLock) {
                controlListenerList.add(listener);
            }
        }

        return  this;
    }


    /**
     * Register Listener to receive changed values in the control that need to be global type messages
     * @param listener Listener to register for events
     * @return this listener that has been created
     */
    public DynamicControl addGlobalControlListener(DynamicControlListener listener)
    {
        if (listener != null) {
            synchronized (globalListenerLock) {
                globalControlListenerList.add(listener);
            }
        }

        return this;
    }

    /**
     * Register Listener to receive changed values in the control that need to be received when value is specifically set from
     * Within sketch
     * @param listener Listener to register for events
     * @return this
     */
    public DynamicControl addValueSetListener(DynamicControlListener listener)
    {
        if (listener != null) {
            synchronized (valueSetListenerLock) {
                valueSetListenerList.add(listener);
            }
        }

        return  this;
    }




    /**
     * Deregister listener so it no longer receives messages from this control
     * @param listener The lsitener we are removing
     * @return this object
     */
    public DynamicControl removeControlListener(DynamicControlListener listener) {
        if (listener != null) {
            synchronized (controlListenerLock) {
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
            synchronized (globalListenerLock) {
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
            synchronized (controlScopeChangedLock) {
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
            synchronized (controlScopeChangedLock) {
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
        // We need to
        synchronized (futureMessageListLock){
            for (FutureControlMessage message:
                 futureMessageList) {
                message.pendingSchedule.setCancelled(true);

            }
            futureMessageList.clear();
        }

        synchronized (controlListenerLock) {controlListenerList.clear();}
        synchronized (controlScopeChangedLock) {controlScopeChangedList.clear();}
        return this;
    }

    /**
     * Notify all registered listeners of object value on this device
     * @return this object
     */
    public DynamicControl notifyLocalListeners()
    {
        synchronized (controlListenerLock)
        {
            controlListenerList.forEach(listener ->
            {
                try
                {
                    listener.update(this);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            });
        }
        return this;
    }



    /**
     * Send Update Message when value set
     */
    public void notifyValueSetListeners(){
        synchronized (valueSetListenerLock)
        {
            valueSetListenerList.forEach(listener ->
            {
                try
                {
                    listener.update(this);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            });
        }
    }

    /**
     * Send Global Update Message
     */
    public void notifyGlobalListeners(){
        synchronized (globalListenerLock)
        {
            globalControlListenerList.forEach(listener ->
            {
                try
                {
                    listener.update(this);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
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
        synchronized (controlScopeChangedLock)
        {
            controlScopeChangedList.forEach(listener ->
            {
                try
                {
                    listener.controlScopeChanged(this.getControlScope());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
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
        if (getControlScope() == ControlScope.UNIQUE)
        {
            control_scope_text = "UNIQUE scope";
        }
        else if (getControlScope() == ControlScope.SKETCH)
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
