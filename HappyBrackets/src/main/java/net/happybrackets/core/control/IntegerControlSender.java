package net.happybrackets.core.control;

/**
 * This class is used for only sending values and does not implement a listener
 * Writing to the value will send the value to other controls with the same name
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
