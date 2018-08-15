package net.happybrackets.core.control;

public abstract class FloatBuddyControl extends FloatControl {

    /**
     * Constructor
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     * @param min_val Minimum value to display on Slider
     * @param max_val Maximum value to display on Slider
     */
    public FloatBuddyControl(Object parent_sketch, String name, double initial_value, double min_val, double max_val) {
        /*************************************************************
         * We will use a text control as our base because returning the slider
         * sometimes displays wrong value in GUI when using a pair
         * Text control gives best behaviour when setting via setValue
         ************************************************************/
        super(parent_sketch, name, initial_value, 0, 0);

        // Now connect a slider to the text control
        DynamicControl text_control = this.getDynamicControl().setControlScope(ControlScope.SKETCH);
        DynamicControl slider_control = new DynamicControl(parent_sketch, ControlType.FLOAT, name, initial_value, min_val, max_val);
        slider_control.setControlScope(ControlScope.SKETCH);

        text_control.addControlScopeListener(new_scope -> {
            slider_control.setControlScope(new_scope);
        });

    }

    /**
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * We must do this in subclass
     * @param new_scope The new Control Scope
     * @return this object
     */
    public FloatBuddyControl setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);
        return this;
    }

    /**
     * Change whether to disable control in display
     * We must do this in subclass
     * @param disabled The new Control Scope
     * @return this object
     */
    public FloatBuddyControl setDisabled(boolean disabled){
        getDynamicControl().setDisabled(disabled);
        return this;
    }

}
