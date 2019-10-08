package net.happybrackets.core.control;

import de.sciss.net.OSCMessage;
import org.junit.Test;

/**
 * Test the function inside ClassObject Control
 */
public class TestGenericGlobalClassObjectControl {
    // We will check value inside test
    boolean testSuccess = false;


    // define our Dynamic COntrol Name
    final String CONTROL_NAME =  "Generic";

    @Test
    public void testsGenericMessage() {
        //setup a running order for our tests
        // Ignore Device name so we do not ignore GLobal message from same device name
        DynamicControl.setIgnoreName(true);

        GenericTestMessageObject genericGlobalMessage = new GenericTestMessageObject("Hello");
        GenericTestMessageObject secondGlobalMessage = new GenericTestMessageObject("Goodbye");

        ClassObjectControl objectControl = new ClassObjectControl(this, CONTROL_NAME, GenericTestMessageObject.class) {
            @Override
            public void valueChanged(Object control_val) {
                if (control_val != null){
                    GenericTestMessageObject  decoded = (GenericTestMessageObject)control_val;
                    // Check the value of our decode here
                    testSuccess = decoded.equals(secondGlobalMessage);
                }
            }
        }.setControlScope(ControlScope.GLOBAL);


        DynamicControl test_control2 = new DynamicControl(this, ControlType.OBJECT, CONTROL_NAME, secondGlobalMessage);
        // encode our message
        OSCMessage message = test_control2.buildNetworkSendlMessage();


        DynamicControl.processOSCControlMessage(message, ControlScope.GLOBAL); // The control Listener will get called
        assert (testSuccess);
    }
}
