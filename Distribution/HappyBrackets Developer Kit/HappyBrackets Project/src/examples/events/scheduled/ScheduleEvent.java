package examples.events.scheduled;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.*;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This class will schedule a global trigger event for a time set to the future, set by an IntegerControl
 *
 * Send Class to all devices.
 *
 * One One Device ----
 * Click top Reset button. All devices should show "Time reset" displayed in status
 *
 * Adjust the Slider to determine how long to wait for global message to activate after pressing "Send Message" button
 *
 * Press "Send Message" button. That device will have "Expect " and a time to expect it. When this time arrives, all devices should play the beep and show the time they responded
 */
public class ScheduleEvent implements HBAction {
    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below

        new TriggerControl(this, "Set clocks to zero") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line 
                // clear any events waiting to be triggered
                HB.getScheduler().reset();

                // we can reset clocks also with a global message
                HB.setScheduleTime(0);
                hb.setStatus("Time reset");
                // Write your DynamicControl code above this line 
            }
        }.setControlScope(ControlScope.GLOBAL);// End DynamicControl resetSchedule code


        // This buddy control s just to define how long to wait for message
        IntegerControl delaySchedule = new IntegerControl(this, "Delay MS", 100) {
            @Override
            public void valueChanged(int control_val) {// Write your DynamicControl code below this line 

                // Write your DynamicControl code above this line 
            }
        }.setDisplayRange(0, 10000, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);// End DynamicControl delaySchedule code

        // This control will cause device to play a beep and display it's time
        TriggerControl globalTrigger = new TriggerControl(this, "Play Now") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line 
                long time = (long) HB.getSchedulerTime();
                hb.testBleep();
                hb.setStatus("" + time);
                // Write your DynamicControl code above this line 
            }
        }.setControlScope(ControlScope.GLOBAL).setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED);// End DynamicControl globalTrigger code


        //we will send message to execute by the number of milliseconds after delaySchedule value

        // This message will cause global trigger to send a delayed message
        // This device will show the time to expect other devices to respond in its status
        new TriggerControl(this, "Send Message") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line 
                double schedulerTime =  (HB.getSchedulerTime());
                double trigger_time = schedulerTime + delaySchedule.getValue();
                globalTrigger.send(trigger_time);

                hb.setStatus("Expect " + (long) trigger_time);
                // Write your DynamicControl code above this line 
            }
        };// End DynamicControl triggerControl code 

        // write your code above this line
    }


    //<editor-fold defaultstate="collapsed" desc="Debug Start">

    /**
     * This function is used when running sketch in IntelliJ IDE for debugging or testing
     *
     * @param args standard args required
     */
    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>
}
