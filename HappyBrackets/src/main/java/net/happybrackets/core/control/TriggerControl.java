package net.happybrackets.core.control;

/**
 * This class encapsulates the functionality of {@link DynamicControl} classes of type {@link ControlType#TRIGGER} in a simple API.
 * Trigger controls do not have an internal value.
 * <br><br> All {@link TriggerControl} controls with the same name and {@link ControlScope} will respond to a message {@link TriggerControl#send()}.
 * For example, consider two {@link TriggerControl} objects with the same {@link ControlScope} and name
 *
 * <br><br> <b>TriggerControl control1 = new TriggerControl(this)</b>....
 * <br> <b>TriggerControl control2 = new TriggerControl(this)</b>....
 *
 * <br><br>Sending from control1 will cause any objects listening to control1 or control2 to receive the action. EG
 *
 * <br><b>control1.send();</b>  // This will also trigger control2
 *
 * <br><br>The control can also schedule trigger messages to be sent at a time in the future by adding the scheduled time to the message
 *  {@link TriggerControl#send(double)}  function. Eg
 * <br><br> <b>control1.send (HB.getSchedulerTime() + 1000);</b> // this will trigger control1 value, as well as all other TriggerControls with same {@link ControlScope} and name, 1 second in the future.
 *
 * <br><br>When the control receives the value, it will be passed through to the {@link TriggerControl#triggerEvent()}  listener that is implemented when the class is created.
 * <br><br>For example
 * <b>
 * <br>TriggerControl myControl = new TriggerControl(this,"My Control"){
 <br>&emsp;@Override
 <br>&emsp;&emsp;public void triggerEvent(){
 <br>&emsp;&emsp;&emsp;System.out.println("Received Trigger");
 <br>&emsp;&emsp;}
 <br>&emsp;};

 <br><br>
 myControl.setValue();
 </b>
 <br><br>This will cause the {@link TriggerControl#triggerEvent()} function to be called, causing <b>Received Trigger</b> to be printed to standard output
 <br><br>Triggering an event within the HappyBrackets control display is effected by clicking the button associated with the control

 * If you do not require a handler on the class, use the {@link TriggerControlSender} class
 */
abstract public class TriggerControl extends DynamicControlParent{


    /**
     * Create a Trigger Dynamic Control
     * @param parent_sketch The parent object for control
     * @param name the name to display
     */
    public TriggerControl(Object parent_sketch, String name) {

        super(new DynamicControl(parent_sketch, ControlType.TRIGGER, name, null));
    }

    // Get our event from parent and send to abstract function
    @Override
    void notifyListener(Object val) {
        triggerEvent();

    }


    /**
     * Send a trigger event
     */
    public void send(){
        getDynamicControl().setValue(null);
    }

    /**
     * Send a trigger event
     * @param scheduler_time the scheduler time this is supposed to occur at
     */
    public void send(double scheduler_time){
        getDynamicControl().setValue(null, scheduler_time);
    }

    public abstract void triggerEvent();

    /**
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * We must do this in subclass
     * @param new_scope The new Control Scope
     * @return this object
     */
    public TriggerControl setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);
        return this;
    }
    /**
     * Change how to display object
     * We must do this in subclass
     * @param display_type The new Control Scope
     * @return this object
     */
    public TriggerControl setDisplayType(DynamicControl.DISPLAY_TYPE display_type){
        getDynamicControl().setDisplayType(display_type);
        return this;
    }
}
