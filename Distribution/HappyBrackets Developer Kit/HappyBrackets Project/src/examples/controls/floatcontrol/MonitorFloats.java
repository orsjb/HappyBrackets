package examples.controls.floatcontrol;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clock;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.FloatBuddyControl;
import net.happybrackets.core.control.FloatTextControl;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch runs a clock and displays the Beat count in a text control ad the clock count in a slider control using Beads Clock
 * Note - if you have the slider control selected, you will not see the value update on the screen.
 */
public class MonitorFloats implements HBAction {
    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");


        /*************************************************************
         * Create a Float type Dynamic Control that displays as a text box
         * Simply type floatTextControl to generate this code
         *************************************************************/
        FloatTextControl clock_beats = new FloatTextControl(this, "Beat Count", 0) {
            @Override
            public void valueChanged(double control_val) {
                /*** Write your DynamicControl code below this line ***/

                /*** Write your DynamicControl code above this line ***/
            }
        };/*** End DynamicControl code clock_beats ***/


        /*************************************************************
         * Create a Float type Dynamic Control pair that displays as a slider and text box
         * Simply type floatBuddyControl to generate this code
         *************************************************************/
        FloatBuddyControl clock_value = new FloatBuddyControl(this, "Clock Count", 0, -1, 2000) {
            @Override
            public void valueChanged(double control_val) {
                /*** Write your DynamicControl code below this line ***/

                /*** Write your DynamicControl code above this line ***/
            }
        };/*** End DynamicControl clock_value code ***/



        /************************************************************
         * start clockTimer
         * Create a clock with a interval based on the clock duration
         *
         * To create this, just type beadsClockTimer
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

                    clock_value.setValue(clock.getCount());
                    clock_beats.setValue(clock.getBeatCount());

                    /*** Write your code to perform functions on the beat above this line ****/
                } else {
                    /*** Write your code to perform functions off the beat below this line ****/
                    clock_value.setValue(clock.getCount());

                    /*** Write your code to perform functions off the beat above this line ****/
                }
            }
        });
        /*********************** end beadsClockTimer **********************/
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
