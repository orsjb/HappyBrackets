package net.happybrackets.develop.instruments.sample;

import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.instruments.SampleModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class BasicSamplePlayerEnvelope implements HBAction, HBReset {
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


        Envelope playback_rate = new Envelope(1);

        SampleModule player = new SampleModule();
        player.setPlaybackRate(playback_rate);

        if (!player.setSample(player.EXAMPLE_SAMPLE_NAME))
        {
            hb.setStatus("Failed to load " + player.EXAMPLE_SAMPLE_NAME);
        }

        playback_rate.addSegment(0.5f, 1000);
        playback_rate.addSegment(0.5f, 2000);
        playback_rate.addSegment(1, 1000);
        playback_rate.addSegment(1, 2000);
        playback_rate.addSegment(2, 2000);
        playback_rate.addSegment(2, 1000, new KillTrigger(player.getKillTrigger()));

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
