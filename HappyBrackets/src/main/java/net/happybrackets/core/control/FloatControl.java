package net.happybrackets.core.control;

/**
 * This class encapsulates the functionality of {@link DynamicControl} classes of type {@link ControlType#FLOAT} in a simple API
 * <br> All {@link FloatControl} controls with the same name and {@link ControlScope} will respond to a message send. For example:
 * For example, consider two {@link FloatControl} objects with the same {@link ControlScope} and name
 *
 * <br>When the control receives the value, it will be passed through to the {@link FloatControl#valueChanged(double)} )} listener that is implemented when the class is created.
 * <br>For example
 * <pre>
 FloatControl control1 = new FloatControl(this, "Read", 1.0);
 FloatControl control2 = new FloatControl(this, "Read", 1.0){
 {@literal @}Override
    public void valueChanged(double new_value){
        System.out.println("Read "+ new_value);
    }
 }
 // Setting the value of control1 will cause any objects listening to control1 or control2 to receive the action. EG
 control1.setValue(2.0);  // This will also set the value of control2  </pre>
 reflected in its {@link #valueChanged} function, causing <b>Read 2.0</b> to be printed to standard output.
 <br>
 <br>The control can also schedule messages to be sent at a time in the future by adding the time to the message
 * using an absolute time in the {@link FloatControl#setValue(double, double)}  function. Eg
 * <br> <b>control1.setValue (2.0, HB.getSchedulerTime() + 1000);</b> // this will set control1 value, as well as all other FloatControl with same {@link ControlScope} and name, to true 1 second in the future
 *
 * If you require a handler on the class, use the {@link #valueChanged(double)} class

 */
public class FloatControl extends DynamicControlParent {

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
    public FloatControl(Object parent_sketch, String name, double initial_value, double min_val, double max_val, DynamicControl.DISPLAY_TYPE display_type) {
        super(new DynamicControl(parent_sketch, ControlType.FLOAT, name, initial_value, min_val, max_val, display_type));
    }


    /**
     * Constructor for abstract FloatControl. Slider, Text and Buddy Control will
     * derive from this class
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     */
    public FloatControl(Object parent_sketch, String name, double initial_value) {
        super(new DynamicControl(parent_sketch, ControlType.FLOAT, name, initial_value, 0, 0, DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT));
    }

    @Override
    void notifyListener(Object val) {
        valueChanged((double)val);
    }

    /**
     * Get the value for the control
     <pre>
     FloatControl control1 = new FloatControl(this, "Read", 1.0);

     double val = control1.getValue(); // val  will be 1.0
     control1.setValue(2.0);
     val = control1.getValue(); // val  will be 2.0
     </pre>
     * @return the control value
     */
    public double getValue(){
        return (double) getDynamicControl().getValue();
    }

    /**
     * <br>When the control receives the value, it will be passed through to the {@link FloatControl#valueChanged(double)} listener that is implemented when the class is created.
     * <br>For example
     *
     *  <pre>
     FloatControl control1 = new FloatControl(this, "Read", 1.0);
     FloatControl control2 = new FloatControl(this, "Read", 1.0){
     {@literal @}Override
     <b>public void valueChanged(double new_value){
     System.out.println("Read "+ new_value);
     }</b>
     }
     // Setting the value of control1 will cause any objects listening to control1 or control2 to receive the action. EG
     control1.setValue(2.0);  // This will also set the value of control2  </pre>
     reflected in its {@link #valueChanged} function, causing <b>Read 2.0</b> to be printed to standard output.
     *  <br>
     * @param control_val the value received
     */
    public void valueChanged(double control_val){};

    /**
     * Set the value for the control. This will pass the message on to all other {@link DynamicControl} with matching name, type and {@link ControlScope} and call {@link #valueChanged(double)}.
     *
     * <br> All {@link FloatControl} controls with the same name and {@link ControlScope} will respond to a message send. For example:
     *  * For example, consider two {@link FloatControl} objects with the same {@link ControlScope} and name
     *  <pre>
     FloatControl control1 = new FloatControl(this, "Read", 1.0);
     FloatControl control2 = new FloatControl(this, "Read", 1.0){
       {@literal @}Override
       <b>public void valueChanged(double new_value){
         System.out.println("Read "+ new_value);
       }</b>
     }
     // Setting the value of control1 will cause any objects listening to control1 or control2 to receive the action. EG
     control1.setValue(2.0);  // This will also set the value of control2  </pre>
     reflected in its {@link #valueChanged} function, causing <b>Read 2.0</b> to be printed to standard output.
     * @param val the value to set the control to
     */
    public void setValue(double val){
        getDynamicControl().setValue(val);
    }
    /**
     * Identical to the {@link #setValue(double)} with the exception that the {@link #valueChanged(double)} event will be caused at the {@link net.happybrackets.core.scheduling.HBScheduler} scheduled time passed in.
     * <br>For example, the following code will cause matching {@link DynamicControl} objects to respond 1 second in the future
     *  <pre>
     FloatControl control1 = new FloatControl(this, "Read", 1.0);
     FloatControl control2 = new FloatControl(this, "Read", 1.0){
        {@literal @}Override
        public void valueChanged(double new_value){
            System.out.println("Read "+ new_value);
        }
     }
     // Setting the value of control1 will cause any objects listening to control1 or control2 to receive the action. EG
     <b>control1.setValue(2.0, HB.getSchedulerTime() + 1000);</b>  // This will also set the value of control2  </pre>
     *
     * @param val the value to set to
     * @param scheduler_time the {@link net.happybrackets.core.scheduling.HBScheduler} time this is supposed to occur at
     */
    public void setValue(double val, double scheduler_time){
        getDynamicControl().setValue(val, scheduler_time);
    }

    /**
     * Change how to display object
     * We must do this in subclass
     * @param display_type The new Control Scope
     * @return this object
     */
    public FloatControl setDisplayType(DynamicControl.DISPLAY_TYPE display_type){
        getDynamicControl().setDisplayType(display_type);
        return this;
    }

    /**
     * Changed the {@link ControlScope} the object has has. It will update control map so the correct events will be generated based on its scope
     * @param new_scope The new Control Scope
     * @return this object
     */
    public FloatControl setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);
        return this;
    }

    /**
     * Set how we want this control displayed
     * @param minimum minimum display value on slider
     * @param maximum maximum display value on slider
     * @param display_type the type of display
     * @return this
     */
    public FloatControl setDisplayRange(double minimum, double maximum, DynamicControl.DISPLAY_TYPE display_type){
        getDynamicControl().setMinimumValue(minimum).setMaximumDisplayValue(maximum).setDisplayType(display_type);
        return this;
    }
}
