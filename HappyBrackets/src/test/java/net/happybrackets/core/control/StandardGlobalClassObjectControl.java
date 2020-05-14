package net.happybrackets.core.control;

import de.sciss.net.OSCMessage;
import org.junit.Test;

/**
 * Test the Sending and decoding of Standard classes that do not have custom decoding
 */
public class StandardGlobalClassObjectControl {
    // We will check value inside test
    boolean testSuccess = false;

    class BasicGlobalClass{
        public BasicGlobalClass(){};
        public BasicGlobalClass(String text){name = text;}
        public String name = "";
    }

    // define our Dynamic COntrol Name
    final String CONTROL_NAME =  "Generic";

    @Test
    public void testsGenericMessage() {
        //setup a running order for our tests
        // Ignore Device name so we do not ignore GLobal message from same device name
        DynamicControl.setIgnoreName(true);

        BasicGlobalClass genericGlobalMessage = new BasicGlobalClass("Hello");
        BasicGlobalClass secondGlobalMessage = new BasicGlobalClass("Goodbye");

        ClassObjectControl objectControl = new ClassObjectControl(this, CONTROL_NAME, BasicGlobalClass.class) {
            @Override
            public void valueChanged(Object control_val) {
                if (control_val != null){
                    BasicGlobalClass  decoded = (BasicGlobalClass)control_val;
                    // Check the value of our decode here
                    testSuccess = decoded.name.equalsIgnoreCase(secondGlobalMessage.name);
                }
            }
        }.setControlScope(ControlScope.GLOBAL);


        DynamicControl test_control2 = new DynamicControl(this, ControlType.OBJECT, CONTROL_NAME, secondGlobalMessage);
        // encode our message
        OSCMessage message = test_control2.buildNetworkSendMessage();


        DynamicControl.processOSCControlMessage(message, ControlScope.GLOBAL); // The control Listener will get called
        assert (testSuccess);
    }
}
