package net.happybrackets.core.control;

/**
 * This class encapsulates the functionality of {@link DynamicControl} classes of type {@link ControlType#INT} in a simple API
 * <br><br> All {@link IntegerControl} controls with the same name and {@link ControlScope} will respond to a message send. For example:
 * For example, consider two IntegerControls with the same {@link ControlScope} and name
 *
 * Setting the value of control1 will cause any objects listening to control1 or control2 to receive the action. EG
 **
 * <br><br>When the control receives the value, it will be passed through to the {@link IntegerControl#valueChanged(int)} )} listener that is implemented when the class is created.
 * <br>For example:
<pre>
 IntegerControl control1 = new IntegerControl(this, "Read", 0);

 IntegerControl control2 = new IntegerControl(this, "Read", 0) {
    {@literal @}Override
    public void valueChanged(int control_val) {
        System.out.println("Read " + control_val);
    }
};

 control1.setValue(2); // this will also set the value of control2
 </pre>
 <br>Will cause the <b>{@link #valueChanged(int)}</b> function to be called with the new value, causing <b>Read 2</b> to be printed to standard output. This is
 because both controls are {@link ControlScope#SKETCH} by default and are both have their name as <b>"Read</b>.
 * <br><br>The control can also schedule messages to be sent at a time in the future by adding the time to the message
 * using an absolute time in the {@link IntegerControl#setValue(int, double)}  function. Eg
 * <br> <b>control1.setValue (2, HB.getSchedulerTime() + 1000);</b> // this will set control1 value, as well as all other IntegerControl with same {@link ControlScope} and name, to true 1 second in the future
 *
 <br>
 * You can also set the value from within the HappyBrackets plugin IDE.
 *
 * <br>Additionally, you can get current value by calling the {@link IntegerControl#getValue()} function.
 * If you  require a handler on the class, override the {@link #valueChanged(int)} function
 */
public class IntegerControl extends DynamicControlParent {

    /**
     * Constructor for IntegerControl if you want it displayed other than text control.
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     * @param min_val Minimum value to display on Slider
     * @param max_val Maximum value to display on Slider
     * @param display_type The way we want object displayed
     */
    public IntegerControl(Object parent_sketch, String name, int initial_value, int min_val, int max_val, DynamicControl.DISPLAY_TYPE display_type) {
        super(new DynamicControl(parent_sketch, ControlType.INT, name, initial_value, min_val, max_val, display_type));
    }

    /**
     * Constructor for abstract IntegerControl. This allows creation without a display
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     */
    public IntegerControl(Object parent_sketch, String name, int initial_value) {
        super(new DynamicControl(parent_sketch, ControlType.INT, name, initial_value, 0, 0, DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT));
    }



    @Override
    void notifyListener(Object val) {
        valueChanged((int)val);
    }

    /**
     * Get the value for the control.
     <pre>
IntegerControl control1 = new IntegerControl(this, "Read", 0);

IntegerControl control2 = new IntegerControl(this, "Read", 0) {
     {@literal @}Override
    public void valueChanged(int control_val) {
        System.out.println("Read " + control_val);
    }
};

int val = <b>control1.getValue();</b> // val = 0
control1.setValue(2);
val = <b>control1.getValue();</b>// val = 1
     </pre>
     * @return the control value
     */
    public int getValue(){
        return (int) getDynamicControl().getValue();
    }

    /**
     * <br>When the control receives the value, it will be passed through to the {@link #valueChanged(int)} listener that is implemented when the class is created.
     * <br>For example
     *
<pre>
IntegerControl control1 = new IntegerControl(this, "Read", 0);

IntegerControl control2 = new IntegerControl(this, "Read", 0) {
    {@literal @}Override
    <b>public void valueChanged(int control_val) {
        System.out.println("Read " + control_val);
    }</b>
};

control1.setValue(2); // this will also set the value of control2

</pre>
     <br>Will cause the <b>{@link #valueChanged(int)}</b> function to be called with the new value, causing <b>Read 2</b> to be printed to standard output
     * @param control_val the value received
     */
    public void valueChanged(int control_val){};

    /**
     * set the value for the control. This will pass the message on to all other {@link DynamicControl} with matching name, type and {@link ControlScope} and call {@link #valueChanged(int)}.
 <pre>
 IntegerControl control1 = new IntegerControl(this, "Read", 0);

 IntegerControl control2 = new IntegerControl(this, "Read", 0) {
    {@literal @}Override
    public void valueChanged(int control_val) {
        System.out.println("Read " + control_val);
    }
 };

 <b>control1.setValue(2);</b> // this will also set the value of control2
     </pre>
     <br>Will cause the <b>{@link #valueChanged(int)}</b> function to be called with the new value, causing <b>Read 2</b> to be printed to standard output
     * @param val the value to set to
     */
    public void setValue(int val){
        getDynamicControl().setValue(val);
    }

    /**
     * set the value for the control at a specific time.
     * Identical to the {@link #setValue(int)} with the exception that the {@link #valueChanged(int)} event will be caused at the {@link net.happybrackets.core.scheduling.HBScheduler} scheduled time passed in.
     * <br>For example, the following code will cause matching {@link DynamicControl} objects to respond 1 second in the future
     <pre>
     IntegerControl control1 = new IntegerControl(this, "Read", 0);

     IntegerControl control2 = new IntegerControl(this, "Read", 0) {
        {@literal @}Override
        public void valueChanged(int control_val) {
        System.out.println("Read " + control_val);
        }
     };

     <b>control1.setValue(2, HB.getSchedulerTime() + 1000); </b>// this will also set the value of control2
     </pre>

     *
     * @param val the value to set to
     * @param scheduler_time the scheduler time this is supposed to occur at
     */
    public void setValue(int val, double scheduler_time){
        getDynamicControl().setValue(val, scheduler_time);
    }

    /**
     * Change how to display object
     * We must do this in subclass
     * @param display_type The new Control Scope
     * @return this object
     */
    public IntegerControl setDisplayType(DynamicControl.DISPLAY_TYPE display_type){
        getDynamicControl().setDisplayType(display_type);
        return this;
    }

    /**
     * Changed the {@link ControlScope} the object has has. It will update control map so the correct events will be generated based on its scope
     * @param new_scope The new Control Scope
     * @return this object
     */
    public IntegerControl setControlScope(ControlScope new_scope){
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
    public IntegerControl setDisplayRange(int minimum, int maximum, DynamicControl.DISPLAY_TYPE display_type){
        getDynamicControl().setMinimumValue(minimum).setMaximumDisplayValue(maximum).setDisplayType(display_type);
        return this;
    }
}
