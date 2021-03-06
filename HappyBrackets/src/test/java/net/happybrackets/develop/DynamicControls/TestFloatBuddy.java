package net.happybrackets.develop.DynamicControls;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.core.control.FloatControl;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class TestFloatBuddy implements HBAction {
    // Change to the number of audio Channels on your device
    final int NUMBER_AUDIO_CHANNELS = 1;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        //hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");


        /*************************************************************
         * Create a Float type Dynamic Control pair that displays as a slider and text box
         * Simply type floatBuddyControl to generate this code
         *************************************************************/
        FloatControl floatBuddyControl = new FloatControl(this, "Buddy Name", 0) {
            @Override
            public void valueChanged(double control_val) {
                /*** Write your DynamicControl code below this line ***/

                System.out.println(control_val);
                /*** Write your DynamicControl code above this line ***/
            }
        }.setDisplayRange(-1, 1, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);/*** End DynamicControl floatBuddyControl code ***/




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
