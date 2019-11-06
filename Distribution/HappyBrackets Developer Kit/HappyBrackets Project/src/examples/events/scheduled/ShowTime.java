package examples.events.scheduled;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.core.control.TriggerControl;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.core.scheduling.Delay;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * Displays the Scheduler time
 * Note that we can re-trigger an event after adjusting scheduler by listening for addScheduleChangeListener
 */
public class ShowTime implements HBAction {


    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below

        // To create this, just type clockTimer
        Clock clock = hb.createClock(1000).addClockTickListener((offset, this_clock) -> {// Write your code below this line
            showTime();
            // Write your code above this line 
        });

        clock.start();// End Clock Timer


        // Type triggerControl to generate this code
        TriggerControl startClock = new TriggerControl(this, "Start Clock") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line 
                clock.start();
                // Write your DynamicControl code above this line 
            }
        }.setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED);// End DynamicControl startClock code

        // Type globalTriggerControl to generate this code 
        TriggerControl resetTime = new TriggerControl(this, "Reset Time") {
            @Override
            public void triggerEvent() {//  Write your DynamicControl code below this line

                HB.setScheduleTime(0);

                // Write your DynamicControl code above this line 
            }
        }.setControlScope(ControlScope.GLOBAL);// End DynamicControl resetTime code

        // Register to be notified when the scheduler change is complete
        HB.getScheduler().addScheduleChangeListener(hbScheduler -> {
           HB.sendStatus("Reschedule complete");

            // stop all clocks
            clock.stop();

            long time =  (long)hbScheduler.getSchedulerTime();

            // round up to a second
            long next_time = time - time % 1000;

            // we will cause our trigger to start the next whole second
            startClock.send(next_time + 1000);
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
    //</editor-fold>
}
