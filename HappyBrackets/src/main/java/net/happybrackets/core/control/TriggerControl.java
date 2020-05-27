package net.happybrackets.core.control;

/**
 * This class encapsulates the functionality of {@link DynamicControl} classes of type {@link ControlType#TRIGGER} in a simple API.
 * Trigger controls do not have an internal value.
 * <br><br> All {@link TriggerControl} controls with the same name and {@link ControlScope} will respond to a message {@link TriggerControl#send()}.
 * For example, consider two {@link TriggerControl} objects with the same {@link ControlScope} and name.
<pre>
    TriggerControl control1 = new TriggerControl(this, "Send");

    TriggerControl control2 = new TriggerControl(this, "Send") {
        {@literal @}Override
        public void triggerEvent() {
            System.out.println("Received Trigger");
        }
    };

    control1.send();
 </pre>
 This will cause the {@link TriggerControl#triggerEvent()} function to be called, causing <b>Received Trigger</b> to be printed to standard output

 * <br><br>The control can also schedule trigger messages to be sent at a time in the future by adding the scheduled time to the message
 *  {@link TriggerControl#send(double)}  function. Eg
 * <br><br> <b>control1.send (HB.getSchedulerTime() + 1000);</b> // this will trigger control1 value, as well as all other TriggerControls with same {@link ControlScope} and name, 1 second in the future.
 *
 *
 <br><br>Triggering an event within the HappyBrackets control display is effected by clicking the button associated with the control

 * If you  require a handler on the class, override the {@link #triggerEvent()} function
 */
 public class TriggerControl extends DynamicControlParent{


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
     * Send a trigger event. This will pass the message on to all other {@link DynamicControl} with matching name, type and {@link ControlScope} and call {@link #triggerEvent()}.
     <pre>
     TriggerControl control1 = new TriggerControl(this, "Send");

     TriggerControl control2 = new TriggerControl(this, "Send") {
        {@literal @}Override
        public void triggerEvent() {
            System.out.println("Received Trigger");
        }
     };

     <b>control1.send()</b>;
     </pre>
     This will cause the {@link TriggerControl#triggerEvent()} function to be called, causing <b>Received Trigger</b> to be printed to standard output

     */
    public void send(){
        getDynamicControl().setValue(null);
    }

    /**
     * Send a trigger event at a specific time. Identical to the {@link #send()} with the exception that the {@link #triggerEvent()}  event will be caused at the {@link net.happybrackets.core.scheduling.HBScheduler} scheduled time passed in.
     * For example:
     <pre>
     TriggerControl control1 = new TriggerControl(this, "Send");

     TriggerControl control2 = new TriggerControl(this, "Send") {
        {@literal @}Override
        public void triggerEvent() {
            System.out.println("Received Trigger");
        }
     };
     <b>control1.send (HB.getSchedulerTime() + 1000);</b> // this will trigger control1 value, as well as all other TriggerControls with same {@link ControlScope} and name, 1 second in the future.
     </pre>

     * @param scheduler_time the scheduler time this is supposed to occur at
     */
    public void send(double scheduler_time){
        getDynamicControl().setValue(null, scheduler_time);
    }

    /**
     * Triggered events are received through this function after calling {@link #send()} on the object. For example:
     <pre>
    TriggerControl control1 = new TriggerControl(this, "Send");

    TriggerControl control2 = new TriggerControl(this, "Send") {
        {@literal @}Override
            <b>public void triggerEvent() {
            System.out.println("Received Trigger");
        }</b>
    };
     control1.send();
     </pre>
     */
    public void triggerEvent(){};

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
     * Change how to display object as {@link net.happybrackets.core.control.DynamicControl.DISPLAY_TYPE}
     * We must do this in subclass
     * @param display_type The new display type
     * @return this object
     */
    public TriggerControl setDisplayType(DynamicControl.DISPLAY_TYPE display_type){
        getDynamicControl().setDisplayType(display_type);
        return this;
    }
}
