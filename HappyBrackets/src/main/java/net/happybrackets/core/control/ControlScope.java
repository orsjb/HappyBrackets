package net.happybrackets.core.control;

import java.net.InetAddress;

/**
 * Define the different types of scope we want for our controls
 * Although similar to the send and receive objects in Max in that the name and type
 * parameter of the DynamicControl determines message interconnection,
 * the {@link ControlScope} dictates how far (in
 * a topological sense) the object can reach in order to communicate with other
 * {@link DynamicControl} objects.  The different scopes available are:
 * <br>
 * <br>{@link ControlScope#UNIQUE} - a new independent control is created and only sends messages to registered listeners
 * <br>{@link ControlScope#SKETCH} - messages are also sent to other controls with the same name and scope belonging to other instances in the same sketch.
 * <br>{@link ControlScope#CLASS} - messages are also sent to other controls with the same name and scope belonging to other instances of the same class.
 * <br>{@link ControlScope#DEVICE} - messages are also sent to other controls with the same name and scope on the same device
 * <br>{@link ControlScope#GLOBAL} - messages are also sent to other controls with the same name and scope on the entire network.
 * <br>{@link ControlScope#TARGET} - messages are also sent to other controls with the same name and scope on specific or targeted devices on the network.
 *
 * <br> The Default ControlScope is {@link ControlScope#SKETCH}, so if {@link DynamicControlParent#setControlScope(ControlScope)} or {@link DynamicControl#setControlScope(ControlScope)} is not called, controls have {@link ControlScope#SKETCH}
 * For two Controls to match, they must have the same ControlScope, {@link ControlType}, and Name. For example:
 <pre>
    IntegerControl control1 = new IntegerControlSender(this, "ControlName", 0);
    control1.setControlScope(ControlScope.CLASS);

    IntegerTextControl control2 = new IntegerTextControl(this, "ControlName", 0) {
        {@literal @}Override
        public void valueChanged(int control_val) {// Write your DynamicControl code below this line
            System.out.println("Read " + control_val);
            // Write your DynamicControl code above this line
        }
    };// End DynamicControl control2 code
    control2.setControlScope(ControlScope.CLASS);

    control1.setValue(2);
 </pre>
 * The control1 and control2 will match because they both have a matching {@link ControlScope#CLASS} condition, the same name ("ControlName")
 * and are of type {@link ControlType#INT}. The result will be that control2 will receive the value from
 * control1, which will result in <b>Read 2</b> being printed to standard output.
 *
 */
public enum ControlScope {
    /**
     * An independent control that only sends messages to the GUI and not to other Controls.
     */
    UNIQUE,
    /**
     * Messages are sent to all devices of the same {@link ControlType}, {@link ControlScope} and name to all instances of the same  {@link Object} as the first parameter of the control constructor.
     <pre>
     IntegerControl control1 = new IntegerControlSender(this, "ControlName", 0);
     control1.setControlScope(ControlScope.SKETCH);

     IntegerTextControl control2 = new IntegerTextControl(this, "ControlName", 0) {
        {@literal @}Override
        public void valueChanged(int control_val) {// Write your DynamicControl code below this line
        System.out.println("Read " + control_val);
        // Write your DynamicControl code above this line
        }
     };// End DynamicControl control2 code
     control2.setControlScope(ControlScope.SKETCH);

     control1.setValue(2);
     </pre>
     * The control1 and control2 will match because they both have a matching {@link ControlScope#CLASS} condition and have the same {@link Object} as the first parameter of the control constructor , the same name ("ControlName")
     * and are of type {@link ControlType#INT}. The result will be that control2 will receive the value from
     * control1, which will result in <b>Read 2</b> being printed to standard output.
     * <br>{@link ControlScope#SKETCH} is different to {@link ControlScope#CLASS} in that if this code was sent multiple times on identical sketches,
     * control1 and control2 would only match on the same sketch.
     * <br>The Object used to match {@link ControlScope#SKETCH}  controls can be defined as another object. For example:
     <pre>
     <b>Object controlObject = hb.get("ControlObject");</b>


     IntegerControl control1 = new IntegerControlSender(<b>controlObject</b>, "ControlName", 0);

     IntegerTextControl control2 = new IntegerTextControl(<b>controlObject</b>, "ControlName", 0) {
     </pre>
     control1 and control2 match because they are both using <b>controlObject</b> as the control reference.
     */
    SKETCH,

