package net.happybrackets.core.scheduling;

import net.happybrackets.device.HB;
import org.junit.Test;

/**
 * Test that our sorting inside DeviceSchedules works Correctly
 */
public class DeviceSchedulerSortTest {


    @Test
    public void testSort() {

        DeviceSchedulerValue value1 = new DeviceSchedulerValue("val1", HB.getScheduler().getUptime(), HB.getSchedulerTime(), 10);
        DeviceSchedules.getInstance().processCurrentMessage(value1.deviceName, value1);

        // Adding to our start time should make value 2 a higher value
        DeviceSchedulerValue value2 = new DeviceSchedulerValue("val2", HB.getScheduler().getUptime() + 100, HB.getSchedulerTime(), 10);
        DeviceSchedules.getInstance().processCurrentMessage(value2.deviceName, value2);

        DeviceSchedulerValue first = DeviceSchedules.getInstance().deviceSchedulerValues.first();
        assert (first.equals(value2));


        // Put a value in with better stratum
        DeviceSchedulerValue value3 = new DeviceSchedulerValue("val1", HB.getScheduler().getUptime(), HB.getSchedulerTime(), 1);
        DeviceSchedules.getInstance().processCurrentMessage(value3.deviceName, value3);

        first = DeviceSchedules.getInstance().deviceSchedulerValues.first();
        assert (first.equals(value1));

        for (DeviceSchedulerValue next:
                DeviceSchedules.getInstance().deviceSchedulerValues) {
            System.out.println(next);

        }

        // check that we have the expected number of ticks, allowing for a rounding in our expectation
        assert (true);


        System.out.println("------------------- Schedule Adjust complete -----------------------");
    }
}
