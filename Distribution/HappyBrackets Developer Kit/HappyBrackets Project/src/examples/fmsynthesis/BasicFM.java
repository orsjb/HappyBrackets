package examples.fmsynthesis;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * basic FM wave generating a 1KHz carrier with a depth of 500Hz
 * Modulating at a rate of 1Hz
 * We will be using a function to calculate the frequency the carrier wavePlayer needs to be at
 */
public class BasicFM implements HBAction {
    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();

        final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
        final float INITIAL_VOLUME = 0.1f; // define how loud we want the sound
        Glide audioVolume = new Glide(hb.ac, INITIAL_VOLUME);

        final float CARRIER_FREQUENCY = 1000; //This is the centre frequency
        final float MODULATOR_DEPTH_FREQUENCY = 500;    // THis is how much we will change the centre frequency
        final float MODULATOR_RATE_FREQUENCY = 1;  // This is the rate at which we will change the frequency

        // We need to create UGen objects so we can use them inside a function
        //these are the parameters that will be used to calculate the frequency at any point in time
        Glide modulatorFrequency = new Glide(hb.ac, MODULATOR_RATE_FREQUENCY);
        Glide modulatorDepthFrequency = new Glide(hb.ac, MODULATOR_DEPTH_FREQUENCY);
        Glide carrierFrequency = new Glide(hb.ac, CARRIER_FREQUENCY);

        // We need to create a sine wave that will change the modulatorDepth
        WavePlayer FM_modulator = new WavePlayer(hb.ac, modulatorFrequency, Buffer.SINE);

        // We need to create a function to define what the frequency of the carrier wavePlayer will be
        // As the value of the FM_modulator changes from -1, through 0, and then to 1 and then back
        // The output of this function will give a value ranging from 500 to 1500
        Function modFunction = new Function(FM_modulator, modulatorDepthFrequency, carrierFrequency) {
            @Override
            public float calculate() {
                return
                        // this is first argument of the function, the FM_modulator.
                        x[0] // This is swinging from -1 to + 1
                        * x[1] // this is the second argument, which is our depth x[0] * x[1] gives a value of -500 to +500
                        + x[2]; // this is our third argument, carrierFrequency. Adding x[0] * x[1] to this value will give 500 to 1500
            }
        };

        // This is our actual wavePlayer for making sound. The frequency is the current value of modFunction
        WavePlayer generatedFMWaveform = new WavePlayer(hb.ac, modFunction, Buffer.SINE);

        // set up a gain amplifier to control the volume
        Gain gainAmplifier = new Gain(hb.ac, NUMBER_AUDIO_CHANNELS, audioVolume);

        // connect our WavePlayer object into the Gain object
        gainAmplifier.addInput(generatedFMWaveform);

        // Now plug the gain object into the audio output
        hb.ac.out.addInput(gainAmplifier);

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
