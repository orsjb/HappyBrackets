package net.happybrackets.tutorial.session3;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.SamplePlayer;

import java.util.Random;

/**
 * Created by ollie on 6/06/2016.
 *
 * The following code creates noise, just like the Noise object, except it does so using a custom Function class.
 *
 * (Note this is much less efficient than using the Noise object because it actually uses a random number generator, whereas the Noise class plays back a readymade buffer of generated noise. Also, the Function object is a bit less efficient because it makes an individual method call each time it calculates a new sample).
 *
 * Change the code so that the Function object generates a saw tooth wave, oscillating between -0.1 and +0.1, with a frequency defined by a float variable 'freq'. To do this you will need to know the sample rate, using ac.getSampleRate().
 *
 *
 */
public class CodeTask3_3 {

    public static void main(String[] args) {
        AudioContext ac = new AudioContext();
        ac.start();
        Random rng = new Random();
        //a function object
        Function f = new Function(ac.out) {
            @Override
            public float calculate() {
                float nextSampleVal = rng.nextFloat() * 0.2f - 0.1f;
                return nextSampleVal;
            }
        };
        ac.out.addInput(f);
    }

}
