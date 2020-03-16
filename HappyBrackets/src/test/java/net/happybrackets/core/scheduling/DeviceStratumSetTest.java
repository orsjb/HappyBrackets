package net.happybrackets.core.scheduling;

import de.sciss.net.OSCMessage;
import net.happybrackets.core.Device;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.device.HB;
import org.junit.Test;

/**
 * Test that our sorting inside DeviceSchedules works Correctly
 */
public class DeviceStratumSetTest {


    @Test
    public void testStratumAdjust() {

        // First add devices to make sure we are second
        DeviceSchedulerValue value1 = new DeviceSchedulerValue(Device.getDeviceName(), HB.getScheduler().getUptime(), HB.getSchedulerTime(), 10);
        DeviceSchedules.getInstance().processCurrentMessage(value1.deviceName, value1);

        // Adding to our start time should make value 2 a higher value
        DeviceSchedulerValue value2 = new DeviceSchedulerValue("val2", HB.getScheduler().getUptime() + 100, HB.getSchedulerTime(), 10);
        DeviceSchedules.getInstance().processCurrentMessage(value2.deviceName, value2);

        DeviceSchedulerValue first = DeviceSchedules.getInstance().deviceSchedulerValues.first();
        assert (first.equals(value2));


        // create a better stratum, encode with OSC, decode and then process
        DeviceStratumMessage adjustment = new DeviceStratumMessage(Device.getDeviceName(), DeviceSchedules.LEAD_STRATUM);

        OSCMessage msg = HBScheduler.buildNetworkSendMessage(OSCVocabulary.SchedulerMessage.STRATUM, adjustment);
        Object[] values = new Object[msg.getArgCount() - HBScheduler.MESSAGE_PARAMS.OBJ_VAL.ordinal()];

        for (int i = 0; i < values.length; i++) {
            values[i] = msg.getArg(HBScheduler.MESSAGE_PARAMS.OBJ_VAL.ordinal() + i);
        }

        DeviceStratumMessage restored =  new DeviceStratumMessage().restore(values);

        assert (DeviceSchedules.getInstance().processStratumMessage(restored));


        System.out.println("------------------- Stratum Adjust complete -----------------------");
    }
}
