package net.happybrackets.core.control;

public abstract class IntegerTextControl extends IntegerControl {

    /**
     * Constructor
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     */
    public IntegerTextControl(Object parent_sketch, String name, int initial_value) {
        super(parent_sketch, name, initial_value, 0, 0);
    }

}
