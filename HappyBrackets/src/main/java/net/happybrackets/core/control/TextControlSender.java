package net.happybrackets.core.control;

/**
 * @deprecated use {@link TextControl} instead
 * This class encapsulates the functionality of {@link DynamicControl} classes of type {@link ControlType#TEXT} in a simple API
 * <br> All Text controls with the same name and {@link ControlScope} will respond to a message send. For example:
 * For example, consider two TextControls with the same {@link ControlScope} and name
 *
 <pre>
 TextControl control1 = new <b>TextControlSender(this, "Read", "");</b>
 TextControl control2 = new TextControl(this, "Read", "") {
    {@literal @}Override
    public void valueChanged(String control_val) {
    System.out.println("Read " + control_val);
    }
 };

 control1.setValue("This is text");
 </pre>
 *
 *
 * <br>The functionality is identical to the {@link TextControl} except {@link TextControlSender} does not have the {@link TextControl#valueChanged(String)} handler;
 *
 <br>Setting the value within the HappyBrackets control display is effected by typing a new value in and pressing ENTER or RETURN
 */
public class TextControlSender extends TextControl {
    /**
     * Constructor for abstract FloatControl. Slider, Text and Buddy Control will
     * derive from this class
     *
     * @param parent_sketch The parent object for control
     * @param name          The name to Display
     * @param initial_value Initial value of the object
     * @deprecated use {@link TextControl} instead
     */
    public TextControlSender(Object parent_sketch, String name, String initial_value) {
        super(parent_sketch, name, initial_value);
    }

    @Override
    public void valueChanged(String control_val) {

    }

    /**
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * If the object is ControlScope Sketch or Unique, it will be displayType in the gui
     * @param new_scope The new Control Scope
     * @return this object
     */
    public TextControlSender setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);

        // we do not want to be able to change this object in the GUI if noting could be connected to it
        //setDisabled(new_scope == ControlScope.SKETCH || new_scope == ControlScope.UNIQUE);
        return this;
    }
}
