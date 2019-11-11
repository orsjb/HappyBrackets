package net.happybrackets.core.control;

/**
 * This class encapsulates the functionality of {@link DynamicControl} classes of type {@link ControlType#INT} in a simple API
 * <br> This class is the parent type of {@link IntegerTextControl}, {@link IntegerSliderControl},  {@link IntegerBuddyControl} and  {@link IntegerControlSender} and all intercommunicate with each other
 * <br> All {@link IntegerControl} controls with the same name and {@link ControlScope} will respond to a message send. For example:
 * For example, consider two BooleanControls with the same {@link ControlScope} and name
 *
 * <br> <b>IntegerControl control1 = new IntegerControlSender(this, "Read", 1);</b>
 * <br> <b>IntegerControl control2 = new IntegerTextControl(this, "Read", 1)</b>....
 *
 * Setting the value of control1 will cause any objects listening to control1 or control2 to receive the action. EG
 *
 * <b>control1.setValue(3);</b>  // This will also set the value of control2
 *
 * <br>The control can also schedule messages to be sent at a time in the future by adding the time to the message
 * using an absolute time in the {@link IntegerControl#setValue(int, double)}  function. Eg
 * <br> <b>control1.setValue (2, HB.getSchedulerTime() + 1000);</b> // this will set control1 value, as well as all other IntegerControl with same {@link ControlScope} and name, to true 1 second in the future
 *
 * <br>When the control receives the value, it will be passed through to the {@link IntegerControl#valueChanged(int)} )} listener that is implemented when the class is created.
 * <br>For example
 * <b>
 * <br>IntegerControl myControl = new IntegerTextControl(this,"My Control",1){
 <br>&emsp@Override
 <br>&emsp&emsp public void valueChanged(int new_value){
 <br>&emsp&emsp&emsp System.out.println("Read "+ new_value);
 <br>&emsp&emsp }
 <br>&emsp };

 <br><br>
 myControl.setValue(2);
 </b>
 <br>Will cause the <b>valueChanged</b> function to be called with the new value, causing <b>Read 2</b> to be printed to standard output
 <br>
 * You can also set the value from within the HappyBrackets plugin IDE. See {@link IntegerTextControl}, {@link IntegerSliderControl},  {@link IntegerBuddyControl} and  {@link IntegerControlSender} for the specifics of each type
 *
 * <br>Additionally, you can get current value by calling the {@link IntegerControl#getValue()} function.
 * If you do not require a handler on the class, use the {@link IntegerControlSender} class
 * <br> See {@link IntegerTextControl}, {@link IntegerSliderControl},  {@link IntegerBuddyControl} and  {@link IntegerControlSender} for specifics of each specialisation of
 */
public abstract class IntegerControl extends DynamicControlParent {

    /**
     * Constructor for abstract IntegerControl. Slider, Text and Buddy Control will
     * derive from this class
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     * @param min_val Minimum value to display on Slider
     * @param max_val Maximum value to display on Slider
     * @param display_type The way we want object displayed
     */
    protected IntegerControl(Object parent_sketch, String name, int initial_value, int min_val, int max_val, DynamicControl.DISPLAY_TYPE display_type) {
        super(new DynamicControl(parent_sketch, ControlType.INT, name, initial_value, min_val, max_val, display_type));
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

    public abstract void valueChanged(int control_val);

    /**
     * set the value for the control. This will notify all the listeners
     * @param val teh value to set to
     */
    public void setValue(int val){
        getDynamicControl().setValue(val);
    }

    /**
     * set the value for the control. This will notify all the listeners
     * @param val the value to set to
     * @param scheduler_time the scheduler time this is supposed to occur at
     */
    public void setValue(int val, double scheduler_time){
        getDynamicControl().setValue(val, scheduler_time);
    }
}
