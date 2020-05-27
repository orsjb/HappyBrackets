package net.happybrackets.core.control;

/**
 * @deprecated use {@link FloatControl}
 * This class encapsulates the functionality of {@link DynamicControl} classes of type {@link ControlType#FLOAT} in a simple API
 * <br> The difference between {@link FloatControlSender} and other {@link FloatControl} objects  is that @link FloatControlSender} does not have a handler, however, you can still retrieve the current value using the {@link FloatControl#getValue()}
 * <br> All  {@link FloatControl} objects with the same name and {@link ControlScope} will respond to a message send.
 *
 *  * <br> Both {@link FloatControlSender} and {@link FloatTextControl} display the approximate value of the control in the HappyBrackets IDE.
 *  * Within the IDE, the value of the value of the control can be changed by typing a new value in and pressing the ENTER or RETURN key.
 **
 * <br>The {@link FloatControlSender} is identical to the  {@link FloatTextControl} except that {@link FloatControlSender} does not have a {@link FloatControl#valueChanged(double)}) handler
 *
 * <br> <br> See {@link FloatControl} for general information about {@link FloatControl} classes and how to set the value within your code
 */
public class FloatControlSender extends FloatTextControl {
    /**
     * Constructor
     *
     * @param parent_sketch The parent object for control
     * @param name          The name to Display
     * @param initial_value Initial value of the object
     * @deprecated use {@link FloatControl}
     */
    public FloatControlSender(Object parent_sketch, String name, double initial_value) {
        super(parent_sketch, name, initial_value);
    }

    @Override
    public void valueChanged(double control_val) {
        //
    }

    /**
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * If the object is ControlScope Sketch or Unique, it will be displayType in the gui
     * @param new_scope The new Control Scope
     * @return this object
     */
    public FloatTextControl setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);

        // we do not want to be able to change this object in the GUI if noting could be connected to it
        //setDisabled(new_scope == ControlScope.SKETCH || new_scope == ControlScope.UNIQUE);
        return this;
    }
}
