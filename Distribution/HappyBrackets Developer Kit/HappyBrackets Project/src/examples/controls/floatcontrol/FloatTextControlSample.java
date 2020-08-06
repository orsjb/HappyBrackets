package examples.controls.floatcontrol;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.FloatControl;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch creates a simple sine wave whose frequency and gain are controlled
 * by a dynamicControl that will display as a text boxes
 */
public class FloatTextControlSample implements HBAction {
    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        final float INITIAL_FREQUENCY = 1000; // this is the frequency of the waveform we will make
        final double MAX_VOLUME = 0.1; // define how loud we want the sound

        WaveModule waveModule = new WaveModule(INITIAL_FREQUENCY, MAX_VOLUME, Buffer.SINE);
        waveModule.connectTo(HB.getAudioOutput());

        /*************************************************************
         * Create Float type Dynamic Controls that displays as a text box
         * Simply type floatTextControl to generate them
         *************************************************************/


        // Now add a dynamicControl to set the frequency

        /* Type floatTextControl to generate this code */
        FloatControl frequencyControl = new FloatControl(this, "Frequency", INITIAL_FREQUENCY) {
            @Override
            public void valueChanged(double control_val) {/* Write your DynamicControl code below this line */

                // set our frequency to the control value
                waveModule.setFrequency(control_val);

                /* Write your DynamicControl code above this line */
            }
        };/* End DynamicControl code frequencyControl */

        // Now add a dynamicControl to set the gain

        /* Type floatTextControl to generate this code */
        FloatControl gainControl = new FloatControl(this, "Gain", MAX_VOLUME) {
            @Override
            public void valueChanged(double control_val) {/* Write your DynamicControl code below this line */
                // change our gain according to control value
                waveModule.setGain(control_val);

                /* Write your DynamicControl code above this line */
            }
        };/* End DynamicControl code gainControl */


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
