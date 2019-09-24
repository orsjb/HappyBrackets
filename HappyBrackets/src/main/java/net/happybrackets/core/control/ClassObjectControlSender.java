package net.happybrackets.core.control;

/**
 * A class for sending class objects as dynamic control messages.
 */
public class ClassObjectControlSender extends ClassObjectControl {

    /**
     * Constructor for ClassObjectControlSender
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     */
    public ClassObjectControlSender(Object parent_sketch, String name, Object initial_value) {
        super(parent_sketch, name, initial_value);
    }

    @Override
    public void valueChanged(Object control_val) {

    }

    /**
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * We must do this in subclass
     * @param new_scope The new Control Scope
     * @return this object
     */
    public ClassObjectControlSender setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);
        return this;
    }

    /**
     * Change how to display object
     * We must do this in subclass
     * @param display_type The new Control Scope
     * @return this object
     */
    public ClassObjectControlSender setDisplayType(DynamicControl.DISPLAY_TYPE display_type){
        getDynamicControl().setDisplayType(display_type);
        return this;
    }
}
