package examples.basic;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch generates a 1KHz sine wave and plays it through a gain object and output to the device
 */
public class HelloWorld implements HBAction {
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using

    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");



        final float INITIAL_FREQUENCY = 1000; // this is the frequency of the waveform we will make
        final float INITIAL_VOLUME = 0.1f; // define how loud we want the sound

        WaveModule waveModule = new WaveModule(INITIAL_FREQUENCY, INITIAL_VOLUME, Buffer.SINE);

        // Now plug the gain object into the audio output
        waveModule.connectTo(hb.ac.out);




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
