package net.happybrackets.develop.DynamicControls;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clock;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.TriggerControl;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class TestTrigger implements HBAction {
    // Change to the number of audio Channels on your device
    final int NUMBER_AUDIO_CHANNELS = 1;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");


        TriggerControl trigger = new TriggerControl(this, "Hello") {
            @Override
            public void triggerEvent() {
                System.out.println("Trigger Received");
            }
        };


        /*************************************************************
         * Create a Trigger type Dynamic Control
         *
         * Simply type globalTriggerControl to generate this code
         *************************************************************/
        TriggerControl globalTrigger1 = new TriggerControl(this, "global control name") {
            @Override
            public void triggerEvent() {
                /*** Write your DynamicControl code below this line ***/
                System.out.println("Global trigger 1");
                /*** Write your DynamicControl code above this line ***/
            }
        };
        globalTrigger1.setControlScope(ControlScope.GLOBAL);
        /*** End DynamicControl globalTrigger1 code ***/

        /*************************************************************
         * Create a Trigger type Dynamic Control
         *
         * Simply type globalTriggerControl to generate this code
         *************************************************************/
        TriggerControl globalTrigger2 = new TriggerControl(this, "global control name") {
            @Override
            public void triggerEvent() {
                /*** Write your DynamicControl code below this line ***/
                System.out.println("Global Trigger 2");
                /*** Write your DynamicControl code above this line ***/
            }
        };
        globalTrigger2.setControlScope(ControlScope.GLOBAL);
        /*** End DynamicControl globalTrigger2 code ***/
        /************************************************************
         * Create a clock with a interval based on the clock duration
         *
         * To create this, just type clockTimer
         ************************************************************/
        // create a clock and start changing frequency on each beat
        final float CLOCK_INTERVAL = 500;

        // Create a clock with beat interval of CLOCK_INTERVAL ms
        Clock clock = new Clock(CLOCK_INTERVAL);


        // let us handle triggers
        clock.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                // see if we are at the start of a beat
                boolean start_of_beat = clock.getCount() % clock.getTicksPerBeat() == 0;
                if (start_of_beat) {
                    /*** Write your code to perform functions on the beat below this line ****/

                    trigger.send();


                    /*** Write your code to perform functions on the beat above this line ****/
                } else {
                    /*** Write your code to perform functions off the beat below this line ****/

                    /*** Write your code to perform functions off the beat above this line ****/
                }
            }
        });
        /*********************** end clockTimer **********************/
        /***** Type your HBAction code above this line ******/
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
