package net.happybrackets.examples;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

/**
 * The purpose of this Sketch is to develop the appropriate Sketch for running a bouncing Sound On PI
 * X Axis will change the pitch
 * Y Axis will change the speed
 * Z Axis will change modulation of Pitch
 */
public class HappybracketsDevelopBounce implements HBAction{
    int freq = 500;

    Clock clock;
    final String CONTROL_PREFIX = "Accel-";
    int muliplier = 2;


    boolean playSound = true;

    @Override
    public void action(HB hb) {

        clock = new Clock(hb.ac, 500);


        // Create Our Sound Controls

        // First Freq and its mirror
        DynamicControl freq_control = hb.createDynamicControl(this, ControlType.INT, "freq", freq, 100, 10000).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.INT, "freq", freq)
                .setControlScope(ControlScope.SKETCH)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {
                        freq = (int) control.getValue();
                    }
                });

        // Next Speed and its mirror
        DynamicControl speed_control = hb.createDynamicControl(this, ControlType.INT, "speed", clock.getTicksPerBeat(), 2, 64).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.INT, "speed", clock.getTicksPerBeat())
                .setControlScope(ControlScope.SKETCH)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {
                        clock.setTicksPerBeat((int) control.getValue());
                    }
                });


        DynamicControl range_control = hb.createDynamicControl(this, ControlType.INT, "range", muliplier, 1, 12).setControlScope(ControlScope.SKETCH);
        hb.createDynamicControl(this, ControlType.INT, "range", muliplier).setControlScope(ControlScope.SKETCH);

        DynamicControl x_simulator = hb.createDynamicControl(this, ControlType.FLOAT, "x-Simulator", 0.0, -1.0, 1.0);

        // add an On / Off switch
        hb.createDynamicControl(this, ControlType.BOOLEAN, "On", true)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {
                        playSound = (Boolean) control.getValue();

                    }
                });


        DynamicControl control_x = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "x", 0.0);
        DynamicControl control_y = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "y", 0.0);
        DynamicControl control_z = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "z", 0.0);


        DynamicControl output_freq_contol = hb.createDynamicControl(this, ControlType.INT,   "Output Freq", 0);

        // accelerometer values typically go from -1 to +1
        DynamicControl.DynamicControlListener x_listener = control_x.addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                float val = (float)control.getValue();
                float abs = val + 1; // This will give us values from 1 to greater
                int central_freq = (int) freq_control.getValue();
                //float variation = central_freq / 2 * val;
                float new_freq = central_freq  *  (float) Math.pow(muliplier, abs);
                freq = Math.round(new_freq);
                System.out.println("New Freq " + freq);
                output_freq_contol.setValue(freq);
            }
        });

        x_simulator.addControlListener(x_listener);

        range_control.addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                muliplier = (int)control.getValue();
            }
        });


        hb.ac.out.addDependent(clock);



        clock.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if (clock.getCount() % 16 == 0 && playSound) {
                    //add the waveplayer

                    WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SINE);
                    //add the gain
                    Envelope e = new Envelope(hb.ac, 0.1f);
                    Gain g = new Gain(hb.ac, 1, e);
                    e.addSegment(0, 200, new KillTrigger(g));
                    //connect together
                    g.addInput(wp);
                    hb.ac.out.addInput(g);
                }
            }
        });
    }
}
