package net.happybrackets.core.control;

import de.sciss.net.OSCMessage;
import org.junit.Test;

public class TestFloatControl {
    boolean testSuccess = false;
    final String CONTROL_NAME =  "Double";

    @Test
    public void testsGenericMessage() {
        //setup a running order for our tests
        // Ignore Device name so we do not ignore GLobal message from same device name
        DynamicControl.setIgnoreName(true);

        double randomVal = Math.random();

        // Type floatTextControl to generate this code
        FloatTextControl floatTextControl = new FloatTextControl(this, CONTROL_NAME, 0) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line
                System.out.println(control_val + "");

                testSuccess = Double.compare(control_val, randomVal) == 0;
                // Write your DynamicControl code above this line
            }
        }.setControlScope(ControlScope.GLOBAL);// End DynamicControl code floatTextControl


        DynamicControl test_control2 = new DynamicControl(this, ControlType.FLOAT, CONTROL_NAME, randomVal).setControlScope(ControlScope.GLOBAL);

        // encode our message
        OSCMessage message = test_control2.buildGlobalMessage();


        DynamicControl.processGlobalMessage(message); // The control Listener will get called


        assert (testSuccess);
    }
}
