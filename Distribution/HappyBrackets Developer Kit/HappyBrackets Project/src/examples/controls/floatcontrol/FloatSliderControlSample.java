package examples.controls.floatcontrol;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.FloatSliderControl;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch creates a simple sine wave whose frequency and gain are controlled
 * by a dynamicControl that will display as a slider
 */
public class FloatSliderControlSample implements HBAction {
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    @Override
    public void action(HB hb) {
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");


        final float CENTRE_FREQUENCY = 1000; // this is the frequency of the waveform we will make
        final float FREQUENCY_VARIATION = 500; // This is how much we will vary frequency around centre frequency
        final float MAX_VOLUME = 0.1f; // define how loud we want the sound

        WaveModule player = new WaveModule(CENTRE_FREQUENCY, MAX_VOLUME, Buffer.SINE);
        player.connectTo(hb.ac.out);



        /*************************************************************
         * Create Float type Dynamic Controls that display as sliders
         * Simply type floatSliderControl to generate them
         *************************************************************/

        // Now add a dynamicControl to set the frequency

        /* Type floatSliderControl to generate this code */
        FloatSliderControl frequencyControl = new FloatSliderControl(this, "Frequency", CENTRE_FREQUENCY, CENTRE_FREQUENCY - FREQUENCY_VARIATION, CENTRE_FREQUENCY + FREQUENCY_VARIATION) {
            @Override
            public void valueChanged(double control_val) { /* Write your DynamicControl code below this line */

                // set our frequency to the control value
                player.setFrequency(control_val);
                /* Write your DynamicControl code above this line */
            }
        };/* End DynamicControl floatSliderControl code */


        // Now add a dynamicControl to set the gain
        /* Type floatSliderControl to generate this code */
        FloatSliderControl gainControl = new FloatSliderControl(this, "Gain", MAX_VOLUME, 0, MAX_VOLUME) {
            @Override
            public void valueChanged(double control_val) {/* Write your DynamicControl code below this line */

                player.setGain(control_val);
                /* Write your DynamicControl code above this line */
            }
        };/* End DynamicControl sliderControl code */


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
