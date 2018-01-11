package net.happybrackets.examples;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * Play a simple sample with a loop
 */
public class SampleLoop implements HBAction {

    @Override
    public void action(HB hb) {
        // Define our sampler
        Sample sample = SampleManager.sample("data/audio/hiphop.wav");
        if (sample != null) {
            SamplePlayer sp = new SamplePlayer(hb.ac, sample);

            // define Loop Direction
            sp.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);

            // define our start and end points
            Glide  loopStart = new Glide(hb.ac, 0);
            Glide loopEnd = new Glide(hb.ac, 1000);
            sp.setLoopStart(loopStart);
            sp.setLoopEnd(loopEnd);

            // Connect our sample player to audio
            Gain g = new Gain(hb.ac, 1, 1);
            g.addInput(sp);
            hb.sound(g);
        }
    }

    /**
     * This function is used when running sketch in IntelliJ for debugging or testing
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
