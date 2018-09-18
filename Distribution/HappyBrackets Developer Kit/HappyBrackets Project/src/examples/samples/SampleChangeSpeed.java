package examples.samples;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.instruments.SampleModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * The sketch demonstrates how to change the speed of a sample player using an envelope
 * We add segments for each change in speed and hold
 * We start the playback at normal speed. We hold it for 5 seconds using an envelope
 * after hold segment is complete, we move to half speed over and then hold
 * after hold segment is complete, we move to double speed over and then hold
 * after hold segment is complete, we move to normal speed over and then hold
 * after hold segment is complete, we move to reverse and then hold. We kill the gain at the end of the segment
 */
public class SampleChangeSpeed implements HBAction {
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        /* type basicSamplePLayer to generate this code */
        // define our sample name
        final String sample_name = "data/audio/Roje/i-write.wav";
        SampleModule samplePlayer = new SampleModule();
        if (samplePlayer.setSample(sample_name)) {/* Write your code below this line */
            samplePlayer.connectTo(hb.ac.out);

            /* Write your code above this line */
        } else {
            hb.setStatus("Failed sample " + sample_name);
        }/* End samplePlayer code */

        // define our speeds
        final float NORMAL_SPEED = 1;
        final float HALF_SPEED = 0.5f;
        final float DOUBLE_SPEED = 2;
        final float REVERSE = -1;

        // define how long we want to envelope segments to be
        final int SEGMENT_DURATION = 5000; // five seconds

        // Create an envelope to change speed
        Envelope speedEnvelope = new Envelope(NORMAL_SPEED);

        // set our sample player to use this envelope as its playback speed
        samplePlayer.setRate(speedEnvelope);

        // add a segment for each speed we want to play at

        // hold speed
        speedEnvelope.addSegment(NORMAL_SPEED, SEGMENT_DURATION);

        // Change speed
        speedEnvelope.addSegment(HALF_SPEED, SEGMENT_DURATION);
        // hold speed
        speedEnvelope.addSegment(HALF_SPEED, SEGMENT_DURATION);

        // change back to normal speed
        speedEnvelope.addSegment(NORMAL_SPEED, SEGMENT_DURATION);
        // hold normal speed
        speedEnvelope.addSegment(NORMAL_SPEED, SEGMENT_DURATION);

        // Change speed
        speedEnvelope.addSegment(DOUBLE_SPEED, SEGMENT_DURATION);
        // hold speed
        speedEnvelope.addSegment(DOUBLE_SPEED, SEGMENT_DURATION);

        // change back to normal speed
        speedEnvelope.addSegment(NORMAL_SPEED, SEGMENT_DURATION);
        // hold normal speed
        speedEnvelope.addSegment(NORMAL_SPEED, SEGMENT_DURATION);

        // Change speed
        speedEnvelope.addSegment(REVERSE, SEGMENT_DURATION);
        // hold speed. Then kill our playback at the end
        speedEnvelope.addSegment(REVERSE, SEGMENT_DURATION, new KillTrigger(samplePlayer.getKillTrigger()));



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
