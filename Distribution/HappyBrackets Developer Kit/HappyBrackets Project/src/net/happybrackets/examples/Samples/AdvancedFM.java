package net.happybrackets.examples.Samples;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

public class AdvancedFM implements HBAction {
    @Override
    public void action(HB hb) {
        // define our parameters
        //these are the parameters that control the FM synth
        Glide modFMFreq = new Glide(hb.ac, 1);
        Glide modFMDepth = new Glide(hb.ac, 500);
        Glide baseFmFreq = new Glide(hb.ac, 1000);

        Envelope mod_envelope = new Envelope(hb.ac, 1);
        mod_envelope.addSegment(5, 5000);
        mod_envelope.addSegment(0.5f, 5000);
        WavePlayer FM_modulator = new WavePlayer(hb.ac, mod_envelope, Buffer.SQUARE);

        Function modFunction = new Function(FM_modulator, modFMDepth, baseFmFreq) {
            @Override
            public float calculate() {
                return x[0] * x[1] + x[2];
            }
        };
        WavePlayer FM_carrier = new WavePlayer(hb.ac, modFunction, Buffer.SINE);

        Gain g = new Gain(hb.ac, 1, 0.1f);
        g.addInput(FM_carrier);
        hb.sound(g);
    }
}
