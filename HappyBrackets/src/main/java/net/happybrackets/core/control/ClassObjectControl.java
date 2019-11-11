package net.happybrackets.core.control;

import com.google.gson.Gson;

import java.lang.reflect.InvocationTargetException;

/**
 * This class encapsulates the functionality of {@link DynamicControl} classes of type {@link ControlType#OBJECT} in a simple API. This enables sending of entire classes as a single message.
 * <br> For example, you can send x, y and z values a {@link TripleAxisMessage} using the {@link ClassObjectControl#setValue(Object)}
 * <br>
 * <br> All {@link ClassObjectControl} objects with the same name and {@link ControlScope} will respond to a {@link ClassObjectControl#setValue(Object)}.
 * Additionally, the {@link Class} of the object that we are sending must also be defined. For example:
 consider two {@link ClassObjectControl} with the same {@link ControlScope} and name:
 * <br><b>
 *     <br><br>ClassObjectControl control1 = new ClassObjectControlSender(this, "MyControl", TripleAxisMessage.class);
 *
 *     <br><br>
 *         <br>ClassObjectControl control2 = new ClassObjectControl(this, "MyControl", TripleAxisMessage.class) {
 *         <br>&emsp;   @Override
 *         <br>&emsp;   public void valueChanged(Object object_val) {
 *         <br>&emsp;&emsp;       TripleAxisMessage control_val = (TripleAxisMessage) object_val;
 *         <br>&emsp;&emsp;System.out.println("x:" + msg.getX() + " y:" + msg.getY() +  " z" + msg.getZ());
 *         <br>&emsp;   }
 *
 *         };
 *
 *     <br><br>
 *     TripleAxisMessage msg = new TripleAxisMessage(0.1f, 0.2f, 0.3f);
 *     <br>control1.setValue(msg);
 *
 * </b>
 *
 *
 <br><br>This will cause the <b>control2</b> to fire the {@link ClassObjectControl#valueChanged(Object)} function with the new value, causing <b>x:0.1 y:0.2 z:0.3</b> to be printed to standard output
 * <br>The control can also schedule messages to be sent at a time in the future by adding the time to the message
 * using an absolute time in the {@link ClassObjectControl#setValue(Object, double)}  function. For example, the message can be scheduled to execute in 1 second as follows: <br>
 * <br> <b>control1.setValue (msg, HB.getSchedulerTime() + 1000);</b>
 <br><br>Values cannot be set within HappyBrackets control displays
 * If you do not require a handler on the class, use the {@link ClassObjectControlSender} class
 *
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
     * @param class_type Initial value of the object. The value in the return function  {@link ClassObjectControl#valueChanged(Object)}  will be cast
     */
    protected ClassObjectControl(Object parent_sketch, String name, Class<?> class_type) {
        super(new DynamicControl(parent_sketch, ControlType.OBJECT, name, createDefaultObject(class_type)));
        classType = class_type;
    }

    static protected Object createDefaultObject(Class<?> class_type){
        Object ret = "";

        try {
            ret = class_type.getConstructor().newInstance();
        } catch (InstantiationException e) {
            //e.printStackTrace();
        } catch (IllegalAccessException e) {
            //e.printStackTrace();
        } catch (InvocationTargetException e) {
            //e.printStackTrace();
        } catch (NoSuchMethodException e) {
            //e.printStackTrace();
        }
        return ret;
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
     * set the value for the control. This will notify all the listeners
     * @param val the value to set to
     * @param scheduler_time the scheduler time this is supposed to occur at
     */
    public void setValue(Object val, double scheduler_time){
        getDynamicControl().setValue(val, scheduler_time);
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
