package net.happybrackets.core.control;

import java.math.BigDecimal;

/**
 * This class encapsulates the functionality of {@link DynamicControl} classes of type {@link ControlType#FLOAT} in a simple API
 * <br> This class is the parent type of {@link FloatTextControl}, {@link FloatSliderControl},  {@link FloatBuddyControl} and  {@link FloatControlSender} and all intercommunicate with each other
 * <br> All {@link FloatControl} controls with the same name and {@link ControlScope} will respond to a message send. For example:
 * For example, consider two BooleanControls with the same {@link ControlScope} and name
 *
 * <br> <b>FloatControl control1 = new FloatControlSender(this, "Read", 1.0);</b>
 * <br> <b>FloatControl control2 = new FloatTextControl(this, "Read", 1.0)</b>....
 *
 * Setting the value of control1 will cause any objects listening to control1 or control2 to receive the action. EG
 *
 * <b>control1.setValue(3.6);</b>  // This will also set the value of control2
 *
 * <br>The control can also schedule messages to be sent at a time in the future by adding the time to the message
 * using an absolute time in the {@link FloatControl#setValue(double, double)}  function. Eg
 * <br> <b>control1.setValue (2.0, HB.getSchedulerTime() + 1000);</b> // this will set control1 value, as well as all other FloatControl with same {@link ControlScope} and name, to true 1 second in the future
 *
 * <br>When the control receives the value, it will be passed through to the {@link FloatControl#valueChanged(double)} )} listener that is implemented when the class is created.
 * <br>For example
 * <b>
 * <br>FloatControl myControl = new FloatTextControl(this,"My Control",1.0){
 <br>&emsp;@Override
 <br>&emsp;&emsp;public void valueChanged(double new_value){
 <br>&emsp;&emsp;&emsp;System.out.println("Read "+ new_value);
 <br>&emsp;&emsp;}
 <br>&emsp;};

 <br><br>
 myControl.setValue(2.0);
 </b>
 <br>Will cause the <b>valueChanged</b> function to be called with the new value, causing <b>Read 2.0</b> to be printed to standard output
 <br>
 * If you do not require a handler on the class, use the {@link FloatControlSender} class
 * <br> See {@link FloatTextControl}, {@link FloatSliderControl},  {@link FloatBuddyControl} and  {@link FloatControlSender} for specifics of each specialisation of
 */
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
