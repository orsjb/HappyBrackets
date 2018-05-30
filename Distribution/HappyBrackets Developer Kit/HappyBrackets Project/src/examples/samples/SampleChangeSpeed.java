package examples.samples;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
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

        /**************************************************************
         * Load a sample and play it
         *
         * simply type samplePLayer-basic to generate this code and press <ENTER> for each parameter
         **************************************************************/
        
        final float INITIAL_VOLUME = 1f; // define how loud we want the sound
        Glide audioVolume = new Glide(INITIAL_VOLUME);

        // Define our sample name
        final String SAMPLE_NAME = "data/audio/Roje/i-write.wav";

        // create our actual sample
        Sample sample = SampleManager.sample(SAMPLE_NAME);

        // test if we opened the sample successfully
        if (sample != null) {
            // Create our sample player
            SamplePlayer samplePlayer = new SamplePlayer(sample);

            // Samples are killed by default at end. We will stop this default actions so our sample will stay alive
            samplePlayer.setKillOnEnd(false);

            // Connect our sample player to audio
            Gain gainAmplifier = new Gain(NUMBER_AUDIO_CHANNELS, audioVolume);
            gainAmplifier.addInput(samplePlayer);
            hb.ac.out.addInput(gainAmplifier);

            /******** Write your code below this line ********/

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
            speedEnvelope.addSegment(REVERSE, SEGMENT_DURATION, new KillTrigger(gainAmplifier));


            /******** Write your code above this line ********/
        } else {
            hb.setStatus("Failed sample " + SAMPLE_NAME);
        }
        /*** End samplePlayer code ***/

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
