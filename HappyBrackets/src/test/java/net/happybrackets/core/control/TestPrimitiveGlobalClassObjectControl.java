package net.happybrackets.core.control;

import de.sciss.net.OSCMessage;
import org.junit.Test;

/**
 * Test the function using a double
 */
public class TestPrimitiveGlobalClassObjectControl {
    // We will check value inside test
    boolean testSuccess = false;


    // define our Dynamic COntrol Name
    final String CONTROL_NAME =  "Generic";

    @Test
    public void testsGenericMessage() {
        //setup a running order for our tests
        // Ignore Device name so we do not ignore GLobal message from same device name
        DynamicControl.setIgnoreName(true);

        double genericGlobalMessage = Math.random();

        double secondGlobalMessage = Math.random();

        ClassObjectControl objectControl = new ClassObjectControl(this, CONTROL_NAME, double.class) {
            @Override
            public void valueChanged(Object control_val) {
                if (control_val != null){
                    double  decoded = (double)control_val;
                    // Check the value of our decode here
                    testSuccess = Double.compare(decoded, secondGlobalMessage) == 0;
                }
            }
        }.setControlScope(ControlScope.GLOBAL);


        DynamicControl test_control2 = new DynamicControl(this, ControlType.OBJECT, CONTROL_NAME, secondGlobalMessage);
        // encode our message
        OSCMessage message = test_control2.buildGlobalMessage();


        DynamicControl.processOSCControlMessage(message, ControlScope.GLOBAL); // The control Listener will get called
        assert (testSuccess);
    }
}
