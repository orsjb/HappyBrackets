package examples.controls.integerControl;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.core.control.IntegerControl;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch creates a simple sine wave whose frequency is controlled
 * by a dynamicControl that will display as a slider
 */
public class IntegerSliderControlSample implements HBAction {
    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        final int CENTRE_FREQUENCY = 1000; // this is the frequency of the waveform we will make
        final int FREQUENCY_VARIATION = 500; // This is how much we will vary frequency around centre frequency
        final float  MAX_VOLUME = 0.1f; // define how loud we want the sound

        WaveModule waveModule = new WaveModule(CENTRE_FREQUENCY, MAX_VOLUME, Buffer.SINE);
        waveModule.connectTo(HB.getAudioOutput());


        // Now add a dynamicControl to set the frequency

        /* Type intSliderControl to generate this code*/
        IntegerControl frequency = new IntegerControl(this, "Frequency", CENTRE_FREQUENCY) {

            @Override
            public void valueChanged(int control_val) {/* Write your DynamicControl code below this line */
                // set our frequency to the control value
                waveModule.setFrequency(control_val);
                /* Write your DynamicControl code above this line */
            }
        }.setDisplayRange(CENTRE_FREQUENCY - FREQUENCY_VARIATION, CENTRE_FREQUENCY + FREQUENCY_VARIATION, DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);/* End DynamicControl frequency code */

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
