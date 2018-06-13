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
        super(parent_sketch, name, initial_value, min_val, max_val);
    }


}
