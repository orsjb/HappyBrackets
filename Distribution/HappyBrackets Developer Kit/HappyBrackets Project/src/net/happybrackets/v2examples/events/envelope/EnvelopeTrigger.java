package net.happybrackets.v2examples.events.envelope;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class EnvelopeTrigger implements HBAction {
    @Override
    public void action(HB hb) {

        final float FREQUENCY = 1000; // this is the frequency of the waveform we will make

        final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
        final float VOLUME = 0.1f; // define how loud we want the sound

        // create a wave player to generate a waveform based on frequency and waveform type
        WavePlayer waveformGenerator = new WavePlayer(hb.ac, FREQUENCY, Buffer.SINE);

        // The Envelope will function as both the volume and the event trigger
        Envelope eventTrigger = new Envelope(hb.ac, VOLUME);

        // set up a gain amplifier to control the volume
        Gain gainAmplifier = new Gain(hb.ac, NUMBER_AUDIO_CHANNELS, eventTrigger);

        // connect our WavePlayer object into the Gain object
        gainAmplifier.addInput(waveformGenerator);

        // Now plug the gain object into the audio output
        hb.ac.out.addInput(gainAmplifier);

        // now add some events
        final float SEGMENT_DURATION  = 4000; // THis is how long we will make our segments
        // we will not change the volume


        eventTrigger.addSegment(VOLUME, SEGMENT_DURATION, new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                /*** Write your code below this line ***/

                /*** Write your code above this line ***/

            }
        });

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