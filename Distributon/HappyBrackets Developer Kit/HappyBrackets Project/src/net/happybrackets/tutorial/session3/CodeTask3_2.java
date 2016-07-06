package net.happybrackets.tutorial.session3;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.SamplePlayer;

/**
 * Created by ollie on 6/06/2016.
 *
 * Run the code below and see how the group of samples is being loaded.
 *
 * There are two things that could be improved with this code.
 *
 * Firstly, the sound distorts because when too many samples are played, we get too loud. Adjust the volume of the guitar plucks to alleviate this.
 * Secondly, the random repetition of sounds (the same note played twice in a row) does not sound nice. Introduce a fix that stops notes repeating.
 *
 * Also introduce random variation to the volume of the guitar plucks.
 * Now introduce random pitch bend on the end of notes.
 *
 *
 */
public class CodeTask3_2 {

    public static void main(String[] args) {
        AudioContext ac = new AudioContext();
        SampleManager.group("Guitar", "data/audio/Nylon_Guitar");
        ac.start();
        //clock
        Clock c = new Clock(ac, 500);
        ac.out.addDependent(c);
        c.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if (c.getCount() % 4 == 0) {
                    ac.out.addInput(new SamplePlayer(ac, SampleManager.randomFromGroup("Guitar")));
                }
            }
        });
    }

}
