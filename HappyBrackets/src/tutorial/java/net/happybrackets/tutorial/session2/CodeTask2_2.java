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
 * 1) Add a WavePlayer object that plays continually.
 * 2) Create a Glide object that controls the frequency of the WavePlayer, with a glide time of 500.
 * 3) Use the Clock to update the Glide every 4 beats. Each time choose a new frequency at random, using pitches from a pentatonic scale. Use the Pitch class.
 * 4) Also add a new note that plays every 8 beats. The note should be a square wave, also chosen from the same pentatonic scale. The note should play through an ADSR envelope and be removed once played, using a KillTrigger.
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
