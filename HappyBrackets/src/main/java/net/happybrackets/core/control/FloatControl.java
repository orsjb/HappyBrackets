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
     */
    protected FloatControl(Object parent_sketch, String name, double initial_value, double min_val, double max_val) {
        super(new DynamicControl(parent_sketch, ControlType.FLOAT, name, initial_value, min_val, max_val));
    }


    @Override
    void notifyListener(Object val) {
        Float f_val =  (float)val;

        // we need to do this change becuase rounding down to a float does not give accutate value
        double d_val =  new BigDecimal(f_val.toString()).doubleValue();

        valueChanged(d_val);
    }

    /**
     * Get the value for the control
     * @return the control value
     */
    public double getValue(){
        return (float) getDynamicControl().getValue();
    }

    public abstract void valueChanged(double control_val);

    /**
     * set the value for the control. This will notify all the listeners
     * @param val the value to set to
     */
    public void setValue(double val){
        getDynamicControl().setValue(val);
    }
}
