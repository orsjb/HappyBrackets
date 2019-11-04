package net.happybrackets.core.control;

import java.math.BigDecimal;

public abstract class FloatControl extends DynamicControlParent {

    /**
     * Constructor for abstract FloatControl. Slider, Text and Buddy Control will
     * derive from this class
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     * @param min_val Minimum value to display on Slider
     * @param max_val Maximum value to display on Slider
     * @param display_type The way we want object displayed
     */
    protected FloatControl(Object parent_sketch, String name, double initial_value, double min_val, double max_val, DynamicControl.DISPLAY_TYPE display_type) {
        super(new DynamicControl(parent_sketch, ControlType.FLOAT, name, initial_value, min_val, max_val, display_type));
    }


    @Override
    void notifyListener(Object val) {
        valueChanged((double)val);
    }

    /**
     * Get the value for the control
     * @return the control value
     */
    public double getValue(){
        return (double) getDynamicControl().getValue();
    }

    public abstract void valueChanged(double control_val);

    /**
     * set the value for the control. This will notify all the listeners
     * @param val the value to set to
     */
    public void setValue(double val){
        getDynamicControl().setValue(val);
    }
    /**
     * set the value for the control. This will notify all the listeners
     * @param val the value to set to
     * @param scheduler_time the scheduler time this is supposed to occur at
     */
    public void setValue(double val, double scheduler_time){
        getDynamicControl().setValue(val, scheduler_time);
    }
}
