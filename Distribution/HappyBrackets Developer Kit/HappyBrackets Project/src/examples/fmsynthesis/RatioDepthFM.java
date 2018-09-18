package examples.fmsynthesis;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * basic FM wave generating a carrier frequency with a depth of 50% of the carriet frequency
 * We will use an envelope to change the carrier frequency
 * Modulating at a rate of 0.4Hz
 * We will be using a function to calculate the frequency the carrier wavePlayer needs to be at
 * This time we will make it Modulator a square wave
 * Hold the carrier frequency low with carrierFrequency envelope segment for 5 seconds.
 * The carrier will modulate, switching between 2/3 of carrier frequency to 4/3 of carrier
 *
 * The carrier will move to high frequency after the hold based on carrierFrequency envelope segment
 */
public class RatioDepthFM implements HBAction {
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using

    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        final float INITIAL_VOLUME = 0.1f; // define how loud we want the sound

        final float LOW_CARRIER_FREQUENCY = 1000;  // This is the Lower frequency
        final float HIGH_CARRIER_FREQUENCY = 7000; // This is the higher frequency after envelope
        final float HOLD_FREQUENCY_DURATION = 5000; // we will make envelope hold carrierFrequency fo this duration

        final float MODULATOR_DEPTH_RATIO = 1f / 3f;    // This is the ration of how much we will change the centre frequency by
        final float MODULATOR_RATE_FREQUENCY = .4f;  // This is the rate at which we will change the frequency

        // We need to create UGen objects so we can use them inside a function
        //these are the parameters that will be used to calculate the frequency at any point in time
        Glide modulatorFrequency = new Glide(MODULATOR_RATE_FREQUENCY);
        Glide modulatorDepthRatio = new Glide(MODULATOR_DEPTH_RATIO);
        Envelope carrierFrequency = new Envelope(LOW_CARRIER_FREQUENCY);


        // We need to create a sine wave that will change the modulatorDepth
        WavePlayer FM_modulator = new WavePlayer(modulatorFrequency, Buffer.SQUARE);

        // We need to create a function to define what the frequency of the carrier wavePlayer will be
        // As the value of the FM_modulator changes from -1, through 0, and then to 1 and then back
        // The output of this function will give a value ranging from 2/3 of carrier to 4/3 of carrier
        Function modFunction = new Function(FM_modulator, modulatorDepthRatio, carrierFrequency) {
            @Override
            public float calculate() {
                return
                        // this is first argument of the function, the FM_modulator.
                        x[0] // This is swinging from -1 to + 1
                        * x[1] // this is the second argument, which is our depth x[0] * x[1] gives a value of -1/3 to +1/3
                        * x[2] // this is our third argument our 1/3 multiplied by carrierFrequency gives us +/- 1/3 * the value
                        + x[2]; // this is our third argument, carrierFrequency. Adding the previous calculation gives us 1/3 to 4/3 times carrierFrequency
            }
        };

        // This is our actual wavePlayer for making sound. The frequency is the current value of modFunction

        WaveModule player = new WaveModule(modFunction, INITIAL_VOLUME, Buffer.SINE);
        player.connectTo(hb.ac.out);



        // now we will make our carrier stay steady for 5 seconds
        carrierFrequency.addSegment(LOW_CARRIER_FREQUENCY, HOLD_FREQUENCY_DURATION);

        //make it go to to HIGH centreFrequency very fast - say 5 ms
        carrierFrequency.addSegment(HIGH_CARRIER_FREQUENCY, 0);

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
