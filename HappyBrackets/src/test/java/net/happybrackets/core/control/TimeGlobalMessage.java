package net.happybrackets.core.control;

import de.sciss.net.OSCMessage;
import net.happybrackets.core.scheduling.ClockAdjustment;
import org.junit.Test;

public class TimeGlobalMessage {

    // We will check value inside test
    boolean testSuccess = false;

    final String CONTROL_NAME =  "Adjust Time";

    @Test
    public void testsCustomMessage() {
        //setup a running order for our tests

        DynamicControl.setIgnoreName(true);

        ClockAdjustment adjustmentMessage = new ClockAdjustment(Math.random() * Integer.MAX_VALUE, (long)(Math.random() * 1000000) );

        DynamicControl test_control = new DynamicControl(this, ControlType.OBJECT, CONTROL_NAME, adjustmentMessage);
        DynamicControl test_control2 = new DynamicControl(this, ControlType.OBJECT, CONTROL_NAME, adjustmentMessage);

        test_control.setControlScope(ControlScope.GLOBAL);
        test_control2.setControlScope(ControlScope.GLOBAL);

        // we will test if our message decoded as an OSC message inside listener
        test_control.addControlListener(control -> {
            Object value =  control.getValue();

            if (value != null){
                ClockAdjustment decoded = new ClockAdjustment().restore(value);
                System.out.println(decoded);

                // check if our Message test decoded OK
                testSuccess = decoded.equals(adjustmentMessage);
            }
        });

        // encode our message
        OSCMessage message = test_control.buildNetworkSendMessage();

        System.out.println(message);

        DynamicControl.processOSCControlMessage(message, ControlScope.GLOBAL);
        DynamicControl.processOSCControlMessage(message, ControlScope.GLOBAL);


        assert (testSuccess);

    }
}
