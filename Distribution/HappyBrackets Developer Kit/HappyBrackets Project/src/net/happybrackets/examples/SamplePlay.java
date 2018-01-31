package net.happybrackets.examples;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.lang.invoke.MethodHandles;

/**
 * Play a simple sample
 */
public class SamplePlay implements HBAction {

    @Override
    public void action(HB hb) {
        // Define our sampler
        Sample sample = SampleManager.sample("data/audio/Roje/i-write.wav");
        if (sample != null) {
            SamplePlayer sp = new SamplePlayer(hb.ac, sample);

            // Connect our sample player to audio
            Gain g = new Gain(hb.ac, 1, 1);
            g.addInput(sp);
            hb.ac.out.addInput(g);
        }
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
