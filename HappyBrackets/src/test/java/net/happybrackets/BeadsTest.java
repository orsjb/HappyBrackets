package net.happybrackets;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;
import net.happybrackets.controller.gui.WaveformVisualiser;

/**
 * Created by ollie on 10/05/2016.
 *
 * This code is exactly the same as in CodeTask1_1.
 *
 * Modify the code so that you have two WavePlayers, one connected to the left channel playing a sine tone at 500hz, and one connected to the right channel, playing a square wave tone at 750hz. They should be passing through a stereo Gain object with a gain of 0.2.
 *
 */
public class BeadsTest {
    public static void main(String[] args) {
        //set up the AudioContext and start it
        AudioContext ac = new AudioContext();
        ac.start();
        //create a Noise generator and a Gain controller
        Noise n = new Noise(ac);
        Gain g = new Gain(ac, 1, 0.2f);
        //plug it all together
        g.addInput(n);
        ac.out.addInput(g);
        //say something to the console output
        System.out.println("Hello World! We are running Beads.");
        WaveformVisualiser.open(ac);
    }
}
