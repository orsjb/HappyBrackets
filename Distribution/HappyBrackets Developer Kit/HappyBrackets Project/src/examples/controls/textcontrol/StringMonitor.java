package examples.controls.textcontrol;


import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.TextControl;
import net.happybrackets.core.control.TextControlSender;
import net.happybrackets.core.scheduling.Clock;
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

        /* Type textControlSender to generate this code */
        TextControl clockMonitor = new TextControlSender(this, "Clock Count", "");


        /* To create this, just type clockTimer */
        Clock clock = hb.createClock(500).addClockTickListener((offset, this_clock) -> {/* Write your code below this line */

            long clock_count = this_clock.getNumberTicks();

            String display_text = new String("clock: " + clock_count);
            clockMonitor.setValue(display_text);

            /* Write your code above this line */
        });

        clock.start();/* End Clock Timer */


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
