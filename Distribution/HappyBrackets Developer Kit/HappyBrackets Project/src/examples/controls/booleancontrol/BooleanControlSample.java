package examples.controls.booleancontrol;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.BooleanControl;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch creates a simple sine wave that we can start and stop with a checkbox
 * by a dynamicControl that will display as a check box
 */
public class BooleanControlSample implements HBAction {
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    @Override
    public void action(HB hb) {
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        final float INITIAL_FREQUENCY = 1000; // this is the frequency of the waveform we will make
        final float MAX_VOLUME = 0.1f; // define how loud we want the sound

        Glide waveformFrequency = new Glide(INITIAL_FREQUENCY);
        Glide gainVolume = new Glide(MAX_VOLUME);

        

        // create a wave player to generate a waveform based on frequency and waveform type
        WavePlayer waveformGenerator = new WavePlayer(waveformFrequency, Buffer.SINE);

        // set up a gain amplifier to control the volume. We are using the glide object to control this value
        Gain gainAmplifier = new Gain(NUMBER_AUDIO_CHANNELS, gainVolume);

        // connect our WavePlayer object into the Gain object
        gainAmplifier.addInput(waveformGenerator);

        // Now plug the gain object into the audio output
        hb.ac.out.addInput(gainAmplifier);


        // Now add a dynamicControl to set the frequency

        /*************************************************************
         * Create a Boolean type Dynamic Control that displays as a check box
         * Simply type booleanControl to generate this code
         *************************************************************/
        BooleanControl booleanControl = new BooleanControl(this, "On/ Off", true) {
            @Override
            public void valueChanged(Boolean control_val) {
                /*** Write your DynamicControl code below this line ***/
                // pause waveform if checkbox is off
                waveformGenerator.pause(!control_val);
                /*** Write your DynamicControl code above this line ***/
            }
        };/*** End DynamicControl booleanControl code ***/


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
