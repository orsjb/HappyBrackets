package net.happybrackets.examples;

import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.instruments.SampleModule;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * Pitch Shift a sample in a circle by connecting a {@link net.beadsproject.beads.ugens.WavePlayer} to control the rate via {@link net.happybrackets.core.instruments.SampleModule#setRate(UGen)}
 */
public class CirclePitchShift implements HBAction {
    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");
        //Write your sketch below



        WavePlayer sampleRate =  new WavePlayer(0.1f, Buffer.SINE);


        // type basicSamplePLayer to generate this code
        // define our sample name
        final String s = "data/audio/Roje/i-write.wav";
        SampleModule sampleModule = new SampleModule();
        if (sampleModule.setSample(s)) {// Write your code below this line

            sampleModule.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
            //sampleRate.connectTo(sampleModule.getSamplePlayer());
            sampleModule.setRate(sampleRate);
            sampleModule.connectTo(HB.getAudioOutput());


            // Write your code above this line
        } else {
            HB.HBInstance.setStatus("Failed sample " + s);
        }// End samplePlayer code


        // write your code above this line
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
