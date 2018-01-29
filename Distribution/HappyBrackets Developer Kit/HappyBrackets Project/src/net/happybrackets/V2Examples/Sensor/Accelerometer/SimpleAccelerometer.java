package net.happybrackets.V2Examples.Sensor.Accelerometer;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

public class SimpleAccelerometer implements HBAction{
    @Override
    public void action(HB hb) {
        final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
        final float VOLUME = 0.1f; // define how loud we want the sound


        // define the  frequency we will multiply our accelerometer value with
        final float MULTIPLIER_FREQUENCY = 1000;

        // Create a Glide object so we can set the frequency of wavePlayer
        Glide waveformFrequency = new Glide(hb.ac, 0);

        // create a wave player to generate a waveform based on frequency and waveform type
        WavePlayer waveformGenerator = new WavePlayer(hb.ac, waveformFrequency, Buffer.SINE);

        // set up a gain amplifier to control the volume
        Gain gainAmplifier = new Gain(hb.ac, NUMBER_AUDIO_CHANNELS, VOLUME);

        // connect our WavePlayer object into the Gain object
        gainAmplifier.addInput(waveformGenerator);

        // Now plug the gain object into the audio output
        hb.ac.out.addInput(gainAmplifier);
    }
}
