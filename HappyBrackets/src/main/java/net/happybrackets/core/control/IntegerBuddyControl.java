package net.happybrackets.core.control;

import static net.happybrackets.core.control.DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY;

public abstract class IntegerBuddyControl extends IntegerControl {

    /**
     * Constructor
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     * @param min_val Minimum value to display on Slider
     * @param max_val Maximum value to display on Slider
     */
    public IntegerBuddyControl(Object parent_sketch, String name, int initial_value, int min_val, int max_val) {
        /*************************************************************
         * We will use a text control as our base because returning the slider
         * sometimes displays wrong value in GUI when using a pair
         * Text control gives best behaviour when setting via setValue
         ************************************************************/
        super(parent_sketch, name, initial_value, min_val, max_val, DISPLAY_ENABLED_BUDDY);

        // Now connect a slider to the text control
        DynamicControl text_control = this.getDynamicControl().setControlScope(ControlScope.SKETCH);
        text_control.setDisplayType(DISPLAY_ENABLED_BUDDY);

    }

    /**
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * We must do this in subclass
     * @param new_scope The new Control Scope
     * @return this object
     */
    public IntegerBuddyControl setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);
        return this;
    }

    /**
     * Change how to display object
     * We must do this in subclass
     * @param display_type The new Control Scope
     * @return this object
     */
    public IntegerBuddyControl setDisabled(DynamicControl.DISPLAY_TYPE display_type){
        getDynamicControl().setDisplayType(display_type);
        return this;
    }
}
