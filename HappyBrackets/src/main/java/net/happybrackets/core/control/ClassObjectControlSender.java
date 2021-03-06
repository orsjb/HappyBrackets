package net.happybrackets.core.control;

/**
 * @deprecated use {@link ClassObjectControlSender} instead
 * The {@link ClassObjectControlSender} class is identical to the {@link ClassObjectControl} class except it does not provide the {@link ClassObjectControl#valueChanged(Object)} event handler.
 * <br> See {@link ClassObjectControl} for full details
 */
public class ClassObjectControlSender extends ClassObjectControl {

    /**
     * Constructor for ClassObjectControlSender
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param class_type Initial value of the object
     * {@link ClassObjectControlSender}
     */
    public ClassObjectControlSender(Object parent_sketch, String name, Class<?> class_type) {
        super(parent_sketch, name, class_type);
    }

    @Override
    public void valueChanged(Object control_val) {

    }

    /**
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * We must do this in subclass
     * @param new_scope The new Control Scope
     * @return this object
     */
    public ClassObjectControlSender setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);
        return this;
    }

    /**
     * Change how to display object
     * We must do this in subclass
     * @param display_type The new Control Scope
     * @return this object
     */
    public ClassObjectControlSender setDisplayType(DynamicControl.DISPLAY_TYPE display_type){
        getDynamicControl().setDisplayType(display_type);
        return this;
    }
}
