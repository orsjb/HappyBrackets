package examples.midi.scales;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch will play a three octave diminished scale starting from MIDI note C2
 * We increment an index from zero, and each time the number incremented
 * it plays next note in the scale
 * Diminshed scale is TSTSTSTS (tone, semitone, tone, semitone, etc ...)
 * Uses function Pitch.getRelativeMidiNote
 *
 * When our last note reaches 3 octaves, we start again at base note
 */
public class CustomScale implements HBAction {

    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    // we need to define class variables so we can access them inside the clock

    // we will increment this number to get to next note is scale

    int nextScaleIndex = 0;
    final int BASE_TONIC = 48; // This will be the tonic of our scale. This correlates to C2 in MIDI

    final int NUMBER_OCTAVES = 3;

    final int MAXIMUM_PITCH = BASE_TONIC + 12 * NUMBER_OCTAVES; // This is the highest note we will play

    // define our pitch scale
    final int[] DIMINISHED_SCALE = {0, 2, 3, 5, 6, 8, 9, 11};

    @Override
    public void action(HB hb) {
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        WaveModule player = new WaveModule();
        player.setMidiFrequency(BASE_TONIC);
        player.setBuffer(Buffer.SQUARE);
        player.connectTo(hb.ac.out);


        /************************************************************
         * To create this, just type clockTimer
         ************************************************************/
        net.happybrackets.core.scheduling.Clock hbClock = hb.createClock(300).addClockTickListener((offset, this_clock) -> {
            /*** Write your Clock tick event code below this line ***/
            // we are going to next note in scale
            nextScaleIndex++;

            // Get the Midi note number based on Scale
            int key_note = Pitch.getRelativeMidiNote(BASE_TONIC, DIMINISHED_SCALE, nextScaleIndex);


            // if it exceeds our maximum, then start again
            if (key_note > MAXIMUM_PITCH)
            {
                key_note = BASE_TONIC;
                nextScaleIndex = 0;
            }


            player.setMidiFrequency(key_note);

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
