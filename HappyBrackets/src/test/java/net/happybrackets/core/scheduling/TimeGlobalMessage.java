package net.happybrackets.core.scheduling;

import de.sciss.net.OSCMessage;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import org.junit.Test;

public class TimeGlobalMessage {

    // We will check value inside test
    boolean testSuccess = false;

    @Test
    public void testsCustomMessage() {
        //setup a running order for our tests

        ClockAdjustment adjustmentMessage = new ClockAdjustment(Math.random() * Integer.MAX_VALUE, (long)(Math.random() * 1000000) );

        // encode our message
        OSCMessage message = HBScheduler.buildNetworkSendMessage(OSCVocabulary.SchedulerMessage.SET, adjustmentMessage);

        System.out.println(message);

        testSuccess = HBScheduler.ProcessSchedulerMessage(message);

        assert (testSuccess);

        // check that we ignore next one
        testSuccess = !HBScheduler.ProcessSchedulerMessage(message);

        assert (testSuccess);

    }
}
