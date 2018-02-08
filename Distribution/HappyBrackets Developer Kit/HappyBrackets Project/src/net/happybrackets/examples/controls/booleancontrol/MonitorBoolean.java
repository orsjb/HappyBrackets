package net.happybrackets.examples.controls.booleancontrol;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clock;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch runs a clock and toggles a Boolean value every time the beat count is a multiple of 100
 * Note that if you have the checkbox seleced, you will not see the value change
 */
public class MonitorBoolean implements HBAction {
    @Override
    public void action(HB hb) {

        final int TOGGLE_VALUE = 100;


        /*************************************************************
         * Create an integer type Dynamic Control that displays as a text box
         *
         * Simply type intTextControl to generate this code
         *************************************************************/
        DynamicControl clock_beats = hb.createDynamicControl(this, ControlType.INT, "Beat Count");
        // we have removed the listener because it was unnecessary
        /*** End DynamicControl code ***/


        /*************************************************************
         * Create a Boolean type Dynamic Control pair that displays as a check box
         *
         * Simply type booleanControl to generate this code
         *************************************************************/
        DynamicControl booleanControl = hb.createDynamicControl(this, ControlType.BOOLEAN, "Beat Toggle", false);
        // we have removed the listener because it was unnecessary
        /*** End DynamicControl code ***/



        /************************************************************
         * start clockTimer
         * Create a clock with a interval based on the clock duration
         *
         * To create this, just type clockTimer
         ************************************************************/
        // create a clock and start changing frequency on each beat
        final float CLOCK_INTERVAL = 50;

        // Create a clock with beat interval of CLOCK_INTERVAL ms
        Clock clock = new Clock(hb.ac, CLOCK_INTERVAL);
        // connect the clock to HB
        hb.ac.out.addDependent(clock);

        // let us handle triggers
        clock.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                // see if we are at the start of a beat
                boolean start_of_beat = clock.getCount() % clock.getTicksPerBeat() == 0;
                if (start_of_beat) {
                    /*** Write your code to perform functions on the beat below this line ****/

                    int num_beats = clock.getBeatCount();

                    clock_beats.setValue(num_beats);
                    if (num_beats % TOGGLE_VALUE == 0){
                        boolean current_state = (boolean)booleanControl.getValue();

                        booleanControl.setValue(!current_state);
                    }


                    /*** Write your code to perform functions on the beat above this line ****/
                } else {
                    /*** Write your code to perform functions off the beat below this line ****/

                    /*** Write your code to perform functions off the beat above this line ****/
                }
            }
        });
        /*********************** end clockTimer **********************/
    }


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
}
