package net.happybrackets.tutorial.session6;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

/**
 * Created by samferguson on 27/07/2016.
 */
public class SyncExample implements HBAction {

    @Override
    public void action(HB hb) {

        hb.clock.getIntervalUGen().setValue(1000);

        hb.doAtTime(new Runnable() {
            @Override
            public void run() {
                SamplePlayer sp = new SamplePlayer(hb.ac, SampleManager.sample("data/audio/Nylon_Guitar/Clean_A_harm.wav"));
                Gain g = new Gain(hb.ac, 1, 0.3f);
                sp.setKillListener(new KillTrigger(g));
                g.addInput(sp);
                hb.sound(g);
            }
        }, hb.getSynchTime() + 5000);


        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if (hb.clock.getCount() % 16 == 0) {
                    //
                }
            }
        });

    }

}
