package examples.controls.textcontrol;


import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.TextControl;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch runs a clock and displays the value in a text box
 * using a text DynamicControl
 */
public class StringMonitor implements HBAction {
    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        /*************************************************************
         * Create a string type Dynamic Control that displays as a text box
         * Simply type textControl to generate this code
         *************************************************************/
        TextControl clockMonitor = new TextControl(this, "Clock Count", "") {
            @Override
            public void valueChanged(String control_val) {
                /*** Write your DynamicControl code below this line ***/

                /*** Write your DynamicControl code above this line ***/
            }
        };/*** End DynamicControl clockMonitor code ***/


        /************************************************************
         * To create this, just type clockTimer
         ************************************************************/
        net.happybrackets.core.scheduling.Clock hbClock = hb.createClock(500.0).addClockTickListener((offset, this_clock) -> {
            /*** Write your Clock tick event code below this line ***/
            long clock_count = this_clock.getNumberTicks();

            String display_text = new String("clock: " + clock_count);
            clockMonitor.setValue(display_text);
            /*** Write your Clock tick event code above this line ***/
        });

        hbClock.start();
        /******************* End Clock Timer *************************/

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
