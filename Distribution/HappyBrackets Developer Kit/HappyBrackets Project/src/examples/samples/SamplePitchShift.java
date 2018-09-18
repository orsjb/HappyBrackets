package examples.samples;


import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.instruments.SampleModule;
import net.happybrackets.core.scheduling.Clock;
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

        final float INITIAL_VOLUME = 0; // define how loud we want the sound
        Envelope audioVolume = new Envelope(INITIAL_VOLUME);

        /* type basicSamplePLayer to generate this code */
        // define our sample name
        final String sample_name = "data/audio/Nylon_Guitar/Clean_A_harm.wav";
        SampleModule samplePlayer = new SampleModule();
        if (samplePlayer.setSample(sample_name)) {/* Write your code below this line */
            samplePlayer.connectTo(hb.ac.out);

            /* Write your code above this line */
        } else {
            hb.setStatus("Failed sample " + sample_name);
        }/* End samplePlayer code */


        // make an interval for 240 beats per minute
        double CLOCK_INTERVAL = Clock.BPM2Interval(240);
        /************************************************************
         * To create this, just type clockTimer
         ************************************************************/
        Clock hbClock = hb.createClock(CLOCK_INTERVAL).addClockTickListener((offset, this_clock) -> {
            /*** Write your Clock tick event code below this line ***/
            // first create our envelope to play sound
            audioVolume.addSegment(1, ENVELOPE_EDGES);
            audioVolume.addSegment(1, (float)CLOCK_INTERVAL - ENVELOPE_EDGES * 3);
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

            samplePlayer.setRate(sample_multiplier);
            samplePlayer.setPosition(0);

            /*** Write your Clock tick event code above this line ***/
        });

        hbClock.start();
        /******************* End Clock Timer *************************/


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
