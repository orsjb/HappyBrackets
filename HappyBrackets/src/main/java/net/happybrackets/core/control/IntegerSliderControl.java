package net.happybrackets.core.control;

public abstract class IntegerSliderControl extends IntegerControl {

    /**
     * Constructor
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     * @param min_val Minimum value to display on Slider
     * @param max_val Maximum value to display on Slider
     */
    public IntegerSliderControl(Object parent_sketch, String name, int initial_value, int min_val, int max_val) {
        super(parent_sketch, name, initial_value, min_val, max_val, DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);
    }


    /**
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * We must do this in subclass
     * @param new_scope The new Control Scope
     * @return this object
     */
    public IntegerSliderControl setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);
        return this;
    }

    /**
     * Change how to display object
     * We must do this in subclass
     * @param display_type The new Control Scope
     * @return this object
     */
    public IntegerSliderControl setDisplayType(DynamicControl.DISPLAY_TYPE display_type){
        getDynamicControl().setDisplayType(display_type);
        return this;
    }
}
