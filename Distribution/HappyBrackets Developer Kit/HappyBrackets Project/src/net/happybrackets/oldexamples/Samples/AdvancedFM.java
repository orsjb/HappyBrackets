package net.happybrackets.oldexamples.Samples;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * An FM Synthesiser that uses 1KHz carrier with 500Hz Depth
 * The frequency is modulated based on an envelope
 */
public class AdvancedFM implements HBAction {
    @Override
    public void action(HB hb) {
        // define our parameters
        //these are the parameters that control the FM synth
        Glide modFMDepth = new Glide(hb.ac, 500);
        Glide baseFmFreq = new Glide(hb.ac, 1000);

        // Define the Envelope that will modulate
        Envelope mod_envelope = new Envelope(hb.ac, 1);
        mod_envelope.addSegment(10, 5000);
        mod_envelope.addSegment(0.5f, 5000);

        // define our Modulator depth
        WavePlayer FM_modulator = new WavePlayer(hb.ac, mod_envelope, Buffer.SINE);

        // Define the frequency after modulation
        Function modFunction = new Function(FM_modulator, modFMDepth, baseFmFreq) {
            @Override
            public float calculate() {
                return x[0] * x[1] + x[2];
            }
        };

        // Define the carrier of the signal
        WavePlayer FM_carrier = new WavePlayer(hb.ac, modFunction, Buffer.SINE);

        // connect to a gain and sound
        Gain g = new Gain(hb.ac, 1, 0.1f);
        g.addInput(FM_carrier);
        hb.ac.out.addInput(g);
    }

    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
