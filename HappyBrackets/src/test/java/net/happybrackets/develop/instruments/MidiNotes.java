package net.happybrackets.develop.instruments;

import net.beadsproject.beads.data.Buffer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class MidiNotes implements HBAction, HBReset {
    private static final int MIN_NOTE = 40;
    private static final int MAX_NOTE = 100;

    int midiNote = MIN_NOTE;

    // Change to the number of audio Channels on your device
    final int NUMBER_AUDIO_CHANNELS = 1;

    // This variable will become true when the composition is reset
    boolean compositionReset = false;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        WaveModule player = new WaveModule().setMidiFequency(MIN_NOTE).setBuffer(Buffer.SQUARE);

        player.connectTo(hb.ac.out);

        /*** To create this, just type clockTimer ***/
        Clock clock = hb.createClock(200).addClockTickListener((offset, this_clock) -> {
            /*** Write your Clock tick event code below this line ***/
            midiNote++;
            if (midiNote > MAX_NOTE){
                midiNote = MIN_NOTE;
            }

            player.setMidiFequency(midiNote);

            /*** Write your Clock tick event code above this line ***/
        });

        clock.start();
        /******************* End Clock Timer *************************/

        /***** Type your HBAction code above this line ******/
    }


    /**
     * Add any code you need to have occur when a reset occurs
     */
    @Override
    public void doReset() {
        compositionReset = true;
        /***** Type your HBReset code below this line ******/

        /***** Type your HBReset code above this line ******/
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
