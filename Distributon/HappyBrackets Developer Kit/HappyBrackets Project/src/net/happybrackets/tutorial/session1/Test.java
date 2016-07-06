package net.happybrackets.tutorial.session1;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

/**
 * Created by ollie on 5/07/2016.
 */
public class Test implements HBAction {
    @Override
    public void action(HB hb) {
        hb.reset();
        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(hb.clock.getCount() % 32 == 0) {
                    Envelope env = new Envelope(hb.ac, 0.1f);
                    Gain g = new Gain(hb.ac, 1, env);
                    WavePlayer wp = new WavePlayer(hb.ac, 500, Buffer.SINE);
                    g.addInput(wp);
                    env.addSegment(0, 500, new KillTrigger(g));
                    hb.sound(g);
                }
            }
        });
    }
}
