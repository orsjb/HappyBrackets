package net.happybrackets.tutorial.session2;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;

/**
 * Created by ollie on 5/06/2016.
 *
 * Here is some basic code that shows how to set up a Clock.
 *
 * Complete the following tasks:
 *
 * 1)
 *
 */
public class CodeTask2_2 {

    public static void main(String[] args) {
        //Audio stuff
        AudioContext ac = new AudioContext();
        ac.start();
        //create a Clock
        Clock c = new Clock(ac, 500);
        //important! Make sure your clock is running by adding it as a 'dependent' to some other UGen.
        ac.out.addDependent(c);
        //add some behaviour that responds to the clock
        c.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(c.isBeat()) {
                    System.out.println("-----BEAT------");
                }
                System.out.println("tick " + c.getCount() + " (beat " + c.getBeatCount() + ")");
            }
        });
    }
}
