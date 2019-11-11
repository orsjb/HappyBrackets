package net.happybrackets.core.control;

/**
 * This class encapsulates the functionality of {@link DynamicControl} classes of type {@link ControlType#INT} in a simple API
 * <br> The difference between {@link IntegerControlSender} and other {@link IntegerControl} objects  is that @link IntegerControlSender} does not have a handler, however, you can still retrieve the current value using the {@link IntegerControl#getValue()}
 * <br> All  {@link IntegerControl} objects with the same name and {@link ControlScope} will respond to a message send.
 *
 *  * <br> Both {@link IntegerControlSender} and {@link IntegerTextControl} display the approximate value of the control in the HappyBrackets IDE.
 *  * Within the IDE, the value of the value of the control can be changed by typing a new value in and pressing the ENTER or RETURN key.
 **
 * <br>The {@link IntegerControlSender} is identical to the  {@link IntegerTextControl} except that {@link IntegerControlSender} does not have a {@link IntegerControl#valueChanged(int)}) handler
 *
 * <br> <br> See {@link IntegerControl} for general information about {@link IntegerControl} classes and how to set the value within your code
 */
public class IntegerControlSender extends IntegerTextControl {
    /**
     * Constructor
     *
     * @param parent_sketch The parent object for control
     * @param name          The name to Display
     * @param initial_value Initial value of the object
     */
    public IntegerControlSender(Object parent_sketch, String name, int initial_value) {
        super(parent_sketch, name, initial_value);
    }

    @Override
    public void valueChanged(int control_val) {

    }

    /**
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * If the object is ControlScope Sketch or Unique, it will be displayType in the gui
     * @param new_scope The new Control Scope
     * @return this object
     */
    public IntegerControlSender setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);

        // we do not want to be able to change this object in the GUI if noting could be connected to it
        //setDisabled(new_scope == ControlScope.SKETCH || new_scope == ControlScope.UNIQUE);
        return this;
    }
}
