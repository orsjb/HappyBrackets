package net.happybrackets.core.control;

/**
 * @deprecated use {@link IntegerControl} instead
 * This class has been deprecated. Use {@link IntegerControl} instead
 * This class encapsulates the functionality of {@link DynamicControl} classes of type {@link ControlType#INT} in a simple API
 * <br> The difference between {@link IntegerControlSender} and other {@link IntegerControl} objects  is that @link IntegerControlSender} does not have a handler, however, you can still retrieve the current value using the {@link IntegerControl#getValue()}
 * <br> All  {@link IntegerControl} objects with the same name and {@link ControlScope} will respond to a message send. For example:
 * <br> Both {@link IntegerControlSender} and {@link IntegerTextControl} display the approximate value of the control in the HappyBrackets IDE.
 * Within the IDE, the value of the value of the control can be changed by typing a new value in and pressing the ENTER or RETURN key.
 * <br>
 *
 * <br>The {@link IntegerControlSender} is identical to the  {@link IntegerTextControl} except that {@link IntegerControlSender} does not have a {@link IntegerControl#valueChanged(int)}) handler
 *
 * <br> See {@link IntegerControl} for general information about {@link IntegerControl} classes and how to set the value within your code
 */
public abstract class IntegerTextControl extends IntegerControl {

    /**
     * Constructor
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     * @deprecated use {@link IntegerControl} instead
     */
    public IntegerTextControl(Object parent_sketch, String name, int initial_value) {
        super(parent_sketch, name, initial_value, 0, 0, DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);
    }

    /**
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * We must do this in subclass
     * @param new_scope The new Control Scope
     * @return this object
     */
    public IntegerTextControl setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);
        return this;
    }

    /**
     * Change how to display object
     * We must do this in subclass
     * @param display_type The new Control Scope
     * @return this object
     */
    public IntegerTextControl setDisplayType(DynamicControl.DISPLAY_TYPE display_type){
        getDynamicControl().setDisplayType(display_type);
        return this;
    }
}
