package examples.samples;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch will play a three octave scale using the Clean_A_harm sample
 * We increment an index from zero, and each time the number incremented
 * it plays next note in the scale
 * This will perform a pitch shift on the sample so it will play the next frequency
 *
 *
 * Calls the function getRelativeMidiNote to get the number of semitones based on scale to shift
 *
 * When our last note reaches 3 octaves, we start again at base note
 */
public class SamplePitchShift implements HBAction {
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    // we need to define class variables so we can access them inside the clock

    // we will increment this number to get to next note is scale
    int nextScaleIndex = 0;
    final int NUMBER_OCTAVES = 3;

    final int ENVELOPE_EDGES = 20; // the edges of the audio volume envelope
    final int MAXIMUM_PITCH = 12 * NUMBER_OCTAVES; // This is the highest note we will play

    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        /**************************************************************
         * Load a sample and play it
         *
         * simply type basicSamplePLayer to generate this code and press <ENTER> for each parameter
         **************************************************************/
        
        final float INITIAL_VOLUME = 0; // define how loud we want the sound
        Envelope audioVolume = new Envelope(INITIAL_VOLUME);

        Glide sampleSpeed = new Glide(1);


        // Define our sample name
        final String SAMPLE_NAME = "data/audio/Nylon_Guitar/Clean_A_harm.wav";

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
            samplePlayer.setRate(sampleSpeed);


            /************************************************************
             * start clockTimer
             * Create a clock with a interval based on the clock duration
             *
             * To create this, just type clockTimer
             ************************************************************/
            // create a clock and start changing frequency on each beat
            final float CLOCK_INTERVAL = 300;

            // Create a clock with beat interval of CLOCK_INTERVAL ms
            Clock clock = new Clock(CLOCK_INTERVAL);

            // let us handle triggers
            clock.addMessageListener(new Bead() {
                @Override
                protected void messageReceived(Bead bead) {
                    // see if we are at the start of a beat
                    boolean start_of_beat = clock.getCount() % clock.getTicksPerBeat() == 0;
                    if (start_of_beat) {
                    /*** Write your code to perform functions on the beat below this line ****/

                    // first create our envelope to play sound
                    audioVolume.addSegment(1, ENVELOPE_EDGES);
                    audioVolume.addSegment(1, CLOCK_INTERVAL - ENVELOPE_EDGES * 3);
                    audioVolume.addSegment(0, ENVELOPE_EDGES);



                    // we are going to next note in scale
                    nextScaleIndex++;

                    // Get the MIDI Amount to shift the note based on Major scale
                    int key_note = Pitch.getRelativeMidiNote(0, Pitch.major, nextScaleIndex);

                    // if it exceeds our maximum, then start again
                    if (key_note > MAXIMUM_PITCH)
                    {
                        nextScaleIndex = 0;
                    }

                    double sample_multiplier = Pitch.shiftPitch(1, nextScaleIndex);

                    sampleSpeed.setValue((float)sample_multiplier);
                    samplePlayer.setPosition(0);

                    /*** Write your code to perform functions on the beat above this line ****/
                } else {
                    /*** Write your code to perform functions off the beat below this line ****/

                    /*** Write your code to perform functions off the beat above this line ****/
                }
            }
        });
        /*********************** end clockTimer **********************/
            /******** Write your code above this line ********/
        } else {
            hb.setStatus("Failed sample " + SAMPLE_NAME); }
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
