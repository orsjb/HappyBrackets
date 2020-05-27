package examples.events.scheduled;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.control.*;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This class will adjust the scheduler time by the amount we send it
 * A thread plays a beep every second
 *
 * When we adjust the scheduler, you will hear the beepClock adjust for that duration
 */
public class AdjustSynchroniser implements HBAction, HBReset {
    boolean compositionReset = false;

    final int ADJUSTMENT_AMOUNT = 1000; // the amount we will adjust our scheduler
    final int ADJUSTMENT_TIME = 5000; // how long it will take to complete the adjustment

    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device

        // To create this, just type clockTimer
        Clock beepClock = hb.createClock(1000).addClockTickListener((offset, this_clock) -> {// Write your code below this line
            hb.testBleep();
            // Write your code above this line 
        });

        beepClock.start();// End Clock Timer


        // This control will dictate how long it will take for the adjustment to occur
        FloatControl adjustmentAmount = new FloatControl(this, "AdjustmentAmount", ADJUSTMENT_AMOUNT).setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);
        IntegerControl adjustmentTime = new IntegerControl(this, "AdjustmentTime", ADJUSTMENT_TIME).setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);


        // Type triggerControl to generate this code 
        new TriggerControl(this, "Adjust Time") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line
                double adjustment = adjustmentAmount.getValue();
                int duration = adjustmentTime.getValue();
                HB.sendScheduleChange(adjustment, duration, null);
                HB.sendStatus("Starting Adjustment of " + adjustment + " over " + duration);
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl setTime code 


        HB.getScheduler().addScheduleChangeListener(hbScheduler -> {
                    HB.sendStatus("Reschedule complete");
        });


        // write your code above this line
    }

    void showTime(){
        double time = HB.getSchedulerTime();
        HB.sendStatus("" + (long)time);
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

    @Override
    public void doReset() {
        compositionReset = true;
    }
    //</editor-fold>
}