    /**
     * Messages are sent to all devices of the same {@link ControlType}, {@link ControlScope} and name to all instances of the same {@link Class} as the first parameter of the control constructor.
     * This is the default scope and if {@link DynamicControlParent#setControlScope(ControlScope)} or {@link DynamicControl#setControlScope(ControlScope)} is not called, controls have {@link ControlScope#SKETCH}
     <pre>
     IntegerControl control1 = new IntegerControlSender(this, "ControlName", 0);
     control1.setControlScope(ControlScope.CLASS);

     IntegerTextControl control2 = new IntegerTextControl(this, "ControlName", 0) {
        {@literal @}Override
        public void valueChanged(int control_val) {// Write your DynamicControl code below this line
            System.out.println("Read " + control_val);
        // Write your DynamicControl code above this line
        }
     };// End DynamicControl control2 code
     control2.setControlScope(ControlScope.CLASS);

     control1.setValue(2);
     </pre>
     * The control1 and control2 will match because they both have a matching {@link ControlScope#CLASS} condition and have the same {@link Class} as the first parameter of the control constructor , the same name ("ControlName")
     * and are of type {@link ControlType#INT}. The result will be that control2 will receive the value from
     * control1, which will result in <b>Read 2</b> being printed to standard output.
     * <br>{@link ControlScope#CLASS} is different to {@link ControlScope#SKETCH} in that if this code was sent multiple times on identical sketches,
     * control1 and control2 on all the sketches would match on all sketches.
     */
    CLASS,

    /**
     * Messages are sent to all devices of the same {@link ControlType}, {@link ControlScope} and name on the same device. For example:
     *
     <pre>
     IntegerControl control1 = new IntegerControlSender(this, "ControlName", 0);
     control1.setControlScope(ControlScope.DEVICE);

     IntegerTextControl control2 = new IntegerTextControl(this, "ControlName", 0) {
         {@literal @}Override
        public void valueChanged(int control_val) {// Write your DynamicControl code below this line
        System.out.println("Read " + control_val);
        // Write your DynamicControl code above this line
        }
     };// End DynamicControl control2 code
     control2.setControlScope(ControlScope.DEVICE);

     control1.setValue(2);
     </pre>
     The value <b>2</b> will be sent to every {@link IntegerControl} or {@link DynamicControl} of {@link ControlType#INT} on this device that has the
     name <b>"ControlName"</b> and has {@link ControlScope#DEVICE}.
     */
    DEVICE,

    /**
     * Messages are sent to all devices of the same {@link ControlType}, {@link ControlScope} and name on the entire network.
     *
     <pre>
     // this is on device 1
     IntegerControl control1 = new IntegerControlSender(this, "ControlName", 0);
     control1.setControlScope(ControlScope.GLOBAL);

     control1.setValue(2);

     // this is on device 2
     IntegerTextControl control2 = new IntegerTextControl(this, "ControlName", 0) {
        {@literal @}Override
        public void valueChanged(int control_val) {// Write your DynamicControl code below this line
            System.out.println("Read " + control_val);
            // Write your DynamicControl code above this line
        }
     };// End DynamicControl control2 code
     control2.setControlScope(ControlScope.GLOBAL);


     </pre>
     The value <b>2</b> will be sent to every {@link IntegerControl} or {@link DynamicControl} of {@link ControlType#INT} on the entire network that has the
     name <b>"ControlName"</b> and has {@link ControlScope#GLOBAL}.
     */
    GLOBAL,

    /**
     * Messages are sent to registered listeners with the same scope and specific or targeted devices on the network.
     * See {@link DynamicControlParent#setControlTarget(String...)} and {@link DynamicControlParent#setControlTarget(InetAddress...)} for specific details on
     * sending messages to a specific target address. Consider the following
     <pre>
    IntegerControl control1 = new IntegerControlSender(this, "ControlName", 0);
    control1.setControlScope(ControlScope.TARGET);
    control1.addControlTarget("HB-123456", "HB-654321");
    control1.addControlTarget( "192.168.0.2");

    IntegerTextControl control2 = new IntegerTextControl(this, "ControlName", 0) {
        {@literal @}Override
        public void valueChanged(int control_val) {// Write your DynamicControl code below this line
            System.out.println("Read " + control_val);
            // Write your DynamicControl code above this line
        }
    };// End DynamicControl control2 code
    control2.setControlScope(ControlScope.TARGET);

    control1.setValue(2); // This will not be seen by control2 unless control1 has this device as one of it's targets


    <b>// This is on device "HB-654321"</b>
    IntegerTextControl control3 = new IntegerTextControl(this, "ControlName", 0) {
        {@literal @}Override
        public void valueChanged(int control_val) {// Write your DynamicControl code below this line
            System.out.println("Read " + control_val);
            // Write your DynamicControl code above this line
        }
    };// End DynamicControl receiver code
    control3.setControlScope(ControlScope.TARGET);
     </pre>
     *
     * In this example, control1 will send the value of <b>2</b> to devices whose names are HB-123456 or HB-654321, or at
     * address "192.168.0.2". Controls on those devices of {@link ControlType#INT}, name "ControlName"
     * with {@link ControlScope#TARGET} will accept the message. In this case, we see that control3
     * matches this (as it is on device "HB-654321") so it will print "Read 2" to the standard output;
     * however, control2 will not receive the message even though it is on the same device as control1 because
     * control1 did not have it's own device as a target through @link DynamicControlParent#setControlTarget(String...)}
     */
    TARGET


}
