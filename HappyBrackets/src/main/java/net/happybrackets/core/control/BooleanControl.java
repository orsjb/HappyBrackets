package net.happybrackets.core.control;

public abstract class BooleanControl extends DynamicControlParent {

    /**
     * Constructor for abstract FloatControl. Slider, Text and Buddy Control will
     * derive from this class
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     */
    protected BooleanControl(Object parent_sketch, String name, Boolean initial_value) {
        super(new DynamicControl(parent_sketch, ControlType.BOOLEAN, name, initial_value));
    }


    @Override
    void notifyListener(Object val) {
        valueChanged((Boolean)val);
    }

    /**
     * Get the value for the control
     * @return the control value
     */
    public Boolean getValue(){
        return (Boolean) getDynamicControl().getValue();
    }

    public abstract void valueChanged(Boolean control_val);

    /**
     * set the value for the control. This will notify all the listeners
     * @param val teh value to set to
     */
    public void setValue(Boolean val){
        getDynamicControl().setValue(val);
    }

    /**
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * We must do this in subclass
     * @param new_scope The new Control Scope
     * @return this object
     */
    public BooleanControl setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);
        return this;

    }
}
