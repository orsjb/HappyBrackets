package net.happybrackets.v2examples.sensor.gyroscope;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.Gyroscope;
import net.happybrackets.device.sensors.SensorNotFoundException;

import java.lang.invoke.MethodHandles;

/**
 * This sketch will play a sine wave whose frequency is dependant upon x axis of accelerometer
 * accelerometer values typically range from -1 to +1, so we convert this value to 0 to 2, and
 * then multiply it to get a new frequency.
 * We use a Glide object as the frequency value input to a WavePlayer object
 */
public class SimpleGyroscope implements HBAction{
    @Override
    public void action(HB hb) {
        final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
        final float VOLUME = 0.1f; // define how loud we want the sound


        // define the  frequency we will multiply our accelerometer value with
        final float MULTIPLIER_FREQUENCY = 1000;

        // Create a Glide object so we can set the frequency of wavePlayer.
        // The initial value is our MULTIPLIER_FREQUENCY
        Glide waveformFrequency = new Glide(hb.ac, MULTIPLIER_FREQUENCY);

        // create a wave player to generate a waveform based on frequency and waveform type
        WavePlayer waveformGenerator = new WavePlayer(hb.ac, waveformFrequency, Buffer.SINE);

        // set up a gain amplifier to control the volume
        Gain gainAmplifier = new Gain(hb.ac, NUMBER_AUDIO_CHANNELS, VOLUME);

        // connect our WavePlayer object into the Gain object
        gainAmplifier.addInput(waveformGenerator);

        // Now plug the gain object into the audio output
        hb.ac.out.addInput(gainAmplifier);

        // create our accelerometer and connect

        /*****************************************************
         * Find an gyroscope sensor. If no sensor is found
         * you will receive a status message
         *
         * to create this code, simply type gyroscopeSensor
         *****************************************************/
        try {
            hb.findSensor(Gyroscope.class).addValueChangedListener(sensor -> {
                Gyroscope gyroscope = (Gyroscope) sensor;
                float pitch = gyroscope.getPitch();
                float roll = gyroscope.getRoll();
                float yaw = gyroscope.getYaw();

                /******** Write your code below this line ********/


                /******** Write your code above this line ********/

            });

        } catch (SensorNotFoundException e) {
            hb.setStatus("Unable to find gyroscope");
        }
        /*** End gyroscopeSensor code ***/




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
