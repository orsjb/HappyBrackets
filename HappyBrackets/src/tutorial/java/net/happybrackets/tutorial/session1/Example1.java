package net.happybrackets.tutorial.session1;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.ugens.Noise;

/**
 * Created by ollie on 10/05/2016.
 */
public class Example1 {

    public static void main(String[] args) {
        AudioContext ac = new AudioContext();
        Noise n = new Noise(ac);
        ac.out.addInput(n);
        ac.start();
    }
}
