package examples.samples;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.instruments.SampleModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * The sketch will
 */
public class SimpleSamplePlayer implements HBAction
{
    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        /* type basicSampleModule to generate this code */
        // define our sample name
        final String sample_name = "data/audio/long/1979.wav";
        SampleModule sampleModule = new SampleModule();
        if (sampleModule.setSample(sample_name)) {/* Write your code below this line */
            sampleModule.connectTo(HB.getAudioOutput());

            /* Write your code above this line */
        } else {
            HB.HBInstance.setStatus("Failed sample " + sample_name);
        }/* End samplePlayer code */

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
