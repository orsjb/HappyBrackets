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
 * The scale degree and register uses modulo and division.
 * For example, Consider we want to play the note 9 scale degrees above.
 * This would mean we would need to play the note which is a major third in the next octave
 * which is a 2 in the next register. 
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


    /**
     * Create a table of sample rate multipliers, one for each semitone
     * @return a table of 12 frequency multipliers, one for each semitone degree
     */
    static double [] createRateMultipliers(){
        final double SEMITONE_CONSTANT =  Math.pow(2 , 1f/12f);
        double [] ret = new double[12];

        for (int i = 0; i < ret.length; i++){
            ret [i] = Math.pow(SEMITONE_CONSTANT, i);
        }
        return ret;
    }

    final double [] MULTIPLIER_TABLE = createRateMultipliers();

    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();

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

                    // get the scale degree from the scale
                    // eg, if nextScaleIndex is 9, scale_degree = 9 % 7 = 2
                    int scale_degree = nextScaleIndex % Pitch.major.length;

                    // If our scale pitch is 2,Pitch.major[2] = 4 (major third)
                    int scale_pitch = Pitch.major[scale_degree];

                    // Get the multiplier of scale based on semitones
                    double pitch_multiplier = MULTIPLIER_TABLE[scale_pitch];

                    // Now get the register of our note
                    // eg, if nextScaleIndex is 9, scale_degree = 9 / 7 = 1
                    int note_register = nextScaleIndex / Pitch.major.length;

                    // get our multiplier of octaves on register
                    double register_multiplier = Math.pow(2, note_register);

                    // calculate how much we will multipley sample playback rate
                    double sample_multiplier = pitch_multiplier * register_multiplier;

                    sampleSpeed.setValue((float)sample_multiplier);
                    samplePlayer.setPosition(0);

                    // we multiply our register x 12 because that is an octave in MIDI
                    // if nextScaleIndex is 9 then 1 x 12 + 4 = 16
                    int note_pitch = note_register * 12 + scale_pitch;

                    // add the number to our base tonic to get the note based on key
                    // if nextScaleIndex is 9 then 48 + 16 = 64. This is E3 in MIDI
                    int key_note = note_pitch;

                    // if it exceeds our maximum, then start again
                    if (key_note > MAXIMUM_PITCH)
                    {
                        nextScaleIndex = 0;
                    }


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
