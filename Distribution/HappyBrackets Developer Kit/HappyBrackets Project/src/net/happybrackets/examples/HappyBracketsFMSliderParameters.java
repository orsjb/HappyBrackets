package net.happybrackets.examples;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

/**
 * This class will create an FM synthesiser and use slider controls to adjust the base frequency, the modulator frequency, and modulaor depth
 */
public class HappyBracketsFMSliderParameters implements HBAction {

    final float INITIAL_MOD_FREQ = 0;
    final float INITIAL_MOD_DEPTH = 100;
    final float INITIAL_BASE_FREQ = 1000;

    @Override
    public void action(HB hb) {

        DynamicControl display_status = hb.createDynamicControl(this, ControlType.TEXT, "Status", "");

        //these are the parameters that control the FM synth
        Glide modFreq = new Glide(hb.ac, INITIAL_MOD_FREQ);
        Glide modDepth = new Glide(hb.ac, INITIAL_MOD_DEPTH);
        Glide baseFreq = new Glide(hb.ac, INITIAL_BASE_FREQ);
        Glide gain = new Glide(hb.ac, 0.1f);

        //this is the FM synth
        WavePlayer modulator = new WavePlayer(hb.ac, modFreq, Buffer.SINE);
        Function modFunction = new Function(modulator, modDepth, baseFreq) {
            @Override
            public float calculate() {
                return x[0] * x[1] + x[2];
            }
        };
        WavePlayer carrier = new WavePlayer(hb.ac, modFunction, Buffer.SINE);
        Gain g = new Gain(hb.ac, 1, gain);
        g.addInput(carrier);
        hb.sound(g);

        //this is the GUI
        DynamicControl base_freq = hb.createDynamicControl(this, ControlType.FLOAT, "Base Freq", INITIAL_BASE_FREQ, 10, 10000);

        base_freq.addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl dynamicControl) {
                float new_val = (float)dynamicControl.getValue();
                baseFreq.setValue(new_val);
                System.out.println("Base Freq " +  new_val);
                display_status.setValue("" + baseFreq.getCurrentValue() + ", " + modFreq.getCurrentValue() + ", " + modDepth.getCurrentValue());
            }
        });

        DynamicControl mod_freq = hb.createDynamicControl(this, ControlType.FLOAT, "Mod Freq", INITIAL_MOD_FREQ, 0, 10000);
        mod_freq.addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl dynamicControl) {
                float new_val = (float)dynamicControl.getValue();
                modFreq.setValue(new_val);
                System.out.println("Mod Freq " +  new_val);
                display_status.setValue("" + baseFreq.getCurrentValue() + ", " + modFreq.getCurrentValue() + ", " + modDepth.getCurrentValue());
            }
        });


        DynamicControl mod_depth = hb.createDynamicControl(this, ControlType.FLOAT, "Mod Depth", INITIAL_MOD_DEPTH, 1, 1000);
        mod_depth.addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl dynamicControl) {
                float new_val = (float)dynamicControl.getValue();
                modDepth.setValue(new_val);
                System.out.println("Base Depth " +  new_val);
                display_status.setValue("" + baseFreq.getCurrentValue() + ", " + modFreq.getCurrentValue() + ", " + modDepth.getCurrentValue());
            }
        });

    }
}
