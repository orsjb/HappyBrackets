package net.happybrackets.examples.Samples;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

/**
 * Basic FM wave generating a  1KHz carrier with a depth of 500Hz
 * Modulating at a rate of 1Hz
 */
public class BasicFM implements HBAction {
    @Override
    public void action(HB hb) {
        // define our parameters
        //these are the parameters that control the FM synth
        Glide modFMFreq = new Glide(hb.ac, 1);
        Glide modFMDepth = new Glide(hb.ac, 500);
        Glide baseFmFreq = new Glide(hb.ac, 1000);

        WavePlayer FM_modulator = new WavePlayer(hb.ac, modFMFreq, Buffer.SINE);

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
