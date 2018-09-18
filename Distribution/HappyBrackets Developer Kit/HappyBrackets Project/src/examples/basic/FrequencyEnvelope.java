package examples.basic;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch generates a variable frequency sine wave and plays it through a gain object and output to the device
 * We will be using an Envelope object to change the frequency of the wavePlayer
 * The frequency will start at 500Hz and progress to 2Khz over 1 second.
 * The frequency will hold for 3 seconds and then move back to 500Hz over 5 seconds
 * After holding again for 3 seconds, we will kill the gain control to stop sound
 */
public class FrequencyEnvelope implements HBAction {
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using

    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        final float INITIAL_VOLUME = 0.1f; // Define how loud we want our sound

        // define the different frequencies we will be using in our envelope
        final float LOW_FREQUENCY = 500;   // this is the low frequency of the waveform we will make
        final float HIGH_FREQUENCY = 2000; // This is the high frequency of the waveform we will make

        // define the times it takes to reach the points in our envelope
        final float RAMP_UP_FREQUENCY_TIME = 1000; // 1 second (time is in milliseconds)
        final float HOLD_FREQUENCY_TIME = 3000; // 3 seconds
        final float RAMP_DOWN_FREQUENCY_TIME = 5000; // 5 seconds


        // Create our envelope using LOW_FREQUENCY as the starting value
        Envelope frequencyEnvelope = new Envelope(LOW_FREQUENCY);

        // create a wave player to generate a waveform using the frequencyEnvelope and a sinewave
        WavePlayer waveformGenerator = new WavePlayer(frequencyEnvelope, Buffer.SINE);

        WaveModule player = new WaveModule(frequencyEnvelope, INITIAL_VOLUME, Buffer.SINE);
        player.connectTo(hb.ac.out);


        // Now start changing the frequency of frequencyEnvelope
        // first add a segment to progress to the higher frequency over 5 seconds
        frequencyEnvelope.addSegment(HIGH_FREQUENCY, RAMP_UP_FREQUENCY_TIME);

        // now add a segment to make the frequencyEnvelope stay at that frequency
        // we do this by setting the start of the segment to the value as our HIGH_FREQUENCY
        frequencyEnvelope.addSegment(HIGH_FREQUENCY, HOLD_FREQUENCY_TIME);

        //Now make our frequency go back to the lower frequency
        frequencyEnvelope.addSegment(LOW_FREQUENCY, RAMP_DOWN_FREQUENCY_TIME);

        //Now make our frequency hold to the lower frequency, and after holding, kill our gainAmplifier
        frequencyEnvelope.addSegment(LOW_FREQUENCY, HOLD_FREQUENCY_TIME, new KillTrigger(player.getKillTrigger()));
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
