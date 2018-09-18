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
 * This sketch will play a three octave scale starting from MIDI note C2
 * We increment an index from zero, and each time the number incremented
 * it plays next note in the scale
 *
 * Uses function Pitch.getRelativeMidiNote to obtain the MIDI note
 * When our last note reaches 3 octaves, we start again at base note
 */
public class MajorScale implements HBAction {

    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    // we need to define class variables so we can access them inside the clock

    // we will increment this number to get to next note is scale
    int nextScaleIndex = 0;
    final int BASE_TONIC = 48; // This will be the tonic of our scale. This correlates to C2 in MIDI

    final int NUMBER_OCTAVES = 3;

    final int MAXIMUM_PITCH = BASE_TONIC + 12 * NUMBER_OCTAVES; // This is the highest note we will play

    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        WaveModule player = new WaveModule();
        player.setMidiFequency(BASE_TONIC);
        player.setBuffer(Buffer.SQUARE);
        player.connectTo(hb.ac.out);


        /************************************************************
         * start clockTimer
         * Create a clock with a interval based on the clock duration
         ************************************************************/
        // create a clock and start changing frequency on each beat
        final float CLOCK_INTERVAL = 300;



        /* To create this, just type clockTimer */
        net.happybrackets.core.scheduling.Clock clock = hb.createClock(CLOCK_INTERVAL).addClockTickListener((offset, this_clock) -> {/* Write your code below this line */
            // we are going to next note in scale
            nextScaleIndex++;

            // Get the Midi note number based on Scale
            int key_note = Pitch.getRelativeMidiNote(BASE_TONIC, Pitch.major, nextScaleIndex);

            // if it exceeds our maximum, then start again and use our start
            if (key_note > MAXIMUM_PITCH)
            {
                key_note = BASE_TONIC;
                nextScaleIndex = 0;
            }

            // convert our MIDI pitch to a frequency
            player.setMidiFequency(key_note);


            /* Write your code above this line */
        });

        clock.start();/* End Clock Timer */


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
