package net.happybrackets.tutorial.session5;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

/**
 * Created by samferguson on 26/07/2016.
 */
public class Example5_4 implements HBAction {

    @Override
    public void action(HB hb) {

//        hb.resetLeaveSounding();

        hb.clock.getIntervalUGen().setValue(500);

        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(hb.clock.getCount() % 16 == 0) {
                    float freq = 500 + hb.rng.nextFloat() * 500;
                    WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SINE);
                    Envelope e = new Envelope(hb.ac, 0.1f);
                    Gain g = new Gain(hb.ac, 1, e);
                    e.addSegment(0, 400, new KillTrigger(g));
                    g.addInput(wp);
                    hb.sound(g);
                    hb.setStatus("Playing freq " + freq);
                }
            }
        });


    }

}
