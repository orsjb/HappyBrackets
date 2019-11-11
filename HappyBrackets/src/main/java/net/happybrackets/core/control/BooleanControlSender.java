package net.happybrackets.core.control;

/**
 * This class encapsulates the functionality of {@link DynamicControl} classes of type {@link ControlType#BOOLEAN} in a simple API
 * <br> The difference between {@link BooleanControlSender} and {@link BooleanControl} is that @link BooleanControlSender} does not have a handler, however, you can still retrieve the current value using the {@link BooleanControl#getValue()}
 * <br> All boolean controls with the same name and {@link ControlScope} will respond to a message send. For example:
 * For example, consider two BooleanControls with the same {@link ControlScope} and name
 *
 * <br> <b>BooleanControl control1 = new BooleanControlSender(this, "Read", false);</b>
 * <br> <b>BooleanControl control2 = new BooleanControl(this, "Read", false)</b>....
 *
 * Setting the value of control1 will cause any objects listening to control1 or control2 to receive the action. EG
 *
 * <b>control1.setValue(true);</b>  // This will also set the value of control2
 * <br>Setting the value within the HappyBrackets control display is effected using a checkbox, where a checked value is true
 * <br>This class is used for only sending values and does not implement a listener
 * Writing to the value will send the value to the controls with the same name. See {@link BooleanControl} for more detail.
 *
 */
public class BooleanControlSender extends BooleanControl {
    /**
     * Constructor for abstract FloatControl. Slider, Text and Buddy Control will
     * derive from this class
     *
     * @param parent_sketch The parent object for control
     * @param name          The name to Display
     * @param initial_value Initial value of the object
     */
    public BooleanControlSender(Object parent_sketch, String name, Boolean initial_value) {
        super(parent_sketch, name, initial_value);
    }

    @Override
    public void valueChanged(Boolean control_val) {

    }
    /**
     * Changed the  {@link ControlScope} that the control has. It will update control map so the correct events will be generated based on its scope
     * @param new_scope The new  {@link ControlScope}
     * @return this object
     */
    public BooleanControlSender setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);
        return this;
    }
}
