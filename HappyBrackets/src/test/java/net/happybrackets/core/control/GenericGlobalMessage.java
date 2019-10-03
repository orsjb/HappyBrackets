package net.happybrackets.core.control;

import com.google.gson.Gson;
import de.sciss.net.OSCMessage;
import org.junit.Test;

public class GenericGlobalMessage {

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

        DynamicControl test_control = new DynamicControl(this, ControlType.OBJECT, CONTROL_NAME, genericGlobalMessage);
        DynamicControl test_control2 = new DynamicControl(this, ControlType.OBJECT, CONTROL_NAME, genericGlobalMessage);

        test_control.setControlScope(ControlScope.GLOBAL);
        test_control2.setControlScope(ControlScope.GLOBAL);

        // we will test if our message decoded as an OSC message inside listener
        test_control.addControlListener(control -> {
            Object value =  control.getValue();

            if (value != null){
                GenericTestMessageObject decoded = new Gson().fromJson((String) value, GenericTestMessageObject.class);
                System.out.println(decoded);

                // Check the value of our decode here
                testSuccess = decoded.equals(genericGlobalMessage);
            }
        });

        // encode our message
        OSCMessage message = test_control.buildGlobalMessage();

        System.out.println(message);

        DynamicControl.processOSCControlMessage(message, ControlScope.GLOBAL); // The control Listener will get called

        assert (testSuccess);
    }
}
