package examples.events.clocks;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.AccelerometerListener;

import java.lang.invoke.MethodHandles;

/**
 * This sketch will create a WavePlayer will play a whole tone scale through a clock
 * The frequency of the WavePlayer is calculated by using the MIDI to Frequency function -- Pitch.mtof
 * The note number is incremented by two each clock beat, resulting in a whole tone scale
 * When the note number has reached our defined END_NOTE, we will move back to start note
 *
 * The x value of an accelerometer is used to change the clock interval
 */
public class SensorControlledClock implements HBAction {

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
        
        final float INITIAL_VOLUME = 0.1f; // define how loud we want the sound
        Glide audioVolume = new Glide(INITIAL_VOLUME);


        // We will convert our NOte number to a frequency
        float next_frequency = Pitch.mtof(currentNote);
        //create an object we can use to modify frequency of WavePlayer.
        Glide waveformFrequency = new Glide(next_frequency);


        // create a wave player to generate a waveform based on waveformFrequency and waveform type
        WavePlayer waveformGenerator = new WavePlayer(waveformFrequency, Buffer.SQUARE);

        // set up a gain amplifier to control the volume
        Gain gainAmplifier = new Gain(NUMBER_AUDIO_CHANNELS, audioVolume);

        // connect our WavePlayer object into the Gain object
        gainAmplifier.addInput(waveformGenerator);

        // Now plug the gain object into the audio output
        hb.ac.out.addInput(gainAmplifier);

        final double CLOCK_INTERVAL = 500;

        /************************************************************
         * To create this, just type clockTimer
         ************************************************************/
        Clock clock = hb.createClock(CLOCK_INTERVAL).addClockTickListener((offset, this_clock) -> {
            /*** Write your Clock tick event code below this line ***/
            if (currentNote < END_NOTE) {
                // add 2 to the current note
                currentNote += 2;
            }
            else {
                currentNote = START_NOTE;
            }

            // convert or not number to a frequency
            float new_frequency = Pitch.mtof(currentNote);

            waveformFrequency.setValue(new_frequency);

            /*** Write your Clock tick event code above this line ***/
        });

        clock.start();
        /******************* End Clock Timer *************************/

        // Let us start changing the speed of the clock with an accelerometer

        /*****************************************************
         * Find an accelerometer sensor. If no sensor is found
         * you will receive a status message
         * accelerometer values typically range from -1 to + 1
         * to create this code, simply type accelerometerSensor
         *****************************************************/
        new AccelerometerListener(hb) {
            @Override
            public void sensorUpdate(float x_val, float y_val, float z_val) {
                /******** Write your code below this line ********/
                // convert our x_val to be between 1 and three
                float converted_x = x_val + 2;

                // just make sure that converted is not zero, otherwise we will get a divide by zero error
                if (converted_x > 0.0f) {

                    // now set our interval so clock is faster on higher number
                    // a lower interval means a higher speed
                    clock.setInterval(CLOCK_INTERVAL / converted_x);
                }
                /******** Write your code above this line ********/

            }
        };
        /*** End accelerometerSensor code ***/

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
