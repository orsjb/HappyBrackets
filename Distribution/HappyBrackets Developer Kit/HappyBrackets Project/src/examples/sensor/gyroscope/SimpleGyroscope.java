package examples.sensor.gyroscope;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Gyroscope;
import net.happybrackets.device.sensors.GyroscopeListener;
import net.happybrackets.device.sensors.SensorNotFoundException;

import java.lang.invoke.MethodHandles;

/**
 * This sketch will play a sine wave whose frequency is dependant upon yaw of gyroscope
 * gyroscope value is zero when device is stationary, so the sound will change pitch and then go back
 * Rotate device to change gyroscope value
 * we will use a multiplier to change the frequency
 * We use a Glide object as the frequency value input to a WavePlayer object
 */
public class SimpleGyroscope implements HBAction{
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    @Override
    public void action(HB hb) {
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");
        
        
        final float INITIAL_VOLUME = 0.1f; // define how loud we want the sound
        Glide audioVolume = new Glide(INITIAL_VOLUME);


        // define the centre frequency we will use
        final float CENTRE_FREQUENCY = 1000;

        // define the amount we will change the waveformFrequency by based on gyroscope value
        final float MULTIPLIER_FREQUENCY = 500;

        // Create a Glide object so we can set the frequency of wavePlayer.
        // The initial value is our MULTIPLIER_FREQUENCY
        Glide waveformFrequency = new Glide(CENTRE_FREQUENCY);

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
         * Add a gyroscope sensor listener. *
         * to create this code, simply type gyroscopeSensor
         *****************************************************/
        new GyroscopeListener(hb) {
            @Override
            public void sensorUpdated(float pitch, float roll, float yaw) {
                /******** Write your code below this line ********/
                float frequency_deviation = yaw * MULTIPLIER_FREQUENCY;
                waveformFrequency.setValue(CENTRE_FREQUENCY + frequency_deviation);
                /******** Write your code above this line ********/
            }
        };
        /*** End gyroscopeSensor code ***/

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
