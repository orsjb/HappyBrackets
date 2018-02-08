package net.happybrackets.oldexamples.Siren;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class HappyBracketsSiren implements HBAction {
    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void action(HB hb) {

        //these are the parameters that control the FM synth
        Glide modFMFreq = new Glide(hb.ac, 0.2f);
        Glide modFMDepth = new Glide(hb.ac, 200);
        Glide baseFmFreq = new Glide(hb.ac, 800);
        Glide FmGain = new Glide(hb.ac, 0.1f);

        //this is the FM synth
        WavePlayer FM_modulator = new WavePlayer(hb.ac, modFMFreq, Buffer.SINE);
        Function modFunction = new Function(FM_modulator, modFMDepth, baseFmFreq) {
            @Override
            public float calculate() {
                return x[0] * x[1] + x[2];
            }
        };
        WavePlayer FM_carrier = new WavePlayer(hb.ac, modFunction, Buffer.SQUARE);
        Gain g = new Gain(hb.ac, 1, FmGain);
        g.addInput(FM_carrier);
        hb.ac.out.addInput(g);
    }
}
