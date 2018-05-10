package examples.basic;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch generates a 1 Khz square wave and plays it through a gain object and output to the device
 * We will be using an Envelope object to change the volume of the Gain
 * The volume will start at 0 (silence) and progress to 0.5 over 1 second.
 * The volume will hold steady for 3 seconds and then fade out over 10 seconds
 */
public class GainEnvelope implements HBAction {
    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();


        final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
        final float INITIAL_FREQUENCY = 1000; // this is the frequency of the waveform we will make

        // define the different levels we will be using in our envelope
        final float MIN_VOLUME = 0f;   // this is silence
        final float MAX_VOLUME = 0.3f; // This is the high frequency of the waveform we will make

        // define the times it takes to reach the points in our envelope
        final float RAMP_UP_VOLUME_TIME = 500; // half a second (time is in milliseconds)
        final float HOLD_VOLUME_TIME = 3000; // 3 seconds
        final float FADEOUT_TIME = 5000; // 10 seconds

        Glide audioFrequency = new Glide(hb.ac, INITIAL_FREQUENCY);

        // Create our envelope using MIN_VOLUME as the starting value
        Envelope gainEnvelope = new Envelope(hb.ac, MIN_VOLUME);

        // create a wave player to generate a waveform using the FREQUENCY and a Square wave
        WavePlayer waveformGenerator = new WavePlayer(hb.ac, audioFrequency, Buffer.SQUARE);

        // set up a gain amplifier to control the volume. We will use the gainEnvelope object to change volume
        Gain gainAmplifier = new Gain(hb.ac, NUMBER_AUDIO_CHANNELS, gainEnvelope);

        // connect our WavePlayer object into the Gain object
        gainAmplifier.addInput(waveformGenerator);

        // Now plug the gain object into the audio output
        hb.ac.out.addInput(gainAmplifier);

        // Now start changing the level of gainEnvelope
        // first add a segment to progress to the higher volume
        gainEnvelope.addSegment(MAX_VOLUME, RAMP_UP_VOLUME_TIME);

        // now add a segment to make the gainEnvelope stay at that volume
        // we do this by setting the start of the segment to the value as our MAX_VOLUME
        gainEnvelope.addSegment(MAX_VOLUME, HOLD_VOLUME_TIME);

        //Now make our gain fade out to MIN_VOLUME
        gainEnvelope.addSegment(MIN_VOLUME, FADEOUT_TIME);
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
