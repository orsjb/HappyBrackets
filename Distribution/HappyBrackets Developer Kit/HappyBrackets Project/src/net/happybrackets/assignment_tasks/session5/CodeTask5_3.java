package net.happybrackets.assignment_tasks.session5;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

/**
 * Tasks in Session 5 are not autograded. Simply explore the behaviour.
 *
 */
public class CodeTask5_3 implements HBAction {

    @Override
    public void action(HB hb) {

        hb.reset();

        //load a set of sounds
        SampleManager.group("Guitar", "data/audio/Nylon_Guitar");

        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(hb.clock.getCount() % 32 == 0) {
                    //play a new random sound
                    Sample s = SampleManager.randomFromGroup("Guitar");
                    hb.sound(new SamplePlayer(hb.ac, s));
                }
            }
        });

    }

}
