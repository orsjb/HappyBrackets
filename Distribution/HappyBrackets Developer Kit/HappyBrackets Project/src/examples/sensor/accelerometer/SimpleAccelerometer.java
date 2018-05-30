package examples.sensor.accelerometer;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.AccelerometerListener;


import java.lang.invoke.MethodHandles;

/**
 * This sketch will play a sine wave whose frequency is dependant upon x axis of accelerometer
 * accelerometer values typically range from -1 to +1, so we convert this value to 0 to 2, and
 * then multiply it to get a new frequency.
 * We use a Glide object as the frequency value input to a WavePlayer object
 */
public class SimpleAccelerometer implements HBAction{
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        final float INITIAL_VOLUME = 0.1f; // define how loud we want the sound
        Glide audioVolume = new Glide(INITIAL_VOLUME);


        // define the  frequency we will multiply our accelerometer value with
        final float MULTIPLIER_FREQUENCY = 1000;

        // Create a Glide object so we can set the frequency of wavePlayer.
        // The initial value is our MULTIPLIER_FREQUENCY
        Glide waveformFrequency = new Glide(MULTIPLIER_FREQUENCY);

        // create a wave player to generate a waveform based on frequency and waveform type
        WavePlayer waveformGenerator = new WavePlayer(waveformFrequency, Buffer.SINE);

        // set up a gain amplifier to control the volume
        Gain gainAmplifier = new Gain(NUMBER_AUDIO_CHANNELS, audioVolume);

        // connect our WavePlayer object into the Gain object
        gainAmplifier.addInput(waveformGenerator);

        // Now plug the gain object into the audio output
        hb.ac.out.addInput(gainAmplifier);

        // create our accelerometer and connect
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
                // convert our x_value so it is between 0 and 2
                float converted_x = x_val + 1;

                // calculate our new frequency

                float new_frequency = converted_x * MULTIPLIER_FREQUENCY;

                // set new value to waveformFrequency
                waveformFrequency.setValue(new_frequency);
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
