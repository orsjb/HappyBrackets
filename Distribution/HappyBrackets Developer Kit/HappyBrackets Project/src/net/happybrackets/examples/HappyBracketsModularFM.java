package net.happybrackets.examples;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class HappyBracketsModularFM implements HBAction {
    final float INITIAL_MOD_FREQ = 0;
    final float INITIAL_MOD_DEPTH = 100;
    final float INITIAL_BASE_FREQ = 1000;
    final String CONTROL_PREFIX = "Accel-";

    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void action(HB hb) {

        HBPermFM fm_synth = new HBPermFM(hb, INITIAL_BASE_FREQ);
        fm_synth.setModFreq(INITIAL_MOD_FREQ);
        fm_synth.setModDepth(INITIAL_MOD_DEPTH);
        fm_synth.setGainLevel(0.1f);

        //this is the GUI
        // Creating with no minimum and maximum enables you to just type in correct values
        DynamicControl base_freq = hb.createDynamicControl(this, ControlType.FLOAT,
                "Base Freq", INITIAL_BASE_FREQ, 10, 10000);

        base_freq.addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl dynamicControl) {
                float new_val = (float)dynamicControl.getValue();
                fm_synth.setBaseFreq(new_val);
                System.out.println("Base Freq " +  new_val);
            }
        });

        // we can do a create an object with same name and scope and it's value will be mirrored in this sketch but without max and min
        // First change scope of first control
        base_freq.setControlScope(ControlScope.SKETCH);

        // now create a mirror with same name, type, and scope - note we did not add max and min so we will just display a text box. Also, we are setting the control scope immediately after we create
        hb.createDynamicControl(this, ControlType.FLOAT, "Base Freq", INITIAL_BASE_FREQ).setControlScope(ControlScope.SKETCH);


        // We will do the same on Mod Freq Control. We can set control scope immediatly after creating
        DynamicControl mod_freq = hb.createDynamicControl(this, ControlType.FLOAT, "Mod Freq", INITIAL_MOD_FREQ, 0, 10000)
                .setControlScope(ControlScope.SKETCH);

        mod_freq.addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl dynamicControl) {
                float new_val = (float)dynamicControl.getValue();
                fm_synth.setModFreq(new_val);
                System.out.println("Mod Freq " +  new_val);
            }
        });
        // Create the mirror for it
        hb.createDynamicControl(this, ControlType.FLOAT, "Mod Freq", INITIAL_MOD_FREQ).setControlScope(ControlScope.SKETCH);


        // Create a mirrored pair again. We can add the listener immediately after the set control
        DynamicControl mod_depth = hb.createDynamicControl(this, ControlType.FLOAT, "Mod Depth", INITIAL_MOD_DEPTH, 1, 1000);
        mod_depth.setControlScope(ControlScope.SKETCH)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl dynamicControl) {
                        float new_val = (float)dynamicControl.getValue();
                        fm_synth.setModDepth(new_val);
                        System.out.println("Base Depth " +  new_val);
                    }
                });

        // Now create its mirror
        hb.createDynamicControl(this, ControlType.FLOAT, "Mod Depth", INITIAL_MOD_DEPTH).setControlScope(ControlScope.SKETCH);


        // Add an on off control
        hb.createDynamicControl(ControlType.FLOAT, "Gain", 0.1, 0.0, 1.0).addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl dynamicControl) {
                float level = (float)dynamicControl.getValue();
                fm_synth.setGainLevel(level);
            }
        });

        // We will Simulate the accelerometer here

        // We said X would be Pitch
        hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "x", 0, -1, 1)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl dynamicControl) {
                        float val = (float) dynamicControl.getValue();
                        float freq = (float) Math.pow(100, val + 1) + 50; // this will give us values from 50 to 10050
                        base_freq.setValue(freq);
                    }
                });

        // Y will control the Mod Rate
        hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "y", 0, -1, 1)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl dynamicControl) {
                        float val = (float) dynamicControl.getValue();
                        // we want to make it an int ranging from 8 to 512
                        val += 2; // Now it is 1 t0 3
                        float freq = (float) Math.pow(2, val * 3);
                        mod_freq.setValue(freq);
                    }
                });

        // Z axis will Change mod depth
        hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "z", 0, -1, 1)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl dynamicControl)            {
                        float val = (float) dynamicControl.getValue();
                        // anything off zero will give us a value
                        //Ranging from 0 to 1
                        float abs_val = Math.abs(val);
                        float depth_freq = abs_val * 5000;
                        mod_depth.setValue(depth_freq);

                    }
                });
    }

}

