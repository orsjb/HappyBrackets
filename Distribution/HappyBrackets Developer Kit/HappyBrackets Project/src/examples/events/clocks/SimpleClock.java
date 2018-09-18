package examples.events.clocks;


import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch will create a WavePlayer will play a chromatic scale through a clock
 * The frequency of the WavePlayer is calculated by using the MIDI to Frequency function -- Pitch.mtof
 * The note number is incremented each clock beat, resulting in a chromatic scale
 * When the note number has reached our defined END_NOTE, the gain amplifier is killed and playback stops
 */
public class SimpleClock implements HBAction {
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    // These parameters need to be class variables so they can be accessed within the clock
    final int START_NOTE = 40; // this is the MIDI number of first note
    final int END_NOTE = 110;  // this is the last note we will play
    // define a variable to calulate and store next frequency
    int currentNote = START_NOTE;

    @Override
    public void action(HB hb) {
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        WaveModule player = new WaveModule();

        player.setBuffer(Buffer.SQUARE);
        player.setMidiFequency(currentNote);

        player.connectTo(hb.ac.out);



        // create a clock and start changing frequency on each beat
        final float CLOCK_DURATION = 300;

        /* To create this, just type clockTimer */
        Clock hbClock = hb.createClock(CLOCK_DURATION).addClockTickListener((offset, this_clock) -> {/* Write your code below this line */
            if (currentNote < END_NOTE) {
                // move to the next chromatic note
                currentNote++;

                player.setMidiFequency(currentNote);
            }
            else
            {
                // Kill our player and stop the clock
                player.getKillTrigger().kill();
                this_clock.stop();
            }

            /* Write your code above this line */
        });

        hbClock.start();/* End Clock Timer */



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
