package net.happybrackets.core.control;

import de.sciss.net.OSCMessage;
import org.junit.Test;

import java.util.Random;

public class CutsomGlobalMessage {

    // We will check value inside test
    boolean testSuccess = false;

    final String CONTROL_NAME =  "Accelerometer";

    @Test
    public void testsCustomMessage() {
        //setup a running order for our tests

        DynamicControl.setIgnoreName(true);

        AccelerometerMessage accelerometerMessage = new AccelerometerMessage((float)Math.random(), (float)Math.random(), (float)Math.random() );
        DynamicControl test_control = new DynamicControl(this, ControlType.OBJECT, CONTROL_NAME, accelerometerMessage);
        DynamicControl test_control2 = new DynamicControl(this, ControlType.OBJECT, CONTROL_NAME, accelerometerMessage);

        test_control.setControlScope(ControlScope.GLOBAL);
        test_control2.setControlScope(ControlScope.GLOBAL);

        // we will test if our message decoded as an OSC message inside liustener
        test_control.addControlListener(control -> {
            Object value =  control.getValue();

            if (value != null){
                AccelerometerMessage decoded = new AccelerometerMessage().restore(value);
                System.out.println(decoded);

                // check if our Message test decoded OK
                testSuccess = decoded.equals(accelerometerMessage);
            }
        });

        // encode our message
        OSCMessage message = test_control.buildGlobalMessage();

        System.out.println(message);

        DynamicControl.processGlobalMessage(message);
        assert (testSuccess);

    }
}
