package examples.controls.booleancontrol;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.BooleanControl;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.instruments.WaveModule;
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

        WaveModule player = new WaveModule(INITIAL_FREQUENCY, MAX_VOLUME, Buffer.SINE);
        player.connectTo(hb.ac.out);


        // Now add a dynamicControl to pause

        /* type booleanControl to generate this code */
        BooleanControl booleanControl = new BooleanControl(this, "On/ Off", true) {
            @Override
            public void valueChanged(Boolean control_val) {/* Write your DynamicControl code below this line */
                // We will pause if checkbox is off
                player.pause(!control_val);

                /* Write your DynamicControl code above this line */
            }
        };/* End DynamicControl booleanControl code */


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
