package net.happybrackets.examples.Bounce;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * The purpose of this Sketch is to develop the appropriate Sketch for running a bouncing Sound On PI
 * X Axis will change the pitch
 * Y Axis will change the speed
 * Z Axis will change modulation of Pitch
 */
public class HappybracketsDevelopBounce implements HBAction{
    float initialFreq = 1000;

    Clock clock;
    float muliplier = 2;


    boolean playSound = true;
    boolean isPlayingSound = false;

    public static void main(String[] args) {

        try {
            //HB.runDebug(HappybracketsDevelopBounce.class);
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void action(HB hb) {

        clock = new Clock(hb.ac, 500);

        Glide modFreq = new Glide(hb.ac, 1);
        Glide modDepth = new Glide(hb.ac, 0);
        Glide baseFreq = new Glide(hb.ac, initialFreq);

        // Create Our Sound Controls

        // First Freq and its mirror
        DynamicControl freq_control = hb.createDynamicControl(this, ControlType.FLOAT, "Base Freq", initialFreq, 100, 10000).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, freq_control.getControlName(), initialFreq)
                .setControlScope(ControlScope.SKETCH)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {
                        baseFreq.setValue((float) control.getValue());
                    }
                });

        // Next Speed and its mirror
        DynamicControl speed_control = hb.createDynamicControl(this, ControlType.INT, "Bounce speed", clock.getTicksPerBeat(), 2, 64).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.INT, speed_control.getControlName(), clock.getTicksPerBeat())
                .setControlScope(ControlScope.SKETCH)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {
                        clock.setTicksPerBeat((int) control.getValue());
                    }
                });


        DynamicControl range_control = hb.createDynamicControl(this, ControlType.FLOAT, "Mod Freq", muliplier, 1, 1000).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, range_control.getControlName(), muliplier)
                .setControlScope(ControlScope.SKETCH)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {
                        modFreq.setValue((float)control.getValue());
                    }
                });

        DynamicControl depth_control = hb.createDynamicControl(this, ControlType.FLOAT, "Mod depth", muliplier, 1, 1000).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.FLOAT, depth_control.getControlName(), muliplier)
                .setControlScope(ControlScope.SKETCH)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {
                        modDepth.setValue ((float)control.getValue());
                    }
                });

        // add an On / Off switch
        hb.createDynamicControl(this, ControlType.BOOLEAN, "On", true)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {
                        playSound = (Boolean) control.getValue();

                    }
                });




        hb.ac.out.addDependent(clock);


        //this is the FM synth
        WavePlayer modulator = new WavePlayer(hb.ac, modFreq, Buffer.SINE);
        Function modFunction = new Function(modulator, modDepth, baseFreq) {
            @Override
            public float calculate() {
                return x[0] * x[1] + x[2];
            }
        };


        WavePlayer wp = new WavePlayer(hb.ac, modFunction, Buffer.SINE);
        //add the gain
        Glide glide = new Glide(hb.ac, 0);
        Gain g = new Gain(hb.ac, 1, glide);
        
        //connect together
        g.addInput(wp);
        hb.ac.out.addInput(g);

        
        clock.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                    long clock_count = clock.getCount();
                    if (clock_count % 16 == 0) {
                        //set gain via glide
                        if (isPlayingSound) {
                            glide.setGlideTime(100);
                            glide.setValue(0);
                            isPlayingSound = false;
                        }
                        else {
                            if (playSound) {
                                glide.setGlideTime(10);
                                glide.setValue(0.1f);
                                isPlayingSound = true;
                            }
                            
                        }
                        
                    }
                }
        });
    }
}
