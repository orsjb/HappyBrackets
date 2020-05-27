package net.happybrackets.develop.DynamicControls;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clock;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.*;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class TestIntControl implements HBAction {
    // Change to the number of audio Channels on your device
    final int NUMBER_AUDIO_CHANNELS = 1;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");


        /*************************************************************
         * Create an integer type Dynamic Control pair that displays as a slider and text box
         * Simply type intBuddyControl to generate this code
         *************************************************************/
        IntegerControl integerBuddyControl = new IntegerControl(this, "Buddy", 0, -100, 100, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY) {

            @Override
            public void valueChanged(int new_value) {
                /*** Write your DynamicControl code below this line ***/
                System.out.println("Buddy " + new_value);
                /*** Write your DynamicControl code above this line ***/
            }
        };/*** End DynamicControl intBuddyControl code ***/


        /*************************************************************
         * Create an integer type Dynamic Control that displays as a slider
         * Simply type intSliderControl to generate this code
         *************************************************************/
        IntegerControl integerSliderControl = new IntegerControl(this, "int control", 0 ) {

            @Override
            public void valueChanged(int new_value) {
                /*** Write your DynamicControl code below this line ***/
                System.out.println("Slider " + new_value);
                integerBuddyControl.setValue(new_value);
                /*** Write your DynamicControl code above this line ***/
            }
        }.setDisplayRange(-100, 100, DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);/*** End DynamicControl integerSliderControl code ***/


        /*************************************************************
         * Create an integer type Dynamic Control that displays as a text box
         * Simply type intTextControl to generate this code
         *************************************************************/
        IntegerControl integerTextControl = new IntegerControl(this, "Global", 0) {
            @Override
            public void valueChanged(int new_value) {
                /*** Write your DynamicControl code below this line ***/
                System.out.println("Text " + new_value);
                integerBuddyControl.setValue(new_value);
                /*** Write your DynamicControl code above this line ***/
            }
        };/*** End DynamicControl integerTextControl code ***/

        integerTextControl.setControlScope(ControlScope.GLOBAL);


        /*************************************************************
         * Create an integer type Dynamic Control that displays as a text box
         * Simply type intTextControl to generate this code
         *************************************************************/
        IntegerControl textControl = new IntegerControl(this, "Global", 0) {
            @Override
            public void valueChanged(int new_value) {
                /*** Write your DynamicControl code below this line ***/

                /*** Write your DynamicControl code above this line ***/
            }
        };
        textControl.setControlScope(ControlScope.GLOBAL);
        /*** End DynamicControl textControl code ***/


        /*************************************************************
         * Create a Float type Dynamic Control pair
         *
         * Simply type globalIntControl to generate this code
         *************************************************************/
        IntegerControl global1 = new IntegerControl(this, "global control name", 0) {

            @Override
            public void valueChanged(int new_value) {
                /*** Write your DynamicControl code below this line ***/

                /*** Write your DynamicControl code above this line ***/
            }
        }.setDisplayRange(-1, 1, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);
        global1.setControlScope(ControlScope.GLOBAL);
        /*** End DynamicControl global1 code ***/

        /*************************************************************
         * Create a Float type Dynamic Control pair
         *
         * Simply type globalIntControl to generate this code
         *************************************************************/
        IntegerControl global2 = new IntegerControl(this, "global control name", 0) {

            @Override
            public void valueChanged(int new_value) {
                /*** Write your DynamicControl code below this line ***/

                /*** Write your DynamicControl code above this line ***/
            }
        }.setDisplayRange( -1, 1, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);
        global2.setControlScope(ControlScope.GLOBAL);
        /*** End DynamicControl global2 code ***/

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

                    int current_val =  integerTextControl.getValue();

                    integerTextControl.setValue(current_val + 1);


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
