package net.happybrackets.develop.instruments;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class EnvelopeControl implements HBAction, HBReset {
    // Change to the number of audio Channels on your device
    final int NUMBER_AUDIO_CHANNELS = 1;

    // This variable will become true when the composition is reset
    boolean compositionReset = false;


    Envelope frequencyEnvelope = new Envelope(1000);
    Envelope gainEnvelope = new Envelope(0);

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        WaveModule player = new WaveModule(1000, 0.1, Buffer.SINE);

        player.setFrequency(frequencyEnvelope);
        player.setGain(gainEnvelope);
        frequencyEnvelope.addSegment(1000, 1000);
        frequencyEnvelope.addSegment(2000, 2000);
        frequencyEnvelope.addSegment(100, 2000);

        gainEnvelope.addSegment(0.1f, 1000);
        gainEnvelope.addSegment(0.1f, 2000);
        gainEnvelope.addSegment(0f, 2000, new KillTrigger(player.getKillTrigger()));

        player.connectTo(hb.ac.out);

        /***** Type your HBAction code above this line ******/
    }


    /**
     * Add any code you need to have occur when a reset occurs
     */
    @Override
    public void doReset() {
        compositionReset = true;
        /***** Type your HBReset code below this line ******/

        /***** Type your HBReset code above this line ******/
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
