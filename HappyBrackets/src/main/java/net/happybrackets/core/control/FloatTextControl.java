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

}
