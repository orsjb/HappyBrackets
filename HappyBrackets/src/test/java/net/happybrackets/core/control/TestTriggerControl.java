package net.happybrackets.core.control;

import de.sciss.net.OSCMessage;
import org.junit.Test;

public class TestTriggerControl {

    boolean testSuccess = false;

    @Test
    public void testsGenericMessage() {


        TriggerControl triggerControl = new TriggerControl(this, "Test") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line
                testSuccess = true;
                // Write your DynamicControl code above this line
            }
        }.setControlScope(ControlScope.SKETCH);// End DynamicControl triggerControl code

        // check that the event has not been sent after setting control scope
        assert (!testSuccess);

        triggerControl.send();

        // now test that event has been triggered after send
        assert (testSuccess);
    }
}
