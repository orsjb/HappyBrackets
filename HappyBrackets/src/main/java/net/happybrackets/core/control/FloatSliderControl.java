package net.happybrackets.core.control;

public abstract class FloatSliderControl extends FloatControl {

    /**
     * Constructor
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     * @param min_val Minimum value to display on Slider
     * @param max_val Maximum value to display on Slider
     */
    public FloatSliderControl(Object parent_sketch, String name, double initial_value, double min_val, double max_val) {
        super(parent_sketch, name, initial_value, min_val, max_val);
    }

    /**
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * We must do this in subclass
     * @param new_scope The new Control Scope
     * @return this object
     */
    public FloatSliderControl setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);
        return this;
    }

    /**
     * Change whether to disable control in display
     * We must do this in subclass
     * @param disabled The new Control Scope
     * @return this object
     */
    public FloatSliderControl setDisabled(boolean disabled){
        getDynamicControl().setDisabled(disabled);
        return this;
    }
}
