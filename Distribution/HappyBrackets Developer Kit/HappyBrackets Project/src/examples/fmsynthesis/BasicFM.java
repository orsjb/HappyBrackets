package examples.fmsynthesis;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * basic FM wave generating a 1KHz carrier with a depth of 500Hz
 * Modulating at a rate of 1Hz
 * We will be using a function to calculate the frequency the carrier wavePlayer needs to be at
 */
public class BasicFM implements HBAction {
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        final float INITIAL_VOLUME = 0.1f; // define how loud we want the sound
        Glide audioVolume = new Glide(INITIAL_VOLUME);

        final float CARRIER_FREQUENCY = 1000; //This is the centre frequency
        final float MODULATOR_DEPTH_FREQUENCY = 500;    // THis is how much we will change the centre frequency
        final float MODULATOR_RATE_FREQUENCY = 1;  // This is the rate at which we will change the frequency

        // We need to create UGen objects so we can use them inside a function
        //these are the parameters that will be used to calculate the frequency at any point in time
        Glide modulatorFrequency = new Glide(MODULATOR_RATE_FREQUENCY);
        Glide modulatorDepthFrequency = new Glide(MODULATOR_DEPTH_FREQUENCY);
        Glide carrierFrequency = new Glide(CARRIER_FREQUENCY);

        // We need to create a sine wave that will change the modulatorDepth
        WavePlayer FM_modulator = new WavePlayer(modulatorFrequency, Buffer.SINE);

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

        // Now make our player using the modFunction as the frequency
        WaveModule player = new WaveModule(modFunction, INITIAL_VOLUME, Buffer.SINE);
        player.connectTo(hb.ac.out);


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
