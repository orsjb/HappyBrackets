package net.happybrackets.tutorial.session1;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.WavePlayer;

import java.util.Random;

/**
 * Created by ollie on 5/06/2016.
 *
 * Using the example code, add a variable N that specifies the number of oscillators, and then write a for-loop that creates N oscillators.
 *
 * Be sure to also regulate the volume to account for the fact that many oscillators will be louder than 1!
 *
 * Try this with 10 or 100 oscillators.
 *
 * The number 50 below dictates the random spread of the oscillator detune. See what happens when you vary this.
 *
 */
public class CodeTask1_3 {

    public static void main(String[] args) {
        //set up the AudioContext and start it
        AudioContext ac = new AudioContext();
        ac.start();
        //create a random number generator
        Random rng = new Random();
        //create a WavePlayer with a slight random detune on the pitch
        WavePlayer wp = new WavePlayer(ac, 500 + rng.nextFloat() * 50, Buffer.SINE);
        ac.out.addInput(wp);
        //ac.out is actually a Gain object, so we can simply turn down the level here...
        ac.out.setGain(0.1f);
    }

}
