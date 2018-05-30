package examples.events.envelope;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch layers three sounds on top of one another, triggered by an event at the completion of an envelope segment
 * A single envelope is used, however, we add segments to the envelope. When the first segment is completed, the next segment starts
 */
public class EnvelopeTrigger implements HBAction {
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        final float FREQUENCY = 1000; // this is the fundamental frequency of the  waveform we will make

        
        final float VOLUME = 0.1f; // define how loud we want the sound

        // create a wave player to generate a waveform based on frequency and waveform type
        WavePlayer waveformGenerator = new WavePlayer(FREQUENCY, Buffer.SINE);

        // The Envelope will function as both the volume and the event trigger
        Envelope eventTrigger = new Envelope(VOLUME);

        // set up a gain amplifier to control the volume
        Gain gainAmplifier = new Gain(NUMBER_AUDIO_CHANNELS, eventTrigger);

        // connect our WavePlayer object into the Gain object
        gainAmplifier.addInput(waveformGenerator);

        // Now plug the gain object into the audio output
        hb.ac.out.addInput(gainAmplifier);

        // now add some events
        final float SEGMENT_DURATION  = 4000; // THis is how long we will make our segments
        // we will not change the volume

        // Create a segment that will generate a beadMessage at the completion of the segment
        // At the completion of the segment duration, the bead messageReceived will be called and add another WavePlayer
        // Just type beadMessage and the code will be automatically generated for you
        eventTrigger.addSegment(VOLUME, SEGMENT_DURATION, new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                /*** Write your code below this line ***/
                // Create a WavePlayer that generates a waveform 2 times the frequency
                WavePlayer wp = new WavePlayer(FREQUENCY * 2, Buffer.SINE);
                gainAmplifier.addInput(wp);
                /*** Write your code above this line ***/

            }
        });

        // Create another segment that will generate a beadMessage at the completion of the segment
        // At the completion of the segment duration, the bead messageReceived will be called and add another WavePlayer
        // Just type beadMessage and the code will be automatically generated for you
        eventTrigger.addSegment(VOLUME, SEGMENT_DURATION, new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                /*** Write your code below this line ***/
                // Create a WavePlayer that generates a waveform 4 times the frequency
                WavePlayer wp = new WavePlayer(FREQUENCY * 4, Buffer.SINE);
                gainAmplifier.addInput(wp);

                /*** Write your code above this line ***/

            }
        });

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
