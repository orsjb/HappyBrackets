package net.happybrackets.develop.DynamicControls;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clock;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.*;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class TestFloatControl implements HBAction {
    // Change to the number of audio Channels on your device
    final int NUMBER_AUDIO_CHANNELS = 1;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");


        /*************************************************************
         * Create a Float type Dynamic Control pair that displays as a slider and text box
         * Simply type floatBuddyControl to generate this code
         *************************************************************/
        FloatBuddyControl floatBuddyControl = new FloatBuddyControl(this, "globalFloat2 name", 0, -1, 1) {
            @Override
            public void valueChanged(double control_val) {
                /*** Write your DynamicControl code below this line ***/

                /*** Write your DynamicControl code above this line ***/
            }
        };/*** End DynamicControl floatBuddyControl code ***/


        /*************************************************************
         * Create a Float type Dynamic Control that displays as a slider
         * Simply type floatSliderControl to generate this code
         *************************************************************/
        FloatSliderControl floatSliderControl = new FloatSliderControl(this, "globalFloat2 name", 0, -1, 1) {
            @Override
            public void valueChanged(double control_val) {
                /*** Write your DynamicControl code below this line ***/
                System.out.println("Slider " + control_val);
                floatBuddyControl.setValue(control_val);
                /*** Write your DynamicControl code above this line ***/
            }
        };/*** End DynamicControl floatSliderControl code ***/


        FloatTextControl floatTextControl = new FloatTextControl(this, "Text", 0) {
            @Override
            public void valueChanged(double control_val) {
                /*** Write your DynamicControl code below this line ***/
                System.out.println("Text " + control_val);
                floatBuddyControl.setValue(control_val);
                /*** Write your DynamicControl code above this line ***/
            }
        };

        /*************************************************************
         * Create a Float type Dynamic Control pair
         * Simply type globalFloatControl to generate this code
         *************************************************************/
        FloatBuddyControl globalFloat = new FloatBuddyControl(this, "global globalFloat2 name", 0, -1, 1) {
            @Override
            public void valueChanged(double control_val) {
                /*** Write your DynamicControl code below this line ***/

                /*** Write your DynamicControl code above this line ***/
            }
        };
        globalFloat.setControlScope(ControlScope.GLOBAL);
        /*** End DynamicControl globalFloat code ***/

        /*************************************************************
         * Create a Float type Dynamic Control pair
         * Simply type globalFloatControl to generate this code
         *************************************************************/
        FloatBuddyControl globalFloat2 = new FloatBuddyControl(this, "global globalFloat2 name", 0, -1, 1) {
            @Override
            public void valueChanged(double control_val) {
                /*** Write your DynamicControl code below this line ***/

                /*** Write your DynamicControl code above this line ***/
            }
        };
        globalFloat2.setControlScope(ControlScope.GLOBAL);
        /*** End DynamicControl globalFloat2 code ***/
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

                    double current_val =  floatTextControl.getValue();

                    floatTextControl.setValue(current_val + 1);


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
