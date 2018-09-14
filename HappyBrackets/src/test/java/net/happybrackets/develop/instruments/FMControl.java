package net.happybrackets.develop.instruments;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class FMControl implements HBAction, HBReset {
    // Change to the number of audio Channels on your device
    final int NUMBER_AUDIO_CHANNELS = 1;

    // This variable will become true when the composition is reset
    boolean compositionReset = false;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

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


        WaveModule player = new WaveModule(modFunction, 0.1, Buffer.SINE).connectTo(hb.ac.out);

        /***** Type your HBAction code above this line ******/
    }


    /**
     * Add any code you need to have occur when a reset occurs
     */
    @Override
    public void doReset() {
        compositionReset = true;
        /***** Type your HBReset code below this line ******/

        /***** Type your HBReset code above this line ******/
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
