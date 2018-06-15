package examples.controls.integerControl;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.IntegerTextControl;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch creates a simple sine wave whose frequency and gain are controlled
 * by a dynamicControl that will display as a text boxes
 */
public class IntegerTextControlSample implements HBAction {
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    @Override
    public void action(HB hb) {
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");


        final int INITIAL_FREQUENCY = 1000; // this is the frequency of the waveform we will make
        final int MAX_VOLUME = 1; // define how loud we want the sound

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
         * Create an integer type Dynamic Control that displays as a text box
         * Simply type intTextControl to generate this code
         *************************************************************/
        IntegerTextControl frequency = new IntegerTextControl(this, "Frequency", INITIAL_FREQUENCY) {
            @Override
            public void valueChanged(int control_val) {
                /*** Write your DynamicControl code below this line ***/
                // set our frequency to the control value
                waveformFrequency.setValue(control_val);
                /*** Write your DynamicControl code above this line ***/
            }
        };/*** End DynamicControl frequency code ***/


        // Now add a dynamicControl to set the gain

        /*************************************************************
         * Create an integer type Dynamic Control that displays as a text box
         * Simply type intTextControl to generate this code
         *************************************************************/
        IntegerTextControl gainControl = new IntegerTextControl(this, "Gain", MAX_VOLUME) {
            @Override
            public void valueChanged(int control_val) {
                /*** Write your DynamicControl code below this line ***/
                // change our gain according to control value
                gainVolume.setValue(control_val);
                /*** Write your DynamicControl code above this line ***/
            }
        };/*** End DynamicControl gainControl code ***/


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
