package net.happybrackets.core.control;

import com.google.gson.Gson;
import de.sciss.net.OSCMessage;
import org.junit.Test;

/**
 * Test the function inside ClassObject Control
 */
public class TestClassObjectControl {
    // We will check value inside test
    boolean testSuccess = false;


    // define our Dynamic COntrol Name
    final String CONTROL_NAME =  "Generic";

    @Test
    public void testsGenericMessage() {
        //setup a running order for our tests

        GenericTestMessageObject genericGlobalMessage = new GenericTestMessageObject("Hello");
        GenericTestMessageObject secondGlobalMessage = new GenericTestMessageObject("Goodbye");

        ClassObjectControl objectControl = new ClassObjectControl(this, CONTROL_NAME, genericGlobalMessage) {
            @Override
            public void valueChanged(Object control_val) {
                if (control_val != null){
                    GenericTestMessageObject  decoded = (GenericTestMessageObject)control_val;
                    // Check the value of our decode here
                    testSuccess = decoded.equals(secondGlobalMessage);
                }
            }
        }.setControlScope(ControlScope.SKETCH);


        ClassObjectControlSender objectSender = new ClassObjectControlSender(this, CONTROL_NAME, genericGlobalMessage).setControlScope(ControlScope.SKETCH);

        objectSender.setValue(secondGlobalMessage);


        assert (testSuccess);
    }
}
