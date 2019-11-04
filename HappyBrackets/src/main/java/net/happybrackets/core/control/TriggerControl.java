package net.happybrackets.core.control;

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
