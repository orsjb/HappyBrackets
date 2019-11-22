package net.happybrackets.examples;

import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.instruments.SampleModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * Pitch Shift a sample using an envelope by connecting a {@link net.beadsproject.beads.ugens.Envelope} to control the rate via {@link SampleModule#setRate(UGen)}
 */
public class EnvelopePitchShift implements HBAction {
    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");
        //Write your sketch below



        Envelope sampleRateEnvelope =  new Envelope(0);

        final String s = "data/audio/Roje/i-write.wav";
        SampleModule sampleModule = new SampleModule();
        if (sampleModule.setSample(s)) {// Write your code below this line

            sampleModule.setRate(sampleRateEnvelope);
            sampleModule.connectTo(HB.getAudioOutput());

            sampleRateEnvelope.addSegment(2, 3000); // Set Rate to 2X over 3 seconds
            sampleRateEnvelope.addSegment(1, 2000); // Set Rate to 1X over 2 seconds

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
