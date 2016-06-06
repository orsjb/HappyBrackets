package net.happybrackets.tutorial.session2;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clock;

/**
 * Created by ollie on 5/06/2016.
 *
 * Fix the bug in the following code (why can't you see the word 'tick' appearing in the console?).
 *
 */
public class CodeTask2_3 {

    public static void main(String[] args) {
        //audio stuff
        AudioContext ac = new AudioContext();
        ac.start();
        //create a Clock
        Clock c = new Clock(ac, 500);
        //add some behaviour that responds to the clock
        c.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                System.out.println("tick");
            }
        });
    }
}
