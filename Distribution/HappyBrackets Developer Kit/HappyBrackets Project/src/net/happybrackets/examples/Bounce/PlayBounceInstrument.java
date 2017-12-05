package net.happybrackets.examples.Bounce;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class PlayBounceInstrument implements HBAction {
    final float INITIAL_FREQ = 500;
    float multiplier = 0;
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
        HBPermBouncerInstrument bouncer = new HBPermBouncerInstrument(hb, INITIAL_FREQ);

        bouncer.play();
        // Create Our Sound Controls

        // First Freq and its mirror
        DynamicControl freq_control = hb.createDynamicControl(this, ControlType.FLOAT, "Base Freq", INITIAL_FREQ, 100, 10000).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, freq_control.getControlName(), INITIAL_FREQ)
                .setControlScope(ControlScope.SKETCH)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {
                        bouncer.setBaseFreq((float) control.getValue());
                    }
                });

        // Next Speed and its mirror
        DynamicControl speed_control = hb.createDynamicControl(this, ControlType.INT, "Bounce speed", 0, 2, 64).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.INT, speed_control.getControlName(), 0)
                .setControlScope(ControlScope.SKETCH)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {
                        bouncer.setSpeed((int) control.getValue());
                    }
                });


        DynamicControl range_control = hb.createDynamicControl(this, ControlType.FLOAT, "Mod Freq", multiplier, 0, 3).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, range_control.getControlName(), multiplier)
                .setControlScope(ControlScope.SKETCH)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {
                        bouncer.setModFreqFactor((float)control.getValue());
                    }
                });

        DynamicControl depth_control = hb.createDynamicControl(this, ControlType.FLOAT, "Mod depth", multiplier, 0, 3).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, depth_control.getControlName(), multiplier)
                .setControlScope(ControlScope.SKETCH)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {
                        bouncer.setModDepthMultiplier((float)control.getValue());
                    }
                });

        // add an On / Off switch
        hb.createDynamicControl(this, ControlType.BOOLEAN, "On", true)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {
                        boolean playSound = (Boolean) control.getValue();
                        if (playSound)
                        {
                            bouncer.play();
                        }
                        else
                        {
                            bouncer.stop();
                        }

                    }
                });


        // We will Simulate the accelerometer here

        // We said X would be Pitch
        hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "x", 0, -1, 1)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl dynamicControl) {
                        float val = (float) dynamicControl.getValue();
                        float base_freq = (float) Math.pow(100, val + 1) + 50; // this will give us values from 50 to 10050
                        freq_control.setValue((base_freq));
                    }
                });

        // Y will control the speed
        hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "y", 0, -1, 1)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl dynamicControl) {
                        float val = (float) dynamicControl.getValue();
                        // we want to make it an int ranging from 8 to 512
                        val += 2; // Now it is 1 t0 3
                        float speed = (float) Math.pow(2, val * 3);
                        speed_control.setValue(speed);
                    }
                });

        // Z axis will Change mod speed and depth
        hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "z", 0, -1, 1)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl dynamicControl)            {
                        float val = (float) dynamicControl.getValue();
                        // anything off zero will give us a value
                        //Ranging from 0 to 1
                        float abs_val = Math.abs(val);
                        float depth_freq = abs_val * 5000;
                        float mod_freq = abs_val * 10;

                        range_control.setValue(mod_freq);
                        depth_control.setValue(depth_freq);

                    }
                });


    }
}
