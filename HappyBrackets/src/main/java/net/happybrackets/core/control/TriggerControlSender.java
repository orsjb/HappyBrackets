package net.happybrackets.core.control;

/**
 * This class is used for only sending values and does not implement a listener
 * Performing a send will perform a trigger on other controls with the same name
 */
public class TriggerControlSender extends TriggerControl{
    /**
     * Create a Trigger Dynamic Control
     *
     * @param parent_sketch The parent object for control
     * @param name          the name to display
     */
    public TriggerControlSender(Object parent_sketch, String name) {
        super(parent_sketch, name);
    }

    @Override
    public void triggerEvent() {

    }

    /**
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * We must do this in subclass
     * @param new_scope The new Control Scope
     * @return this object
     */
    public TriggerControlSender setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);
        return this;
    }
}