package net.happybrackets.core.control;

import com.google.gson.Gson;

import java.lang.reflect.InvocationTargetException;

/**
 * Dynamic Control used for sending class objects inside controls
 * Can be used to send object types such as full classes or doubles
 *
 */
public abstract class ClassObjectControl  extends DynamicControlParent {

    static Gson gson = new Gson();
    private Class<?> classType;

    /**
     * Constructor for abstract ClassObjectControl
     * derive from this class
     * @param parent_sketch The parent object for control
     * @param name The name to Display
     * @param initial_value Initial value of the object. The value in the return function  {@link ClassObjectControl#valueChanged(Object)}  will be cast
     */
    protected ClassObjectControl(Object parent_sketch, String name, Object initial_value) {
        super(new DynamicControl(parent_sketch, ControlType.OBJECT, name, initial_value));
        classType = initial_value.getClass();
    }

    @Override
    void notifyListener(Object val) {
        Object control_val = null;

        try {


            if (val.getClass() == classType) {
                control_val = val;

            } else if (val instanceof String) { // let us see if it is JsonData
                control_val = gson.fromJson((String) val, classType);

            } else if (val instanceof Object[]) {
                try {
                    control_val = ((CustomGlobalEncoder) classType.getConstructor().newInstance()).restore(val);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            } else {
                control_val = null;
            }

        }
        catch (Exception ex){

        }

        if (control_val != null) {
            valueChanged(control_val);
        }
    }

    /**
     * Get the value for the control
     * @return the control value. If unable to be cast bill be a null value
     */
    public Object getValue(){
        Object ret = null;

        Object control_val = getDynamicControl().getValue();

        if (control_val.getClass() == classType){
            ret = control_val;
        }
        else if (control_val instanceof String){ // let us see if it is JsonData
            ret = gson.fromJson((String) control_val, classType);
        }
        else if (control_val instanceof CustomGlobalEncoder) {
            ret = ((CustomGlobalEncoder)control_val).restore(control_val);
        }

        return  ret;
    }

    public abstract void valueChanged(Object control_val);

    /**
     * set the value for the control. This will notify all the listeners
     * @param val the value to set to
     */
    public void setValue(Object val){
        getDynamicControl().setValue(val);
    }

    /**
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * We must do this in subclass
     * @param new_scope The new Control Scope
     * @return this object
     */
    public ClassObjectControl setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);
        return this;
    }

    /**
     * Change how to display object
     * We must do this in subclass
     * @param display_type The new Control Scope
     * @return this object
     */
    public ClassObjectControl setDisplayType(DynamicControl.DISPLAY_TYPE display_type){
        getDynamicControl().setDisplayType(display_type);
        return this;
    }
}
