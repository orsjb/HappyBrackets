package net.happybrackets.core.control;

import de.sciss.net.OSCMessage;
import org.junit.Test;

/**
 * Test the function inside ClassObject Control
 */
public class TestCustomGlobalClassObjectControl {
    // We will check value inside test
    boolean testSuccess = false;


    // define our Dynamic COntrol Name
    final String CONTROL_NAME =  "Custom";

    @Test
    public void testCustomMessage() {
        //setup a running order for our tests
        // Ignore Device name so we do not ignore GLobal message from same device name
        DynamicControl.setIgnoreName(true);

        TripleAxisMessage customGlobalMessage = new TripleAxisMessage((float)Math.random(), (float)Math.random(), (float)Math.random() );
        TripleAxisMessage secondGlobalMessage = new TripleAxisMessage((float)Math.random(), (float)Math.random(), (float)Math.random() );

        ClassObjectControl objectControl = new ClassObjectControl(this, CONTROL_NAME, TripleAxisMessage.class) {
            @Override
            public void valueChanged(Object control_val) {
                if (control_val != null){
                    TripleAxisMessage decoded = (TripleAxisMessage)control_val;
                    // Check the value of our decode here
                    testSuccess = decoded.equals(secondGlobalMessage);
                }
            }
        }.setControlScope(ControlScope.GLOBAL);


        DynamicControl test_control2 = new DynamicControl(this, ControlType.OBJECT, CONTROL_NAME, secondGlobalMessage);
        // encode our message
        OSCMessage message = test_control2.buildGlobalMessage();


        DynamicControl.processGlobalMessage(message); // The control Listener will get called
        assert (testSuccess);
    }
}
