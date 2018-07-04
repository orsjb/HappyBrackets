package net.happybrackets.core.control;

public abstract class FloatTextControl extends FloatControl {

    /**
     * Constructor
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     */
    public FloatTextControl(Object parent_sketch, String name, double initial_value) {
        super(parent_sketch, name, initial_value, 0, 0);
    }

    /**
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * We must do this in subclass
     * @param new_scope The new Control Scope
     * @return this object
     */
    public FloatTextControl setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);
        return this;

    }
}
