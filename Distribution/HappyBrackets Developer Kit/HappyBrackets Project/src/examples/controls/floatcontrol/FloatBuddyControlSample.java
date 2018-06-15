package examples.controls.floatcontrol;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.FloatBuddyControl;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch creates a simple sine wave whose frequency and gain are controlled
 * by dynamicControl pairs that will display as a sliders and text boxes
 */
public class FloatBuddyControlSample implements HBAction {
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using

    @Override
    public void action(HB hb) {
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        final float CENTRE_FREQUENCY = 1000; // this is the frequency of the waveform we will make
        final float FREQUENCY_VARIATION = 500; // This is how much we will vary frequency around centre frequency
        final float MAX_VOLUME = 0.1f; // define how loud we want the sound

        Glide waveformFrequency = new Glide(CENTRE_FREQUENCY);
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
         * Create a Float type Dynamic Control pair that displays as a slider and text box
         * Simply type floatBuddyControl to generate this code
         *************************************************************/
        FloatBuddyControl floatBuddyControl = new FloatBuddyControl(this, "Frequency", CENTRE_FREQUENCY, CENTRE_FREQUENCY - FREQUENCY_VARIATION, CENTRE_FREQUENCY + FREQUENCY_VARIATION) {
            @Override
            public void valueChanged(double control_val) {
                /*** Write your DynamicControl code below this line ***/
                // set our frequency to the control value
                waveformFrequency.setValue((float)control_val);
                /*** Write your DynamicControl code above this line ***/
            }
        };/*** End DynamicControl floatBuddyControl code ***/



        // Now add a dynamicControl to set the gain

        /*************************************************************
         * Create a Float type Dynamic Control pair that displays as a slider and text box
         * Simply type floatBuddyControl to generate this code
         *************************************************************/
        FloatBuddyControl buddyControl = new FloatBuddyControl(this, "Gain", MAX_VOLUME, 0, MAX_VOLUME) {
            @Override
            public void valueChanged(double control_val) {
                /*** Write your DynamicControl code below this line ***/
                /*** Write your DynamicControl code below this line ***/
                // change our gain according to control value
                gainVolume.setValue((float)control_val);
                /*** Write your DynamicControl code above this line ***/
            }
        };/*** End DynamicControl buddyControl code ***/

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
