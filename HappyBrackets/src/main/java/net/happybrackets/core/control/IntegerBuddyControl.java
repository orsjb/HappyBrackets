package net.happybrackets.core.control;

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
        super(parent_sketch, name, initial_value, 0, 0);

        // Now connect a slider to the text control
        DynamicControl text_control = this.getDynamicControl().setControlScope(ControlScope.SKETCH);
        DynamicControl slider_control = new DynamicControl(parent_sketch, ControlType.INT, name, initial_value, min_val, max_val);
        slider_control.setControlScope(ControlScope.SKETCH);

        text_control.addControlScopeListener(new_scope -> {
            slider_control.setControlScope(new_scope);
        });

    }


}
