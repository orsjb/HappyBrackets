package net.happybrackets.core.control;

/**
 * This class encapsulates the functionality of {@link DynamicControl} classes of type {@link ControlType#TEXT} in a simple API
 * <br> All Text controls with the same name and {@link ControlScope} will respond to a message send. For example:
 * For example, consider two TextControls with the same {@link ControlScope} and name
 *
 * <br> <b>TextControl control1 = new TextControl(this, "Read", "Hello")</b>....
 * <br> <b>TextControl control2 = new TextControl(this, "Read", "Hello")</b>....
 *
 * Setting the value of control1 will cause any objects listening to control1 or control2 to receive the action. EG
 *
 * <b>control1.setValue("goodbye");</b>  // This will also set the value of control2
 *
 * <br>The control can also schedule messages to be sent at a time in the future by adding the time to the message
 * using an absolute time in the {@link TextControl#setValue(String, double)}  function. Eg
 * <br> <b>control1.setValue ("Hi There", HB.getSchedulerTime() + 1000);</b> // this will set control1 value, as well as all other TextControls with same {@link ControlScope} and name, to true 1 second in the future
 *
 * <br>When the control receives the value, it will be passed through to the {@link TextControl#valueChanged(String)} listener that is implemented when the class is created.
 * <br>For example
 * <b>
 * <br>TextControl myControl = new TextControl(this,"My Control",""){
 <br>&emsp@Override
 <br>&emsp&emsp public void valueChanged(String new_value){
 <br>&emsp&emsp&emsp System.out.println("Read "+new_value);
 <br>&emsp&emsp }
 <br>&emsp };

 <br><br>
 myControl.setValue("This is text");
 </b>
 <br>Will cause the <b>valueChanged</b> function to be called with the new value, causing <b>Read This is text</b> to be printed to standard output
 <br>Setting the value within the HappyBrackets control display is effected by typing a new value in and pressing ENTER or RETURN

 * If you do not require a handler on the class, use the {@link TextControlSender} class
 */
public abstract class TextControl extends DynamicControlParent {

    /**
     * Constructor for abstract FloatControl. Slider, Text and Buddy Control will
     * derive from this class
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     */
    protected TextControl(Object parent_sketch, String name, String initial_value) {
        super(new DynamicControl(parent_sketch, ControlType.TEXT, name, initial_value));
    }


    @Override
    void notifyListener(Object val) {
        valueChanged((String)val);
    }

    /**
     * Get the value for the control
     * @return the control value
     */
    public String getValue(){
        return (String) getDynamicControl().getValue();
    }

    public abstract void valueChanged(String control_val);

    /**
     * set the value for the control. This will notify all the listeners
     * @param val the value to set to
     */
    public void setValue(String val){
        getDynamicControl().setValue(val);
    }

    /**
     * set the value for the control. This will notify all the listeners
     * @param val the value to set to
     * @param scheduler_time the scheduler time this is supposed to occur at
     */
    public void setValue(String val, double scheduler_time){
        getDynamicControl().setValue(val, scheduler_time);
    }

    /**
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * We must do this in subclass
     * @param new_scope The new Control Scope
     * @return this object
     */
    public TextControl setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);
        return this;
    }

    /**
     * Change how to display object
     * We must do this in subclass
     * @param display_type The new Control Scope
     * @return this object
     */
    public TextControl setDisplayType(DynamicControl.DISPLAY_TYPE display_type){
        getDynamicControl().setDisplayType(display_type);
        return this;
    }
}
