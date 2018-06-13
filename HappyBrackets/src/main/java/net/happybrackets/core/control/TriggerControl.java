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

    public abstract void triggerEvent();
}
