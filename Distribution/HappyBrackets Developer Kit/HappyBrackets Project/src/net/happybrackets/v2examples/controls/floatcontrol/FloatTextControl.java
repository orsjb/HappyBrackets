package net.happybrackets.v2examples.controls.floatcontrol;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch creates a simple sine wave whose frequency and gain are controlled
 * by a dynamicControl that will display as a text boxes
 */
public class FloatTextControl implements HBAction {
    @Override
    public void action(HB hb) {

        final float INITIAL_FREQUENCY = 1000; // this is the frequency of the waveform we will make
        final float MAX_VOLUME = 0.1f; // define how loud we want the sound

        Glide waveformFrequency = new Glide(hb.ac, INITIAL_FREQUENCY);
        Glide gainVolume = new Glide(hb.ac, MAX_VOLUME);

        final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using

        // create a wave player to generate a waveform based on frequency and waveform type
        WavePlayer waveformGenerator = new WavePlayer(hb.ac, waveformFrequency, Buffer.SINE);

        // set up a gain amplifier to control the volume. We are using the glide object to control this value
        Gain gainAmplifier = new Gain(hb.ac, NUMBER_AUDIO_CHANNELS, gainVolume);

        // connect our WavePlayer object into the Gain object
        gainAmplifier.addInput(waveformGenerator);

        // Now plug the gain object into the audio output
        hb.ac.out.addInput(gainAmplifier);


        // Now add a dynamicControl to set the frequency

        /*************************************************************
         * Create a Float type Dynamic Control that displays as a text box
         *
         * Simply type floatTextControl to generate this code
         *************************************************************/
        hb.createDynamicControl(this, ControlType.FLOAT, "Frequency", INITIAL_FREQUENCY)
                .addControlListener(control -> {
                    float control_val = (float) control.getValue();

                    /*** Write your DynamicControl code below this line ***/
                    // set our frequency to the control value
                    waveformFrequency.setValue(control_val);

                    /*** Write your DynamicControl code above this line ***/
                });

        // Now add a dynamicControl to set the gain

        /*************************************************************
         * Create a Float type Dynamic Control that displays as a text box
         *
         * Simply type floatTextControl to generate this code
         *************************************************************/
        hb.createDynamicControl(this, ControlType.FLOAT, "Gain", MAX_VOLUME)
                .addControlListener(control -> {
                    float control_val = (float) control.getValue();

                    /*** Write your DynamicControl code below this line ***/
                    // change our gain according to control value
                    gainVolume.setValue(control_val);

                    /*** Write your DynamicControl code above this line ***/
                });


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
