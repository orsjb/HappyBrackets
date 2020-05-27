package net.happybrackets.core.control;

/**
 * @deprecated use  {@link FloatControl#FloatControl(Object, String, double, double, double, DynamicControl.DISPLAY_TYPE)}
 * This class encapsulates the functionality of {@link DynamicControl} classes of type {@link ControlType#FLOAT} in a simple API
 * <br>The {@link FloatSliderControl} can have its value set inside the HappyBrackets IDE using a standard slider control controller by the mouse. trackpad or arrow keys
 * <br>The maxmimum and minimum values that the control can be set to using the slider are defined when creating the control via {@link FloatSliderControl#FloatSliderControl(Object, String, double, double, double)}
 * <br>The upper and lower limits, however, are only limitations on the display and the actual control can be set to any value available to other {@link FloatControl} objects
 * <br> All  {@link FloatControl} objects with the same name and {@link ControlScope} will respond to a value change.
 *
 *
 * <br> <br> See {@link FloatControl} for general information about {@link FloatControl} classes and how to set the value within your code
 */
public abstract class FloatSliderControl extends FloatControl {

    /**
     * Constructor
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     * @param min_val Minimum value to display on Slider
     * @param max_val Maximum value to display on Slider
     * @deprecated use  {@link FloatControl#FloatControl(Object, String, double, double, double, DynamicControl.DISPLAY_TYPE)} with {@link DynamicControl.DISPLAY_TYPE#DISPLAY_DEFAULT}
     */
    public FloatSliderControl(Object parent_sketch, String name, double initial_value, double min_val, double max_val) {
        super(parent_sketch, name, initial_value, min_val, max_val, DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);
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
     * Change how to display object
     * We must do this in subclass
     * @param display_type The new Control Scope
     * @return this object
     */
    public FloatSliderControl setDisplayType(DynamicControl.DISPLAY_TYPE display_type){
        getDynamicControl().setDisplayType(display_type);
        return this;
    }
}
