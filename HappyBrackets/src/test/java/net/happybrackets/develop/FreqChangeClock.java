package net.happybrackets.develop;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.controller.gui.WaveformVisualiser;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

public class FreqChangeClock implements HBAction {
    int freq = 500;
    Clock clock;
    final String CONTROL_PREFIX = "Accel-";
    //AudioContext ac;

    boolean playSound = true;
    @Override
    public void action(HB hb) {

        DynamicControl freq_control = hb.createDynamicControl(this, ControlType.INT, "freq", 500, 100, 10000);
        DynamicControl speed_control = hb.createDynamicControl(this, ControlType.INT, "speed", 16, 2, 64);

        DynamicControl on_off = hb.createDynamicControl(this, ControlType.BOOLEAN, "On", 1);

        DynamicControl control_x = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "x", 0.0);
        DynamicControl control_y = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "y", 0.0);
        DynamicControl control_z = hb.createDynamicControl(this, ControlType.FLOAT, CONTROL_PREFIX + "z", 0.0);

        control_x.setControlScope(ControlScope.GLOBAL);
        control_y.setControlScope(ControlScope.GLOBAL);
        control_z.setControlScope(ControlScope.GLOBAL);

        // accelerometer values typically go from -1 to +1
        control_x.addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                float val = (float)control.getValue();
                int central_freq = (int) freq_control.getValue();
                float variation = central_freq / 2 * val;
                float new_freq = central_freq - variation;
                freq = Math.round(new_freq);
                System.out.println("New Freq " + freq);

            }
        });
        //freq_control.setControlScope(ControlScope.GLOBAL);
        //speed_control.setControlScope(ControlScope.GLOBAL);

        //set up the audio context
        //ac = new AudioContext();
        //ac.start();

        clock = new Clock(hb.ac, 500);
        hb.ac.out.addDependent(clock);

        freq_control.addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                freq = (int)control.getValue();
            }
        });

        speed_control.addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                clock.setTicksPerBeat((int) control.getValue());
            }
        });

        on_off.addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                playSound = (int) control.getValue() != 0;

            }
        });
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

        //visualiser
        //WaveformVisualiser.open(ac);
    }

}
