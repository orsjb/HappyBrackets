package net.happybrackets.v2examples.events.clocks;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch will create a WavePlayer will play a whole tone scale through a clock
 * The frequency of the WavePlayer is calculated by using the MIDI to Frequency function -- Pitch.mtof
 * The note number is incremented by two each clock beat, resulting in a whole tone scale
 * When the note number has reached our defined END_NOTE, we will go back to start
 *
 * An envelope is created that will change the speed of the clock by changing its interval
 * We set the clock to use the envelope output as its interval
 * Adding segments to the envelope will add additional clock speed changes
 */
public class VariableClock implements HBAction {

    // These parameters need to be class variables so they can be accessed within the clock
    final int START_NOTE = 40; // this is the MIDI number of first note
    final int END_NOTE = 110;  // this is the last note we will play
    // define a variable to calulate and store next frequency
    int currentNote = START_NOTE;

    @Override
    public void action(HB hb) {

        final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
        final float VOLUME = 0.1f; // define how loud we want the sound


        // We will convert our NOte number to a frequency
        float next_frequency = Pitch.mtof(currentNote);
        //create an object we can use to modify frequency of WavePlayer.
        Glide waveformFrequency = new Glide(hb.ac, next_frequency);


        // create a wave player to generate a waveform based on waveformFrequency and waveform type
        WavePlayer waveformGenerator = new WavePlayer(hb.ac, waveformFrequency, Buffer.SQUARE);

        // set up a gain amplifier to control the volume
        Gain gainAmplifier = new Gain(hb.ac, NUMBER_AUDIO_CHANNELS, VOLUME);

        // connect our WavePlayer object into the Gain object
        gainAmplifier.addInput(waveformGenerator);

        // Now plug the gain object into the audio output
        hb.ac.out.addInput(gainAmplifier);

        /************************************************************
         * start clockTimer
         * Create a clock with a interval based on the clock duration
         *
         * To create this, just type clockTimer
         ************************************************************/
        // create a clock and start changing frequency on each beat
        final float CLOCK_INTERVAL = 300;

        // Create a clock with beat interval of CLOCK_INTERVAL ms
        Clock clock = new Clock(hb.ac, CLOCK_INTERVAL);
        // connect the clock to HB
        hb.ac.out.addDependent(clock);

        // let us handle triggers
        clock.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                // see if we are at the start of a beat
                boolean start_of_beat = clock.getCount() % clock.getTicksPerBeat() == 0;
                if (start_of_beat) {
                    /*** Write your code to perform functions on the beat below this line ****/

                    if (currentNote < END_NOTE) {
                        // add 2 to the current note
                        currentNote += 2;
                    }
                    else {
                        currentNote = START_NOTE;
                    }

                    // convert or not number to a frequency
                    float next_frequency = Pitch.mtof(currentNote);

                    waveformFrequency.setValue(next_frequency);


                    /*** Write your code to perform functions on the beat above this line ****/
                } else {
                    /*** Write your code to perform functions off the beat below this line ****/

                    /*** Write your code to perform functions off the beat above this line ****/
                }
            }
        });
        /*********************** end clockTimer **********************/

        // let us change the speed of the clock with an envelope

        // define the envelope we will use
        Envelope clockInterval = new Envelope(hb.ac, CLOCK_INTERVAL);
        clock.setIntervalEnvelope(clockInterval);

        // Let us start changing the speed of the clock

        // define envelope segment duration
        final int ENVELOPE_SEGMENT_LENGTH = 5000;

        float next_envelope_clock_interval = CLOCK_INTERVAL;

        // Hold clock for ENVELOPE_SEGMENT_LENGTH milliseconds
        clockInterval.addSegment(next_envelope_clock_interval, ENVELOPE_SEGMENT_LENGTH);

        // set next clock duration. increase to double speed by halving interval
        next_envelope_clock_interval = CLOCK_INTERVAL / 2;

        // Now change to new speed over ENVELOPE_SEGMENT_LENGTH milliseconds
        clockInterval.addSegment(next_envelope_clock_interval, ENVELOPE_SEGMENT_LENGTH);

        // Hold clock for ENVELOPE_SEGMENT_LENGTH milliseconds
        clockInterval.addSegment(next_envelope_clock_interval, ENVELOPE_SEGMENT_LENGTH);

        // set next clock duration back to original
        next_envelope_clock_interval = CLOCK_INTERVAL;

        // Now change to new speed over ENVELOPE_SEGMENT_LENGTH milliseconds
        clockInterval.addSegment(next_envelope_clock_interval, ENVELOPE_SEGMENT_LENGTH);

        // Hold clock for ENVELOPE_SEGMENT_LENGTH milliseconds
        clockInterval.addSegment(next_envelope_clock_interval, ENVELOPE_SEGMENT_LENGTH);


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
