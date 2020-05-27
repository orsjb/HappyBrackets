package net.happybrackets.core.control;

/**
 * @deprecated use {@link BooleanControl} instead
 * This class encapsulates the functionality of {@link DynamicControl} classes of type {@link ControlType#BOOLEAN} in a simple API.
 * <br> The difference between {@link BooleanControlSender} and {@link BooleanControl} is that {@link BooleanControlSender} does not have a {@link BooleanControl#valueChanged(Boolean)} handler, however, you can still retrieve the current value using the {@link BooleanControl#getValue()}
 * <br><br> See {@link BooleanControl} for more detail.
 */
public class BooleanControlSender extends BooleanControl {
    /**
     * Constructor for abstract FloatControl. Slider, Text and Buddy Control will
     * derive from this class
     *
     * @param parent_sketch The parent object for control
     * @param name          The name to Display
     * @param initial_value Initial value of the object
     * @deprecated use {@link BooleanControl} instead
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
