package net.happybrackets.core.control;

import static net.happybrackets.core.control.DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY;

/**
 * @deprecated use {@link IntegerControl} instead with {@link DynamicControl.DISPLAY_TYPE#DISPLAY_ENABLED_BUDDY}
 * This class encapsulates the functionality of {@link DynamicControl} classes of type {@link ControlType#INT} in a simple API
 * <br>The {@link IntegerBuddyControl} is a hybrid between the {@link IntegerSliderControl} and the {@link IntegerTextControl} in that the value can be changed in the HappyBrackets IDE with both text and a slider
 * <br>The maxmimum and minimum values that the control can be set to using the slider are defined when creating the control via {@link IntegerBuddyControl#IntegerBuddyControl(Object, String, int, int, int)}
 * <br>The upper and lower limits, however, are only limitations on the display and the actual control can be set to any value available to other {@link IntegerControl} objects
 * <br> All  {@link IntegerControl} objects with the same name and {@link ControlScope} will respond to a value change.
 *
 *
 * <br> <br> See {@link IntegerControl} for general information about {@link IntegerControl} classes and how to set the value within your code
 */
public abstract class IntegerBuddyControl extends IntegerControl {

    /**
     * Constructor
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object
     * @param min_val Minimum value to display on Slider
     * @param max_val Maximum value to display on Slider
     * @deprecated use {@link IntegerControl} instead with {@link DynamicControl.DISPLAY_TYPE#DISPLAY_ENABLED_BUDDY}
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
