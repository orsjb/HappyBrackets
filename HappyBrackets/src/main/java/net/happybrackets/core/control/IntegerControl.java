package net.happybrackets.core.control;

public abstract class IntegerControl extends DynamicControlParent {

    /**
     * Constructor for abstract FloatControl. Slider, Text and Buddy Control will
     * derive from this class
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     * @param min_val Minimum value to display on Slider
     * @param max_val Maximum value to display on Slider
     */
    protected IntegerControl(Object parent_sketch, String name, int initial_value, int min_val, int max_val) {
        super(new DynamicControl(parent_sketch, ControlType.INT, name, initial_value, min_val, max_val));
    }


    @Override
    void notifyListener(Object val) {
        valueChanged((int)val);
    }

    /**
     * Get the value for the control
     * @return the control value
     */
    public int getValue(){
        return (int) getDynamicControl().getValue();
    }

    public abstract void valueChanged(int new_value);

    /**
     * set the value for the control. This will notify all the listeners
     * @param val teh value to set to
     */
    public void setValue(int val){
        getDynamicControl().setValue(val);
    }
}
