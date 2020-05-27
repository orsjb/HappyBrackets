package net.happybrackets.core.control;

/**
 * This class encapsulates the functionality of {@link DynamicControl} classes of type {@link ControlType#BOOLEAN} in a simple API.
 * <br>
 * <br> All {@link BooleanControl} objects with the same name and {@link ControlScope} will respond to a {@link BooleanControl#setValue(Boolean)}.
 * For example, consider two BooleanControls with the same {@link ControlScope} and name
 *
<pre>
 BooleanControl control1 = new BooleanControl(this, "Read", false);
 BooleanControl control2 = new BooleanControl(this, "Read", false) {
    {@literal @}Override
    public void valueChanged(Boolean control_val) {
        System.out.println("Read " + control_val);
    }
};// End DynamicControl control2 code

 control1.setValue(true); // Setting control1 will also set control2
 </pre>
 <br>Will cause the {@link #valueChanged(Boolean)} function to be called with the new value, causing <b>Read true</b> to be printed to standard output

 * <br>
 * <br>The control can also schedule messages to be sent at a time in the future by adding the time to the message
 * using an absolute time in the {@link BooleanControl#setValue(Boolean, double)}  function. Eg: <br>
 * <br> <b>control1.setValue (true, HB.getSchedulerTime() + 1000);</b> // this will set control1 value, as well as all other BooleanControls with same {@link ControlScope} and name, to true 1 second in the future
 * <br>
 <br><br>Setting the value within the HappyBrackets controls display is effected using a checkbox, where a checked value is true.
 <br> It is possible to get current value using the {@link #getValue()}

 * If you  require a handler on the class, override {@link BooleanControl#valueChanged(Boolean)} function
 *
 */
public class BooleanControl extends DynamicControlParent {

    /**
     * Constructor for abstract FloatControl. Slider, Text and Buddy Control will
     * derive from this class
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     */
    public BooleanControl(Object parent_sketch, String name, Boolean initial_value) {
        super(new DynamicControl(parent_sketch, ControlType.BOOLEAN, name, initial_value));
    }


    @Override
    void notifyListener(Object val) {
        valueChanged((Boolean)val);
    }

    /**
     * Get the value for the control
     <pre>
     BooleanControl control1 = new BooleanControl(this, "Read", false);

     boolean val = <b>control1.getValue();</b> // val will be false
     control1.setValue(true);
     val = <b>control1.getValue();</b> // val will be true
     </pre>
     * @return the control value
     */
    public Boolean getValue(){
        return (Boolean) getDynamicControl().getValue();
    }

    /**
     * Fired event that occurs when the value for the control has been set. This will pass the message on to all other {@link DynamicControl} with matching name, type and {@link ControlScope} and call {@link #valueChanged(Boolean)}.
     * The function must be implemented when creating objects
     <pre>
     BooleanControl control1 = new BooleanControl(this, "Read", false);
     BooleanControl control2 = new BooleanControl(this, "Read", false) {
       {@literal @}Override
        <b>public void valueChanged(Boolean control_val) {
            System.out.println("Read " + control_val);
        }</b>
     };// End DynamicControl control2 code

     control1.setValue(true);// Setting control1 will also set control2
     </pre>
     <br>Will cause the {@link #valueChanged(Boolean)} function to be called with the new value, causing <b>Read true</b> to be printed to standard output
     *
     *
     * @param control_val The new value of the control
     */
    public void valueChanged(Boolean control_val){};

    /**
     * set the value for the control. This will notify all the listeners with same name and {@link ControlScope}. For example
     *
     <pre>
     BooleanControl control1 = new BooleanControl(this, "Read", false);
     BooleanControl control2 = new BooleanControl(this, "Read", false) {
        {@literal @}Override
        public void valueChanged(Boolean control_val) {
            System.out.println("Read " + control_val);
        }
     };// End DynamicControl control2 code

     <b>control1.setValue(true);</b> // Setting control1 will also set control2
     </pre>
     <br>Will cause the {@link #valueChanged(Boolean)} function to be called with the new value, causing <b>Read true</b> to be printed to standard output

     * <br>
     * <br>The control can also schedule messages to be sent at a time in the future by adding the time to the message
     * using an absolute time in the {@link BooleanControl#setValue(Boolean, double)}  function.
     * @param val the value to set to
     */
    public void setValue(Boolean val){
        getDynamicControl().setValue(val);
    }

    /**
     Identical to the {@link #setValue(Boolean)} with the exception that the {@link #valueChanged(Boolean)} event will be caused at the {@link net.happybrackets.core.scheduling.HBScheduler} scheduled time passed in.
     * <br>For example, the following code will cause matching {@link DynamicControl} objects to respond 1 second in the future
     * <br>
     <pre>
     BooleanControl control1 = new BooleanControl(this, "Read", false);
     BooleanControl control2 = new BooleanControl(this, "Read", false) {
        {@literal @}Override
            public void valueChanged(Boolean control_val) {
            System.out.println("Read " + control_val);
        }
     };// End DynamicControl control2 code

     <b>control1.setValue(true, HB.getSchedulerTime() + 1000);</b> // Setting control1 will also set control2
     </pre>
     <br>Will cause the {@link #valueChanged(Boolean)} function to be called with the new value one second in the future, causing <b>Read true</b> to be printed to standard output one second in the future

     *
     * @param val the value to set to
     * @param scheduler_time the scheduler time this is supposed to occur at
     */
    public void setValue(Boolean val, double scheduler_time){
        getDynamicControl().setValue(val, scheduler_time);
    }


    /**
     * Changed the {@link ControlScope} the object has has. It will update control map so the correct events will be generated based on its scope
     * @param new_scope The new Control Scope
     * @return this object
     */
    public BooleanControl setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);
        return this;
    }

    /**
     * Change how to display object as {@link net.happybrackets.core.control.DynamicControl.DISPLAY_TYPE}
     * @param display_type The new Control Scope
     * @return this object
     */
    public BooleanControl setDisplayType(DynamicControl.DISPLAY_TYPE display_type){
        getDynamicControl().setDisplayType(display_type);
        return this;
    }

}
